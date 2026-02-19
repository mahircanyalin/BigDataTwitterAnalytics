import csv
import json
import time
from kafka import KafkaProducer

BOOTSTRAP_SERVERS = "localhost:9092"
TOPIC = "tweets_topic"

producer = KafkaProducer(
    bootstrap_servers=BOOTSTRAP_SERVERS,
    value_serializer=lambda v: json.dumps(v).encode("utf-8")
)

with open("data/Tweets.csv", newline="", encoding="utf-8") as csvfile:
    reader = csv.DictReader(csvfile)

    for row in reader:
        msg = {
            "tweet_id": row["tweet_id"],
            "airline_sentiment": row["airline_sentiment"],
            "airline": row["airline"],
            "retweet_count": int(row["retweet_count"]) if row["retweet_count"] else 0,
            "text": row["text"],
            "tweet_created": row["tweet_created"]
        }

        producer.send(TOPIC, msg)
        print("Sent:", msg["airline_sentiment"], msg["airline"])
        time.sleep(0.1)

