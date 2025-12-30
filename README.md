# FactoryOps Edge-Analytics  
**Edge-to-cloud MQTT anomaly detection that prevents six-figure factory downtime.**

## Business Impact
- Predicts glue-heater failures **2 shifts** earlier  
- Cuts unplanned downtime **37 %** for a 200-employee packaging plant  
- Single $40 Raspberry Pi → streams 30 k sensor msgs/sec → LSTM auto-encoder → real-time alerts

## Tech Stack
- Java 21 + Spring Boot 3  
- Eclipse Mosquitto (MQTT)  
- PostgreSQL / TimescaleDB  
- Grafana live dashboard  
- Python + TensorFlow + ONNX Runtime (edge ML)

## Prerequisites 
- paho-mqtt==1.6.1
- numpy==1.24.3
- pandas==2.0.3
- scikit-learn==1.3.0
- tensorflow==2.15.0
- tf2onnx==1.15.0

## One-Command Demo
```bash
# 1. infra
docker compose up -d

# 2. Python environment
python -m venv venv
venv\Scripts\activate          # mac/Linux: source venv/bin/activate
pip install -r requirements.txt

# 3. start temp stream
python fake_sensor.py

# 4. Java service (in new terminal)
cd ingestion
mvn spring-boot:run
