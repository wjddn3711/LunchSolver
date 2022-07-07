package com.app.lunchsolver.dto;

import com.app.lunchsolver.enums.RestaurantType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRestaurantRequest {
    private String query;
    private String x;
    private String y;
    private String bounds;

    private RestaurantType type;
    @Builder
    public GetRestaurantRequest(String query, String x, String y, String bounds,RestaurantType type) {
        this.query = query;
        this.x = x;
        this.y = y;
        this.bounds = bounds;
        this.type = type;
    }
}
