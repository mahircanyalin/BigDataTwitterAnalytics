# Term Project: High-Performance Analysis of Twitter Data

**Components:**
- **Batch Layer**: Apache Spark (Historical analysis)
- **Speed Layer**: Apache Kafka & Apache Flink (Real-time stream processing)
- **Storage**: HDFS (Data storage)
- **Serving**: Apache Hive (Metadata catalog)

## Setup & Initialization

Before running the demo, ensure your environment is ready.

1.  **Start the Cluster**:
    ```bash
    cd project_code
    docker-compose up -d
    ```
    Wait for a few minutes for all services (Namenode, Datanode, Hive, Spark, Kafka, Flink) to become healthy.

2.  **Initialize HDFS & Hive**:
    *Upload the raw dataset and create Hive tables.*
    
    ```bash
    # Create HDFS directories
    docker exec namenode hdfs dfs -mkdir -p /project/raw/
    
    # Upload Tweets.csv to HDFS (Copy from local to container first)
    docker cp data/Tweets.csv namenode:/tmp/Tweets.csv
    docker exec namenode hdfs dfs -put /tmp/Tweets.csv /project/raw/
    
    # Create Hive Tables
    docker cp scripts/setup_hive.hql hive-server:/tmp/setup_hive.hql
    docker exec hive-server beeline -u jdbc:hive2://localhost:10000 -f /tmp/setup_hive.hql
    ```

## Demo Script (Step-by-Step)

### Step 1: Show the Infrastructure (The "Backend")
*Open your terminal and show that the cluster is healthy.*
```bash
docker ps
```
*"We have a fully containerized environment running Hadoop HDFS, Hive, Spark, Kafka, and Flink."*

### Step 2: Real-Time Stream Demo (The "Speed Layer")
*We will see live alerts.*

1.  **Start the Flink Job** (if not already running):
    ```bash
    cp flink-java/target/flink-java-1.0-SNAPSHOT.jar flink_jobs/
    docker exec flink-jobmanager flink run -d /opt/flink/usrlib/flink-java-1.0-SNAPSHOT.jar
    ```

2.  **Start the Data Stream** (The Producer):
    ```bash
    # Ensure a virtual env is active if needed, or install kafka-python
    pip install kafka-python
    python3 producer.py
    ```
    *(run for 10-20 seconds so logs start flowing)*

3.  **Show the Alerts**:
    *Open a NEW terminal window/tab and run:*
    ```bash
    docker logs -f flink-taskmanager
    ```

### Step 3: Data Archiving Verification

```bash
docker exec hive-server beeline --outputformat=vertical -u jdbc:hive2://localhost:10000 -e "SELECT airline, airline_sentiment, text FROM tweets_stream_avro LIMIT 3;"
```

### Step 4: Batch Analysis Demo (The "Batch Layer")

1.  **Run the Spark Job**:
    ```bash
    docker exec spark /opt/spark/bin/spark-submit --master "local[*]" /opt/spark/batch_analysis.py
    ```

2.  **Show Final Results in Hive**:
    ```bash
  docker exec hive-server beeline --outputformat=vertical -u jdbc:hive2://localhost:10000 -e "SELECT * FROM batch_airline_sentiment;"   ```
