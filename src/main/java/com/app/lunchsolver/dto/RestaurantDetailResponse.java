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
    private JsonObject images;
    private String imgUrl;
}
