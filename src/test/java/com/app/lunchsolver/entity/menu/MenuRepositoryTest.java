package com.app.lunchsolver.entity.menu;

import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.GetRestaurantResponse;
import com.app.lunchsolver.dto.MenuDTO;
import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.entity.restaurant.RestaurantsRepository;
import com.app.lunchsolver.enums.RestaurantType;
import com.app.lunchsolver.util.BaseUtility;
import com.app.lunchsolver.util.NaverUtility;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
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

@RunWith(SpringRunner.class)
@SpringBootTest
class MenuRepositoryTest {
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseUtility utility;
    @Autowired
    private RestaurantsRepository restaurantsRepository;

    @Autowired
    private NaverUtility naverUtility;
    private final String HOST_v1 = "https://pcmap.place.naver.com";
    private final String HOST_v2 = "https://pcmap-api.place.naver.com";
    private final String x =  "126.9858499";
    private final String y =  "37.560042";

    Gson gson = new Gson();
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Test
    @BeforeEach
    public void getRestaurantData_v3 () throws Exception {

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
                        .imageUrl(mapped_data.getImageUrl()==null?"": URLDecoder.decode(mapped_data.getImageUrl(),"UTF-8"))
                        .name(mapped_data.getName())
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
    public void findByIdIfNotExistScrap() throws Exception {
        // given
        List<Menu> allMenus = new ArrayList<>();
        List<Long> ids = Arrays.asList(11678758l,
                11678838l,
                11679306l,
                11679353l,
                11679393l,11679455l);
        try {
            for (Long id : ids) {
                Restaurant restaurant = restaurantsRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("restaurant cant be null"));
                List<Menu> menus = menuRepository
                        .findMenuByRestaurant(restaurant);
                if(menus == null || menus.isEmpty()){
                    // 리스트가 비어있다면 스크래핑
                    crawlMenuDatas(id, restaurant);
                }
                allMenus.addAll(menus);
            }
            menuRepository.saveAll(allMenus);
        } catch (Exception err) {
        }
    }

    public List<Menu> crawlMenuDatas(long restaurantId, Restaurant entity) {
        List<Menu> menus = new ArrayList<>();
        try {
            long id = restaurantId;
            String url = String.format("/restaurant/%d/menu/list", id);
            String _url = HOST_v1 + url;

            HttpHeaders httpHeaders = utility.getDefaultHeader();

            HttpEntity requestMessage = new HttpEntity(httpHeaders);
            ResponseEntity response = null;
            // when

            response = restTemplate.exchange(
                    _url,
                    HttpMethod.GET,
                    requestMessage,
                    String.class);


            // 음식점 정보들 파싱
            Document doc = Jsoup.parse((String) response.getBody());

            Element scriptElement = doc.getElementsByTag("script").get(2);
            String innerJson = scriptElement.childNode(0).toString();
            int start = innerJson.indexOf("window.__APOLLO_STATE__");
            int end = innerJson.indexOf("window.__PLACE_STATE__");
            // JSON으로 파싱
            JSONObject target = new JSONObject(innerJson.substring(start, end).substring(25));

            JSONArray jsonArray = target.names();
            List<String> restaurantList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String possible = jsonArray.get(i).toString();
                // 레스토랑 정보를 갖고 있는 곳은 RestaurantListSummary:XXXXXX 의 형태를 띄며 한번의 스크래핑에서 50개의 결과값이 나오게 된다
                if (possible.contains("Menu")) {
                    restaurantList.add(possible);
                }
            }
//
            for (String s : restaurantList) {
                // 해당 JObject와 Response 객체간의 매핑
                MenuDTO mapped_data = gson.fromJson(target.get(s).toString(), MenuDTO.class);
                Menu data = Menu.builder()
                        .id(mapped_data.getId())
                        .restaurant(entity)
                        .description(mapped_data.getDescription())
                        .name(mapped_data.getName())
                        .priority(mapped_data.getPriority())
                        .build();
                menus.add(data);
            }
        }
        catch (Exception err){
            System.out.println(err.getMessage());
        }
        return menus;
    }
}