# BigDataTwitterAnalytics

A distributed Big Data analytics pipeline for real-time and batch processing of Twitter data using Kafka, Apache Flink, Spark, Hive, and Hadoop ecosystem components.

---

## 📌 Project Overview

This project implements an end-to-end data pipeline that:

1. Streams tweet data via Kafka
2. Processes real-time sentiment alerts using Apache Flink
3. Stores structured data in Hive tables
4. Performs batch analytics using Apache Spark
5. Integrates with Hadoop-compatible file systems

The system demonstrates both real-time stream processing and offline batch analytics in a distributed environment.

---

## 🏗 Architecture

Data Flow:

Producer (Python)  
→ Kafka Topic  
→ Apache Flink (Streaming Processing)  
→ Hive / HDFS Storage  
→ Apache Spark (Batch Analytics)

---

## ⚙️ Technologies Used

- Apache Kafka – Distributed event streaming
- Apache Flink – Real-time stream processing
- Apache Spark – Batch data analytics
- Apache Hive – Data warehousing
- Hadoop FileSystem (HDFS plugin)
- Docker Compose – Cluster orchestration
- Java – Flink streaming job
- Python – Kafka producer & Spark batch analysis
- Avro – Data serialization model

---

## 🚀 Real-Time Processing (Flink)

The Flink job:

- Consumes tweet data from Kafka
- Deserializes using Avro model
- Detects negative sentiment patterns
- Generates real-time alerts
- Writes processed results to storage

Key Class:
- `TwitterNegativeAlert.java`

---

## 📊 Batch Processing (Spark)

The Spark job:

- Reads historical tweet data
- Performs aggregations & statistical analysis
- Extracts insights from stored datasets

File:
- `spark/batch_analysis.py`

---

## 🗄 Data Storage (Hive)

Hive tables are created using:

- `hive/create_tables.sql`
- `scripts/setup_hive.hql`

Structured storage enables analytical queries on processed data.

---

## 🐳 Deployment

The system is containerized using Docker Compose:

```bash
docker-compose up
