package com.example.coffee.util;

import java.util.Map;

public class CosineSimilarityUtil {

    private CosineSimilarityUtil() {}

    public static double cosineSimilarity(Map<Long, Double> v1, Map<Long, Double> v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Map.Entry<Long, Double> entry : v1.entrySet()) {
            double val1 = entry.getValue();
            double val2 = v2.getOrDefault(entry.getKey(), 0.0);
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
        }
        for (double val2 : v2.values()) {
            norm2 += val2 * val2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
