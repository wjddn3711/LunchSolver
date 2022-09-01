package com.app.lunchsolver.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RestaurantDetailResponse {

    private String name;
    private String price;
    private String[] images;
    private String description;
    private String id;
    private int priority;
}
