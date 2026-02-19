package com.twitter;

import java.io.Serializable;

public class TweetAvro implements Serializable {
    public String tweet_id;
    public String airline_sentiment;
    public String airline;
    public Integer retweet_count;
    public String text;
    public String tweet_created;

    // partition için
    public String dt;

    // Flink POJO için boş constructor şart
    public TweetAvro() {}
}
