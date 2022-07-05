package com.app.lunchsolver.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GetRestaurantResponse {
    private long id;
    private String address;
    private String category;
    private String imageUrl;
    private String name;
    private String distance;
    private String businessHours;
    private Double visitorReviewScore;
    private String saveCount;
    private Double bookingReviewScore;
}
