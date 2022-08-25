package com.app.lunchsolver.entity.restaurant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
class RestaurantsRepositoryTest {
    @Autowired
    RestaurantsRepository restaurantsRepository;

    @AfterEach
    public void cleanUp(){
        restaurantsRepository.deleteAll();
    }

    @Test
    @DisplayName("임의의 레스토랑 정보를 저장한후 불러온 매장 정보와 동일한지 확인")
    public void givenRestaurant_whenFindall_thenGetRestaurants () throws Exception {
        // given
        String address = "을지로3가 5926-25846";
        String category = "바(BAR)";
        Long id = 1790381706L;
        String name = "드레싱 베이비";
        Long distance = 810L;
        String businessHours = "평일 11:15~22:00 평일 break 15:00~17:00 | 주말 11:30~22:00 주말 주방재료준비 15:30~16:30";
        Double visitorReviewScore = 3.43;
        Long saveCount = 2000L;
        Double bookingReviewScore = 4.23;
        String imageUrl = "https:\\u002F\\u002Fldb-phinf.pstatic.net\\u002F20210906_227\\u002F16308926152264AxV2_JPEG\\u002FjZKBQcY8beh0d_9fPVHGOpc3.jpg";

//        restaurantsRepository.save(Restaurant.builder()
//                        .address(address)
//                        .category(category)
//                        .id(id)
//                        .name(name)
//                        .distance(distance)
//                        .businessHours(businessHours)
//                        .visitorReviewScore(visitorReviewScore)
//                        .saveCount(saveCount)
//                        .bookingReviewScore(bookingReviewScore)
//                        .imageUrl(imageUrl)
//                        .build()
//                );
;
        // when
        List<Restaurant> restaurantList = restaurantsRepository.findAll();
        System.out.println(restaurantList.get(0));
        // then
        Restaurant restaurant = restaurantList.get(0);
        assertThat(restaurant.getAddress()).isEqualTo(address);
        assertThat(restaurant.getName()).isEqualTo(name);
    }
}