package com.app.lunchsolver.entity.restaurant;

import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.GetRestaurantResponse;
import com.app.lunchsolver.dto.RestaurantDetailResponse;
import com.app.lunchsolver.entity.menu.Menu;
import com.app.lunchsolver.entity.menu.MenuRepository;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;

@RunWith(SpringRunner.class)
@SpringBootTest
class RestaurantsRepositoryTest {
    @Autowired
    RestaurantsRepository restaurantsRepository;

    @Autowired
    MenuRepository menuRepository;


    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BaseUtility utility;

    @Autowired
    private NaverUtility naverUtility;

    private final String HOST_v2 = "https://pcmap-api.place.naver.com";
    private final String x =  "126.9858499";
    private final String y =  "37.560042";
    private final String bounds = "126.9738873;37.5502692;126.9980272;37.5696434";
    private final String HOST_v1 = "https://pcmap.place.naver.com";
    Gson gson = new Gson();


    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

//    @AfterEach
    public void cleanUp(){
        restaurantsRepository.deleteAll();
    }



    @Test
    @DisplayName("카테고리별 음식점 모두 스크래핑후 보내기")
    @BeforeEach
    public void getRestaurantData_v2 () throws Exception {
        List<Long> idList = new ArrayList<>();
        List<Restaurant> entities = new ArrayList<>();

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

            JSONArray datas = new JSONArray(response.getBody().toString());
            datas.getJSONObject(0);

            JSONArray items = datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").getJSONArray("items");
            int total = Integer.parseInt(datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").get("total").toString());
            int maxCnt = total < 30 ? total : 30;


            for (int i = 0; i < maxCnt; i++) {
                GetRestaurantResponse mapped_data = gson.fromJson(items.get(i).toString(), GetRestaurantResponse.class);
                //1. first map with entity : 엔티티와 매핑하기전 validation을 거친다
                idList.add(mapped_data.getId()); // idList 에 id 값을 넣는다
                Restaurant restaurant = Restaurant.builder()
                        .id(mapped_data.getId())
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
                        .x(mapped_data.getX())
                        .y(mapped_data.getY())
                        .build();
                entities.add(restaurant);
            }
            restaurantsRepository.saveAll(entities);
        }
        // 아래에서 메뉴 save
        List<Menu> menus = new ArrayList<>();
         for (Restaurant entity : entities) {
            long id = entity.getId();
            String url = String.format("/restaurant/%d/menu/list",id);
            String _url = HOST_v1+url;

            HttpHeaders httpHeaders = utility.getDefaultHeader();

            HttpEntity requestMessage = new HttpEntity(httpHeaders);
             ResponseEntity response = null;
            // when
             try{
                 response = restTemplate.exchange(
                         _url,
                         HttpMethod.GET,
                         requestMessage,
                         String.class);
             }
            catch (Exception e){
                 // 무시
                continue;
            }

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
                if(possible.contains("Menu")){
                    restaurantList.add(possible);
                }
            }
//
            List<RestaurantDetailResponse> results = new ArrayList<RestaurantDetailResponse>();
            for (String s : restaurantList) {
                // 해당 JObject와 Response 객체간의 매핑
                RestaurantDetailResponse mapped_data = gson.fromJson(target.get(s).toString(), RestaurantDetailResponse.class);
                Menu data = Menu.builder()
                                .id(mapped_data.getId())
                                        .restaurant(entity)
                                                .description(mapped_data.getDescription())
                                                        .name(mapped_data.getName())
                                                                        .priority(mapped_data.getPriority())
                                                                                .build();

                menuRepository.save(data);
                results.add(mapped_data);
            }

            for (RestaurantDetailResponse result : results) {
                log.info(result.toString());
            }
        }
        // when

        // then


    }

    @Test
    public void givenRestaurantID_whenFindallexist_thenInsertRestaurants() throws Exception{
        //given
        List<Restaurant> restaurants = restaurantsRepository.findAll();
        //when
        for (Restaurant entity : restaurants) {
            // findby id
            Restaurant newJoined = restaurantsRepository.findRestaurantById(entity.getId())
                    .orElse(restaurantsRepository.save(entity));
        }
        //then
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