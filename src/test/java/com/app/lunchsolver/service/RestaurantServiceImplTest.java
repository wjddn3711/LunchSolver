package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static org.hamcrest.MatcherAssert.assertThat;


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



    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
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
    @DisplayName("카테고리별 100개 스크래핑하여 DB에 담기")
    @BeforeEach
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
            int total = Integer.parseInt(datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").get("total").toString());
            int maxCnt = total<100? total:100;
            for (int i = 0; i < maxCnt; i++) {
                GetRestaurantResponse mapped_data = gson.fromJson(items.get(i).toString(),GetRestaurantResponse.class);
                //1. first map with entity : 엔티티와 매핑하기전 validation을 거친다
                System.out.println(mapped_data);
                Restaurant restaurant = Restaurant.builder()
                        .id(mapped_data.getId())
                        .address(mapped_data.getAddress())
                        .category(mapped_data.getCategory()==null?"없음": mapped_data.getCategory())
                        .imageUrl(mapped_data.getImageUrl()==null?"":URLDecoder.decode(mapped_data.getImageUrl(),"UTF-8"))
                        .name(mapped_data.getName())
//                        .distance(utility.stringToLongDistance(mapped_data.getDistance()))
                        .businessHours(mapped_data.getBusinessHours())
                        .visitorReviewScore(mapped_data.getVisitorReviewScore()==null? 0.0 : Double.parseDouble(mapped_data.getVisitorReviewScore()))
                        .saveCount(utility.stringToLongSaveCnt(mapped_data.getSaveCount()))
                        .bookingReviewScore(mapped_data.getBookingReviewScore())
                        .restaurantType(type)
                        .x(mapped_data.getX())
                        .y(mapped_data.getY())
                        .build();
                entities.add(restaurant);
            }
            restaurantsRepository.saveAll(entities);

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

    @Test
    public void 좌표값을기준으로1km반경내음식점찾기() throws Exception{
        //given
        AddressDTO request = new AddressDTO(126.9738873,37.5502692);
        //when
        List<RestaurantDTOInterface> r = restaurantsRepository.getRestaurantByLocation(request.getX(), request.getY());
        //then
        for (RestaurantDTOInterface restaurantDTOInterface : r) {
            log.info(restaurantDTOInterface.getName());
            log.info(restaurantDTOInterface.getDiff_Distance()+"");
        }
    }

    @Test
    @DisplayName("x,y 좌표값을 받아와 위치기반 가까운 거리의 매장정보를 반환")
    public void getRestaurantDTOfromDB () throws Exception {
        // given
        AddressDTO request = new AddressDTO(126.9738873,37.5502692);
        // when
        List<RestaurantDTO> dtos = new ArrayList<RestaurantDTO>(); // 초기화진행
        List<Restaurant> restaurantList = restaurantsRepository.findAll();
        for (Restaurant restaurant : restaurantList) {
            // repository 의 db값 -> response dto 로 변환 (거리 계산을 해야하기 때문에 엔티티 그대로 모델로 사용하지못함)
//            dtos.add(RestaurantDTO.builder()
//                    .address(restaurant.getAddress())
//                    .diffDistance(utility.distance(
//                            restaurant.getX(),
//                            restaurant.getY(),
//                            request.getX(),
//                            request.getY(),
//                            "meter"))
//                    .businessHours(restaurant.getBusinessHours())
//                    .restaurantType(restaurant.getRestaurantType())
//                    .bookingReviewScore(restaurant.getBookingReviewScore())
//                    .name(restaurant.getName())
//                    .saveCount(restaurant.getSaveCount())
//                    .saveCount(restaurant.getSaveCount())
//                            .x(restaurant.getX())
//                            .y(restaurant.getY())
//                    .build());

        }
        // then

        for (RestaurantDTO dto : dtos) {
            log.info(dto.toString());
        }
    }
}