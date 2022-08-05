package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.GetRestaurantResponse;
import com.app.lunchsolver.dto.RestaurantDetailResponse;
import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.entity.restaurant.RestaurantsRepository;
import com.app.lunchsolver.enums.RestaurantType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;


import javax.swing.text.html.HTML;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


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

        for (RestaurantType type : RestaurantType.values()) {
            // 카테고리내의 모든 음식들을 크롤링
            String url = "/graphql";
            String _url = HOST_v2+url;
            GetRestaurantRequest request = GetRestaurantRequest.builder()
                    .x(x)
                    .y(y)
                    .bounds("126.9738873;37.5502692;126.9980272;37.5696434")
                    .query("음식점")
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
                //1. first map with entity : 엔티티와 매핑하기전 validation을 거친다
                Restaurant restaurant = Restaurant.builder()
                        .id(Long.parseLong(mapped_data.getId()))
                        .address(mapped_data.getAddress())
                        .category(mapped_data.getCategory()==null?"없음": mapped_data.getCategory())
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
                if(possible.contains("Menu") && Character.isDigit(possible.charAt(possible.length()-2))){
                    restaurantList.add(possible);
                }
            }
//
            List<RestaurantDetailResponse> results = new ArrayList<RestaurantDetailResponse>();
            for (String s : restaurantList) {
                // 해당 JObject와 Response 객체간의 매핑
                RestaurantDetailResponse mapped_data = gson.fromJson(target.get(s).toString(), RestaurantDetailResponse.class);
                mapped_data.setImgUrl(String.valueOf(mapped_data.getImages().get("json")));
                results.add(mapped_data);
            }
        }
        // when

        // then

    }

    @Value("${kakaoAk.key}")
    private String authorization_key;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Test
    public void givenRouteReturnXY() throws Exception{
        //given
        String route = "양덕로 60";
//        route = URLEncoder.encode(route, "UTF-8"); // 한글이 깨질수있기 때문에 인코딩
        String url = "https://dapi.kakao.com/v2/local/search/address.json";
        UriComponents uri = UriComponentsBuilder.newInstance()
                .fromHttpUrl(url)
                .queryParam("query",route)
                .build();

        //when
        HttpHeaders httpHeaders = utility.getDefaultHeader();
        httpHeaders.add("Authorization", String.format("KakaoAK %s",authorization_key));
        HttpEntity requestMessage = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                uri.toUriString(),
                HttpMethod.GET,
                requestMessage,
                String.class);
        //then
        JSONObject datas = new JSONObject(response.getBody().toString());
        JSONObject addressData = datas.getJSONArray("documents").getJSONObject(0).getJSONObject("address");
        double x = Math.round(Double.parseDouble(addressData.getString("x")) *10000000)/10000000.0;
        double y = Math.round(Double.parseDouble(addressData.getString("y")) *10000000)/10000000.0;
        log.info("x는 "+x);
        log.info("y는 "+y);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    }
}