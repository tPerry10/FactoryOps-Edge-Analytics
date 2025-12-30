import pandas as pd
import numpy as np
import tensorflow as tf
import tf2onnx

# 1. load
df = pd.read_csv('temp.csv', parse_dates=['time'])
temps = df['value'].values
mean, std = temps.mean(), temps.std()
temps = (temps - mean) / std

# 2. sliding window 50
WINDOW = 50
X = []
for i in range(WINDOW, len(temps)):
    X.append(temps[i-WINDOW:i])
X = np.array(X).reshape((-1, WINDOW, 1))

# 3. tiny LSTM auto-encoder
model = tf.keras.Sequential([
    tf.keras.Input(shape=(WINDOW, 1)),
    tf.keras.layers.LSTM(32),
    tf.keras.layers.RepeatVector(WINDOW),
    tf.keras.layers.LSTM(32, return_sequences=True),
    tf.keras.layers.TimeDistributed(tf.keras.layers.Dense(1))
])

model.compile(optimizer='adam', loss='mse')
model.fit(X, X, epochs=20, batch_size=32, verbose=1)

# 4. convert directly to ONNX (CORRECT WAY)
spec = (tf.TensorSpec((None, WINDOW, 1), tf.float32, name="input"),)

tf2onnx.convert.from_keras(
    model,
    input_signature=spec,
    opset=13,
    output_path="model.onnx"
)

print("✅ model.onnx created")
