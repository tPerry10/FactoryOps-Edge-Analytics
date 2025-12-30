package com.factoryops;

import ai.onnxruntime.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class AnomalyService {

    private final OrtEnvironment env = OrtEnvironment.getEnvironment();
    private final OrtSession session;

    public AnomalyService() throws OrtException {
        // load model from classpath
        String path = "D:/factoryops/ingestion/src/main/resources/models/model.onnx";
        session = env.createSession(path, new OrtSession.SessionOptions());
    }

    // window = last 50 normalised values
    public double score(float[] window) throws OrtException {
        float[][][] input = new float[1][window.length][1];
        for (int i = 0; i < window.length; i++) input[0][i][0] = window[i];

        OnnxTensor tensor = OnnxTensor.createTensor(env, input);
        OrtSession.Result res = session.run(Map.of("input", tensor));
        float[][][] output = (float[][][]) res.get(0).getValue();

        // MSE between input and reconstruction
        double mse = 0;
        for (int i = 0; i < window.length; i++) {
            double diff = input[0][i][0] - output[0][i][0];
            mse += diff * diff;
        }
        return mse / window.length;
    }
}
