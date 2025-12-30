package com.factoryops.config;

import com.factoryops.AnomalyService;
import com.factoryops.entity.SensorReading;
import com.factoryops.repository.SensorReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MqttConfig {
    @Autowired private AnomalyService anomalyService;
    private final List<Double> window = new ArrayList<>();

    public MqttConfig() {}

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{"tcp://localhost:1883"});
        options.setUserName("factory_user");
        options.setPassword("factory_pass".toCharArray());
        options.setAutomaticReconnect(true);

        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter("factoryClient", mqttClientFactory(), "factory/temp");
        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(SensorReadingRepository repo) {
        return message -> {
            String payload = (String) message.getPayload();
            System.out.println("Received temp: " + payload);
            try {
                org.json.JSONObject obj = new org.json.JSONObject(payload);
                double val = obj.getDouble("value");

                // normalise exactly like training
                double mean = 25.0, std = 2.0;   // same values used in train.py
                float norm = (float) ((val - mean) / std);

                window.add((double) norm);
                if (window.size() > 50) window.remove(0);

                if (window.size() == 50) {
                    float[] arr = new float[50];
                    for (int i = 0; i < 50; i++) arr[i] = window.get(i).floatValue();
                    double mse = anomalyService.score(arr);
                    System.out.printf("MSE = %.4f  %s%n", mse, mse > 0.05 ? "ANOMALY" : "OK");
                }

                // save to DB regardless
                SensorReading reading = new SensorReading(
                        java.time.Instant.now(),
                        "temp_01",
                        val);
                repo.save(reading);
            } catch (Exception e) {
                System.err.println("Bad payload: " + payload + " - " + e.getMessage());
            }
        };
    }
}
