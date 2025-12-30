package com.factoryops.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sensor_reading")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant time;
    private String sensorId;
    private Double value;

    // Constructors
    public SensorReading() {}
    public SensorReading(Instant time, String sensorId, Double value) {
        this.time = time;
        this.sensorId = sensorId;
        this.value = value;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getTime() { return time; }
    public void setTime(Instant time) { this.time = time; }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
