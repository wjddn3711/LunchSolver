package com.app.lunchsolver.entity.restaurant;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Getter
@ToString
public class Restaurant {

    // pk
    @Id
    private long id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String distance;

    @Column(nullable = false)
    private String businessHours;

    // 아래 셋은 종합하여  맛집 랭킹에 사용될 예정
    @Column(nullable = false)
    private Double visitorReviewScore;

    @Column(nullable = false)
    private String saveCount;

    @Column(nullable = false)
    private Double bookingReviewScore;

    @Builder
    public Restaurant(long id, String address, String category, String imageUrl, String name, String distance, String businessHours, Double visitorReviewScore, String saveCount, Double bookingReviewScore) {
        this.id = id;
        this.address = address;
        this.category = category;
        this.imageUrl = imageUrl;
        this.name = name;
        this.distance = distance;
        this.businessHours = businessHours;
        this.visitorReviewScore = visitorReviewScore;
        this.saveCount = saveCount;
        this.bookingReviewScore = bookingReviewScore;
    }
}
