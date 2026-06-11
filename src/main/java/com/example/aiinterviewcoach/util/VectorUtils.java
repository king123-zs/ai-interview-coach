package com.example.aiinterviewcoach.util;

import java.util.List;
import java.util.stream.Collectors;

public final class VectorUtils {

    private VectorUtils() {
    }

    public static String toPgVectorString(List<Double> vector) {
        return vector.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }
}
