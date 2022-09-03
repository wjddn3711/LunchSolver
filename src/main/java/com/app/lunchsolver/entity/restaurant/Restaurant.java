package com.app.lunchsolver.entity.restaurant;

import com.app.lunchsolver.dto.RestaurantDTO;
import com.app.lunchsolver.entity.BaseTimeEntity;
import com.app.lunchsolver.entity.menu.Menu;
import com.app.lunchsolver.enums.RestaurantType;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
public class Restaurant extends BaseTimeEntity {

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

//    @Column(nullable = false)
//    private Long distance;

    @Column(nullable = false, length = 1500)
    private String businessHours;

    // 아래 셋은 종합하여  맛집 랭킹에 사용될 예정
    @Column(nullable = false)
    private Double visitorReviewScore;

    @Column(nullable = false)
    private Long saveCount;

    @Column(nullable = false)
    private Double bookingReviewScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantType restaurantType;

    @Column(nullable = false)
    private Double x;

    @Column(nullable = false)
    private Double y;

    @OneToMany(mappedBy = "restaurant")
    private List<Menu> menus = new ArrayList<>();

    public void newMenu(Menu menu){
        this.menus.add(menu);
    }

    @Builder
    public Restaurant(long id, String address, String category, String imageUrl, String name, String businessHours, Double visitorReviewScore, Long saveCount, Double bookingReviewScore, RestaurantType restaurantType, Double x, Double y) {
        this.id = id;
        this.address = address;
        this.category = category;
        this.imageUrl = imageUrl;
        this.name = name;
//        this.distance = distance;
        this.businessHours = businessHours;
        this.visitorReviewScore = visitorReviewScore;
        this.saveCount = saveCount;
        this.bookingReviewScore = bookingReviewScore;
        this.restaurantType = restaurantType;
        this.x = x;
        this.y = y;
    }
}
