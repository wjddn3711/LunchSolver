package com.app.lunchsolver.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class GetRestaurantResponse {
    private long id;
    private String address;
    private String category;
    private String imageUrl;
    private String name;
    private String distance;
    private String businessHours;
    private String visitorReviewScore;
    private String saveCount;
    private Double bookingReviewScore;
    private Double x;
    private Double y;
}
