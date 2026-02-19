package com.twitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// ===== FileSink + Avro =====
import org.apache.flink.formats.avro.AvroWriters;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;

import java.time.ZoneId;

public class TwitterNegativeAlert {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(5000); // 5 saniyede bir checkpoint
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(2000);

        // ❗️AYNEN KORUNDU
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("kafka:29092")
                .setTopics("tweets_topic")
                .setGroupId("flink-alert-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> stream =
                env.fromSource(source, WatermarkStrategy.noWatermarks(), "kafka_source");

        ObjectMapper mapper = new ObjectMapper();

        // =================================================
        // 1️⃣ ALERT LOGIC (HİÇ DOKUNULMADI)
        // =================================================
        DataStream<String> alerts = stream
                .map(s -> {
                    try {
                        JsonNode n = mapper.readTree(s);
                        String sentiment = n.path("airline_sentiment").asText("");
                        if ("negative".equals(sentiment)) {
                            String airline = n.path("airline").asText("UNKNOWN");
                            String text = n.path("text").asText("")
                                    .replace("\n", " ").trim();
                            if (text.length() > 140)
                                text = text.substring(0, 140) + "...";
                            return "ALERT [" + airline + "]: " + text;
                        }
                        return "";
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(x -> x != null && !x.isBlank());

        alerts.print();

        // =================================================
        // 2️⃣ AVRO STREAM
        // =================================================
        DataStream<TweetAvro> avroStream = stream
                .map(s -> {
                    try {
                        JsonNode n = mapper.readTree(s);
                        TweetAvro t = new TweetAvro();

                        t.tweet_id = n.path("tweet_id").asText(null);
                        t.airline_sentiment = n.path("airline_sentiment").asText(null);
                        t.airline = n.path("airline").asText(null);
                        t.retweet_count = n.path("retweet_count").asInt(0);
                        t.text = n.path("text").asText(null);
                        t.tweet_created = n.path("tweet_created").asText(null);

                        return t;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(x -> x != null);

        // =================================================
        // 3️⃣ FILESINK (AVRO + HDFS + dt=YYYY-MM-DD)
        // =================================================
        FileSink<TweetAvro> avroSink =
                FileSink.forBulkFormat(
                                new Path("hdfs://namenode:9000/project/streamed_tweets_avro/"),
                                AvroWriters.forReflectRecord(TweetAvro.class)
                        )
                        // dt=YYYY-MM-DD partition
                        .withBucketAssigner(
                                new DateTimeBucketAssigner<>(
                                        "dt=yyyy-MM-dd",
                                        ZoneId.of("UTC")
                                )
                        )
                        .build();

        avroStream.sinkTo(avroSink);

        env.execute("Twitter Negative Alert");
    }
}
