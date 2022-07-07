package com.app.lunchsolver.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum RestaurantType {
    KOREAN("한식"),
    WESTERN("양식"),
    ASIAN("아시아음식"),
    JAPAN("일식"),
    CHINESE("중식"),
    SNACK("분식"),
    CAFE("카페"),
    BUFFET("뷔페"),
    OTHERS("기타")
    ;

    private final String label;

    public static List<String> getLabels(){
        List<String> labels = new ArrayList<>();
        Arrays.stream(RestaurantType.values()).forEach(type -> labels.add(type.getLabel()));
        return labels;
    }
}
