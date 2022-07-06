package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.GetRestaurantResponse;
import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.entity.restaurant.RestaurantsRepository;
import com.app.lunchsolver.util.BaseUtility;
import com.app.lunchsolver.util.NaverUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import javax.swing.text.html.HTML;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest
class RestaurantServiceImplTest {

    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BaseUtility utility;

    @Autowired
    private NaverUtility naverUtility;
    private final String HOST_v1 = "https://pcmap.place.naver.com";

    private final String HOST_v2 = "https://pcmap-api.place.naver.com";
    private final String x =  "126.9858499";
    private final String y =  "37.560042";
    private final String bounds = "126.9738873;37.5502692;126.9980272;37.5696434";

    Gson gson = new Gson();

    @Autowired
    RestaurantsRepository restaurantsRepository;

    @AfterEach
    public void cleanUp(){
        restaurantsRepository.deleteAll();
    }


    @Test
    @DisplayName("")
    public void getRestaurantData_v1 () throws Exception {
        // given
        String url = "/restaurant/list";
        String _url = HOST_v1+url;
        GetRestaurantRequest request = GetRestaurantRequest.builder()
                .x(x)
                .y(y)
                .bounds("126.9738873;37.5502692;126.9980272;37.5696434")
                .query("음식점").
        build();
        Map<String, String> params =  objectMapper.convertValue(request,Map.class);
        System.out.println(params);
        String uriBuilder = utility.uriParameterBuilder(params,_url);

        HttpHeaders httpHeaders = utility.getDefaultHeader();

        HttpEntity requestMessage = new HttpEntity(httpHeaders);
        System.out.println(uriBuilder);
        // when
        ResponseEntity response = restTemplate.exchange(
                uriBuilder,
                HttpMethod.GET,
                requestMessage,
                String.class);

        // 음식점 정보들 파싱
        Document doc = Jsoup.parse((String) response.getBody());

        Element scriptElement = doc.getElementsByTag("script").get(2);
        String innerJson =  scriptElement.childNode(0).toString();
        int start = innerJson.indexOf("window.__APOLLO_STATE__");
        int end = innerJson.indexOf("window.__PLACE_STATE__");
        // JSON으로 파싱
        JSONObject target = new JSONObject(innerJson.substring(start,end).substring(25));

        JSONArray jsonArray = target.names();
        List<String> restaurantList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String possible = jsonArray.get(i).toString();
            // 레스토랑 정보를 갖고 있는 곳은 RestaurantListSummary:XXXXXX 의 형태를 띄며 한번의 스크래핑에서 50개의 결과값이 나오게 된다
            if(possible.contains("RestaurantListSummary") && Character.isDigit(possible.charAt(possible.length()-2))){
                restaurantList.add(possible);
            }
        }

        List<GetRestaurantResponse> results = new ArrayList<GetRestaurantResponse>();
        for (String s : restaurantList) {
            // 해당 JObject와 Response 객체간의 매핑
            GetRestaurantResponse mapped_data = gson.fromJson(target.get(s).toString(),GetRestaurantResponse.class);
            results.add(mapped_data);
        }
        // then

    }

    @Test
    @DisplayName("")
    public void getRestaurantData_v2 () throws Exception {

        // given
        String url = "/graphql";
        String _url = HOST_v2+url;
        GetRestaurantRequest request = GetRestaurantRequest.builder()
                .x(x)
                .y(y)
                .bounds("126.9738873;37.5502692;126.9980272;37.5696434")
                .query("음식점").
                build();
        String jsonOperation = naverUtility.getRestaurants(request);


        HttpHeaders httpHeaders = utility.getDefaultHeader();

        HttpEntity requestMessage = new HttpEntity(new JSONArray(jsonOperation).toString(),httpHeaders);

        // when
        ResponseEntity response = restTemplate.exchange(
                _url,
                HttpMethod.POST,
                requestMessage,
                String.class);
        List<Restaurant> entities = new ArrayList<>();

        JSONArray datas = new JSONArray(response.getBody().toString());
        datas.getJSONObject(0);

        JSONArray items = datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").getJSONArray("items");
        for (int i = 0; i < 100; i++) {
            GetRestaurantResponse mapped_data = gson.fromJson(items.getString(i),GetRestaurantResponse.class);
        //1. first map with entity : 엔티티와 매핑하기전 validation을 거친다
            Restaurant restaurant = Restaurant.builder()
                    .id(Long.parseLong(mapped_data.getId()))
                    .address(mapped_data.getAddress())
                    .category(mapped_data.getCategory())
                    .imageUrl(mapped_data.getImageUrl()==null?"":URLDecoder.decode(mapped_data.getImageUrl(),"UTF-8"))
                    .name(mapped_data.getName())
                    .distance(utility.stringToLongDistance(mapped_data.getDistance()))
                    .businessHours(mapped_data.getBusinessHours())
                    .visitorReviewScore(mapped_data.getVisitorReviewScore()==null? 0.0 : Double.parseDouble(mapped_data.getVisitorReviewScore()))
                    .saveCount(utility.stringToLongSaveCnt(mapped_data.getSaveCount()))
                    .bookingReviewScore(mapped_data.getBookingReviewScore())
                    .build();
            entities.add(restaurant);
        }

        restaurantsRepository.saveAll(entities);

        System.out.println("저장 완료");
        // then  : save Entities

    }
}