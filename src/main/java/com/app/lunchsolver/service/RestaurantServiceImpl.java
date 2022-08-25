package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.AddressRequest;
import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.GetRestaurantResponse;
import com.app.lunchsolver.dto.RestaurantDTO;
import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.entity.restaurant.RestaurantsRepository;
import com.app.lunchsolver.enums.RestaurantType;
import com.app.lunchsolver.util.BaseUtility;

import com.app.lunchsolver.util.NaverUtility;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RestaurantServiceImpl implements RestaurantService{
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseUtility utility;

    @Autowired
    RestaurantsRepository restaurantsRepository;

    @Autowired
    private NaverUtility naverUtility;

    private final String HOST = "pcmap.place.naver.com";

    private final String HOST_v2 = "https://pcmap-api.place.naver.com/graphql";

    Gson gson = new Gson();

    @Transactional
    @Override
    public void getRestaurantData(GetRestaurantRequest getRestaurantRequest) throws UnsupportedEncodingException {
        for (RestaurantType type : RestaurantType.values()) {
            // 카테고리내의 모든 음식들을 크롤링
            GetRestaurantRequest request = GetRestaurantRequest.builder()
                    .x(getRestaurantRequest.getX())
                    .y(getRestaurantRequest.getY())
                    .bounds(getRestaurantRequest.getBounds())
                    .query("음식점")
                    .type(type)
                    .build();
            log.info("Service 에서 모델 : "+request);

            String _url = HOST_v2;
            String jsonOperation = naverUtility.getRestaurants(request);
            HttpHeaders httpHeaders = utility.getDefaultHeader();

            HttpEntity requestMessage = new HttpEntity(jsonOperation, httpHeaders);
            ResponseEntity response = restTemplate.exchange(
                    _url,
                    HttpMethod.POST,
                    requestMessage,
                    String.class);
            List<Restaurant> entities = new ArrayList<>();

            JSONArray datas = new JSONArray(response.getBody().toString());
            datas.getJSONObject(0);

            JSONArray items = datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").getJSONArray("items");
            int total = Integer.parseInt(datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").get("total").toString());
            int maxCnt = total < 100 ? total : 100;
            for (int i = 0; i < maxCnt; i++) {
                GetRestaurantResponse mapped_data = gson.fromJson(items.get(i).toString(), GetRestaurantResponse.class);
                //1. first map with entity : 엔티티와 매핑하기전 validation을 거친다
                Restaurant restaurant = Restaurant.builder()
                        .id(Long.parseLong(mapped_data.getId()))
                        .address(mapped_data.getAddress())
                        .category(mapped_data.getCategory() == null ? "없음" : mapped_data.getCategory())
                        .imageUrl(mapped_data.getImageUrl() == null ? "" : URLDecoder.decode(mapped_data.getImageUrl(), "UTF-8"))
                        .name(mapped_data.getName())
//                        .distance(utility.stringToLongDistance(mapped_data.getDistance()))
                        .businessHours(mapped_data.getBusinessHours())
                        .visitorReviewScore(mapped_data.getVisitorReviewScore() == null ? 0.0 : Double.parseDouble(mapped_data.getVisitorReviewScore()))
                        .saveCount(utility.stringToLongSaveCnt(mapped_data.getSaveCount()))
                        .bookingReviewScore(mapped_data.getBookingReviewScore())
                        .restaurantType(type)
                        .build();
                entities.add(restaurant);
            }
            log.info("saving in service succeed");
            restaurantsRepository.saveAll(entities);
        }
    }


    @Override
    public void getRestaurantDetail(String url) {
        String _url = HOST +url;
        HttpHeaders httpHeaders = utility.getDefaultHeader();

        HttpEntity request = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                _url,
                HttpMethod.GET,
                request,
                String.class);
    }

    @Override
    public void getRestaurantDTO() {

    }

    @Override
    public List<RestaurantDTO> getRestaurantDTO(AddressRequest request) {
        List<RestaurantDTO> dtos = new ArrayList<RestaurantDTO>(); // 초기화진행
        List<Restaurant> restaurantList = restaurantsRepository.findAll();
        for (Restaurant restaurant : restaurantList) {
            // repository 의 db값 -> response dto 로 변환 (거리 계산을 해야하기 때문에 엔티티 그대로 모델로 사용하지못함)
            dtos.add(RestaurantDTO.builder()
                            .address(restaurant.getAddress())
                            .diffDistance(utility.distance(
                                    restaurant.getX(),
                                    restaurant.getY(),
                                    request.getX(),
                                    request.getY(),
                                    "meter"))
                            .businessHours(restaurant.getBusinessHours())
                            .restaurantType(restaurant.getRestaurantType())
                            .bookingReviewScore(restaurant.getBookingReviewScore())
                            .name(restaurant.getName())
                            .saveCount(restaurant.getSaveCount())
                            .saveCount(restaurant.getSaveCount())
                    .build());
        }
        return dtos;
    }
}
