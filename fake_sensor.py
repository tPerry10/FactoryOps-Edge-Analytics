import paho.mqtt.client as mqtt, time, random, json, math

client = mqtt.Client()
client.connect("localhost", 1883)

base_temp = 24.0
t = 0
while True:
    signal = base_temp + 2*math.sin(t/50) + random.normalvariate(0, 0.2)
    if random.random() < 0.01:
        signal += 5
        client.publish("factory/temp", json.dumps({"value": round(signal,2), "ts": time.time()}))
        print("sent", signal)
        time.sleep(1)
        t += 1