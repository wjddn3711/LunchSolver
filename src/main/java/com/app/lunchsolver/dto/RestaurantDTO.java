package com.app.lunchsolver.dto;

import com.app.lunchsolver.enums.RestaurantType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RestaurantDTO {

    private long id;

    private String address;

    private String category;

    private String imageUrl;

    private String name;

    private Double diffDistance;

    private String businessHours;

    private Double visitorReviewScore;

    private Long saveCount;

    private Double bookingReviewScore;

    private RestaurantType restaurantType;

    private Double x;

    private Double y;

    @Builder
    public RestaurantDTO(long id, String address, String category, String imageUrl, String name, Double diffDistance, String businessHours, Double visitorReviewScore, Long saveCount, Double bookingReviewScore, RestaurantType restaurantType, Double x, Double y) {
        this.id = id;
        this.address = address;
        this.category = category;
        this.imageUrl = imageUrl;
        this.name = name;
        this.diffDistance = diffDistance;
        this.businessHours = businessHours;
        this.visitorReviewScore = visitorReviewScore;
        this.saveCount = saveCount;
        this.bookingReviewScore = bookingReviewScore;
        this.restaurantType = restaurantType;
        this.x = x;
        this.y = y;
    }
}
