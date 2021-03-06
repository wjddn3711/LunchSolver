package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.GetRestaurantResponse;
import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.entity.restaurant.RestaurantsRepository;
import com.app.lunchsolver.enums.RestaurantType;
import com.app.lunchsolver.util.BaseUtility;
import com.app.lunchsolver.util.NaverUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
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
                .query("?????????").
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

        // ????????? ????????? ??????
        Document doc = Jsoup.parse((String) response.getBody());

        Element scriptElement = doc.getElementsByTag("script").get(2);
        String innerJson =  scriptElement.childNode(0).toString();
        int start = innerJson.indexOf("window.__APOLLO_STATE__");
        int end = innerJson.indexOf("window.__PLACE_STATE__");
        // JSON?????? ??????
        JSONObject target = new JSONObject(innerJson.substring(start,end).substring(25));

        JSONArray jsonArray = target.names();
        List<String> restaurantList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String possible = jsonArray.get(i).toString();
            // ???????????? ????????? ?????? ?????? ?????? RestaurantListSummary:XXXXXX ??? ????????? ?????? ????????? ?????????????????? 50?????? ???????????? ????????? ??????
            if(possible.contains("RestaurantListSummary") && Character.isDigit(possible.charAt(possible.length()-2))){
                restaurantList.add(possible);
            }
        }

        List<GetRestaurantResponse> results = new ArrayList<GetRestaurantResponse>();
        for (String s : restaurantList) {
            // ?????? JObject??? Response ???????????? ??????
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
                .query("?????????").
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
        //1. first map with entity : ???????????? ??????????????? validation??? ?????????
            Restaurant restaurant = Restaurant.builder()
                    .id(Long.parseLong(mapped_data.getId()))
                    .address(mapped_data.getAddress())
                    .category(mapped_data.getCategory()==null?"??????": mapped_data.getCategory())
                    .imageUrl(mapped_data.getImageUrl()==null?"":URLDecoder.decode(mapped_data.getImageUrl(),"UTF-8"))
                    .name(mapped_data.getName())
                    .distance(utility.stringToLongDistance(mapped_data.getDistance()))
                    .businessHours(mapped_data.getBusinessHours())
                    .visitorReviewScore(mapped_data.getVisitorReviewScore()==null? 0.0 : Double.parseDouble(mapped_data.getVisitorReviewScore()))
                    .saveCount(utility.stringToLongSaveCnt(mapped_data.getSaveCount()))
                    .bookingReviewScore(mapped_data.getBookingReviewScore())
                    .restaurantType(RestaurantType.ASIAN)
                    .build();
            entities.add(restaurant);
        }
        restaurantsRepository.saveAll(entities);

        System.out.println("?????? ??????");
        // then  : save Entities

    }
    
    @Test
    @DisplayName("")
    public void getRestaurantAndSaveByType () throws Exception {
        // given
        for (RestaurantType type : RestaurantType.values()) {
            // ?????????????????? ?????? ???????????? ?????????
            String url = "/graphql";
            String _url = HOST_v2+url;
            GetRestaurantRequest request = GetRestaurantRequest.builder()
                    .x(x)
                    .y(y)
                    .bounds("126.9738873;37.5502692;126.9980272;37.5696434")
                    .query("?????????")
                    .type(type)
                    .build();
            String jsonOperation = naverUtility.getRestaurants(request);
            HttpHeaders httpHeaders = utility.getDefaultHeader();

            HttpEntity requestMessage = new HttpEntity(jsonOperation,httpHeaders);
            ResponseEntity response = restTemplate.exchange(
                    _url,
                    HttpMethod.POST,
                    requestMessage,
                    String.class);
            List<Restaurant> entities = new ArrayList<>();

            JSONArray datas = new JSONArray(response.getBody().toString());
            datas.getJSONObject(0);

            JSONArray items = datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").getJSONArray("items");
            int total = Integer.parseInt(datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").getString("total"));
            int maxCnt = total<100? total:100;
            for (int i = 0; i < maxCnt; i++) {
                GetRestaurantResponse mapped_data = gson.fromJson(items.getString(i),GetRestaurantResponse.class);
                //1. first map with entity : ???????????? ??????????????? validation??? ?????????
                Restaurant restaurant = Restaurant.builder()
                        .id(Long.parseLong(mapped_data.getId()))
                        .address(mapped_data.getAddress())
                        .category(mapped_data.getCategory()==null?"??????": mapped_data.getCategory())
                        .imageUrl(mapped_data.getImageUrl()==null?"":URLDecoder.decode(mapped_data.getImageUrl(),"UTF-8"))
                        .name(mapped_data.getName())
                        .distance(utility.stringToLongDistance(mapped_data.getDistance()))
                        .businessHours(mapped_data.getBusinessHours())
                        .visitorReviewScore(mapped_data.getVisitorReviewScore()==null? 0.0 : Double.parseDouble(mapped_data.getVisitorReviewScore()))
                        .saveCount(utility.stringToLongSaveCnt(mapped_data.getSaveCount()))
                        .bookingReviewScore(mapped_data.getBookingReviewScore())
                        .restaurantType(type)
                        .build();
                entities.add(restaurant);
            }
            restaurantsRepository.saveAll(entities);

        }
        List<Long> ids = restaurantsRepository.findAllreturnId();
        for (Long id : ids) {
            System.out.println(id);
        }
        // when
        // then
    }

    @Test
    @DisplayName("")
    public void givenIdListSearchAndSaveRestaurantDetail () throws Exception {
        // given

        List<Long> ids = Arrays.asList(
                11356993l,
                11477706l,
                11592593l,
                11592607l,
                11592643l,
                11592650l,
                11618393l,
                11618456l,
                11619941l,
                11618586l,
                11623970l,
                11664585l,
                11677524l,
                11677544l,
                11677741l,
                11678715l,
                11678758l,
                11678838l,
                11679306l,
                11679353l,
                11679393l,11679455l);
        for (Long id : ids) {
            String url = String.format("/restaurant/%d/menu/list",id);
            String _url = HOST_v1+url;


            HttpHeaders httpHeaders = utility.getDefaultHeader();

            HttpEntity requestMessage = new HttpEntity(httpHeaders);
            // when
            ResponseEntity response = restTemplate.exchange(
                    _url,
                    HttpMethod.GET,
                    requestMessage,
                    String.class);

            // ????????? ????????? ??????
            Document doc = Jsoup.parse((String) response.getBody());

            Element scriptElement = doc.getElementsByTag("script").get(2);
            String innerJson =  scriptElement.childNode(0).toString();
            int start = innerJson.indexOf("window.__APOLLO_STATE__");
            int end = innerJson.indexOf("window.__PLACE_STATE__");
            // JSON?????? ??????
            JSONObject target = new JSONObject(innerJson.substring(start,end).substring(25));

            JSONArray jsonArray = target.names();
            List<String> restaurantList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String possible = jsonArray.get(i).toString();
                // ???????????? ????????? ?????? ?????? ?????? RestaurantListSummary:XXXXXX ??? ????????? ?????? ????????? ?????????????????? 50?????? ???????????? ????????? ??????
                if(possible.contains("Menu") && Character.isDigit(possible.charAt(possible.length()-2))){
                    restaurantList.add(possible);
                }
            }
//
//            List<GetRestaurantResponse> results = new ArrayList<GetRestaurantResponse>();
//            for (String s : restaurantList) {
//                // ?????? JObject??? Response ???????????? ??????
//                GetRestaurantResponse mapped_data = gson.fromJson(target.get(s).toString(),GetRestaurantResponse.class);
//                results.add(mapped_data);
//            }
        }
        // when

        // then

    }
}