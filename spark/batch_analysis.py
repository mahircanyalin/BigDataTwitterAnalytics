from pyspark.sql import SparkSession
from pyspark.sql.functions import col, count, sum, when, lit

def main():
    # Initialize Spark Session with Hive support
    spark = SparkSession.builder \
        .appName("TwitterBatchAnalysis") \
        .config("spark.hadoop.hive.metastore.uris", "thrift://hive-metastore:9083") \
        .enableHiveSupport() \
        .getOrCreate()

    # Read from Hive table
    # If explicit path needed: spark.read.option("header","true").csv("/project/raw/Tweets.csv")
    # But we try to use the Hive table defined earlier
    df = spark.table("tweets_raw_csv")

    # Perform Aggregations
    # Total tweets per airline
    # Sentiments: airline_sentiment IN ('positive', 'negative', 'neutral')
    
    result = df.groupBy("airline").agg(
        count("*").alias("total_tweets"),
        sum(when(col("airline_sentiment") == "positive", 1).otherwise(0)).alias("positive_count"),
        sum(when(col("airline_sentiment") == "negative", 1).otherwise(0)).alias("negative_count"),
        sum(when(col("airline_sentiment") == "neutral", 1).otherwise(0)).alias("neutral_count")
    ).withColumn("negative_ratio", col("negative_count") / col("total_tweets"))

    # Show results in logs for verification
    result.show()

    # Write to HDFS as Parquet
    # Location: /project/batch_results_parquet/
    # We can write directly to path or insert into table. 
    # Plan says "Writes to HDFS as Parquet files". The Hive table points there.
    output_path = "hdfs://namenode:9000/project/batch_results_parquet/"
    
    result.write.mode("overwrite").parquet(output_path)

    print(f"Batch analysis completed. Results written to {output_path}")

    spark.stop()

if __name__ == "__main__":
    main()
