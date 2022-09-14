package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.*;
import com.app.lunchsolver.entity.menu.Menu;
import com.app.lunchsolver.entity.menu.MenuRepository;
import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.entity.restaurant.RestaurantsRepository;
import com.app.lunchsolver.enums.RestaurantType;
import com.app.lunchsolver.util.BaseUtility;

import com.app.lunchsolver.util.NaverUtility;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    MenuRepository menuRepository;

    @Autowired
    private NaverUtility naverUtility;


    private final String HOST_v1 = "https://pcmap.place.naver.com";
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

            JSONArray datas = new JSONArray(response.getBody().toString());
            datas.getJSONObject(0);

            JSONArray items = datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").getJSONArray("items");
            int total = Integer.parseInt(datas.getJSONObject(0).getJSONObject("data").getJSONObject("restaurants").get("total").toString());
            int maxCnt = total < 100 ? total : 100;
            for (int i = 0; i < maxCnt; i++) {
                GetRestaurantResponse mapped_data = gson.fromJson(items.get(i).toString(), GetRestaurantResponse.class);
                log.info(mapped_data.toString());
                //1. first map with entity : 엔티티와 매핑하기전 validation을 거친다
                Restaurant newJoined = restaurantsRepository.findRestaurantById(mapped_data.getId())
                        .orElse(restaurantsRepository.save(Restaurant.builder()
                                                                        .id(mapped_data.getId())
                                                                        .address(mapped_data.getAddress())
                                                                        .category(mapped_data.getCategory() == null ? "없음" : mapped_data.getCategory())
                                                                        .imageUrl(mapped_data.getImageUrl() == null ? "" : mapped_data.getImageUrl())
                                                                        .name(mapped_data.getName())
                                                                        .businessHours(mapped_data.getBusinessHours())
                                                                        .visitorReviewScore(mapped_data.getVisitorReviewScore() == null ? 0.0 : Double.parseDouble(mapped_data.getVisitorReviewScore()))
                                                                        .saveCount(utility.stringToLongSaveCnt(mapped_data.getSaveCount()))
                                                                        .bookingReviewScore(mapped_data.getBookingReviewScore())
                                                                        .restaurantType(type)
                                                                        .x(mapped_data.getX())
                                                                        .y(mapped_data.getY())
                                                                        .build()));
            }
            log.info("saving in service succeed");
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
    public Page<RestaurantDTO> getRestaurantDTO(AddressDTO request, Pageable pageable) {
        // Point 간의 거리를 통하여 가까운 음식점 정보를 db 에서 매칭
        Page<RestaurantDTOInterface> interfaces = restaurantsRepository.getRestaurantByLocation(request.getX(), request.getY(), pageable);
        // interface -> dto 로
        Page<RestaurantDTO> dtos = RestaurantDTO.interfaceToDto(interfaces);
        return dtos;
    }

    @Override
    public List<MenuDTO> findMenusById(long restaurantId) {
        List<MenuDTO> result = null;
        Restaurant restaurant = restaurantsRepository
                .findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 접근입니다."));
        List<Menu> menus = menuRepository
                .findMenuByRestaurant(restaurant);
        if(menus == null || menus.isEmpty()){
            // 리스트가 비어있다면
            result = crawlMenuDatas(restaurantId, restaurant);
        }
        else{
            // 만약 리스트가 비어있지 않다면 원래 데이터를 DTO로 변환
            result = menus.stream()
                    .map(menu -> new MenuDTO(menu)).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<MenuDTO> crawlMenuDatas(long restaurantId, Restaurant restaurant) {
        log.info("크롤링 시작");
        List<MenuDTO> menus = new ArrayList<>();
        List<Menu> saveDatas = new ArrayList<>();
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
        for (String s : restaurantList) {
            // 해당 JObject와 Response 객체간의 매핑
            MenuDTO mapped_data = gson.fromJson(target.get(s).toString(), MenuDTO.class);
            // 몇몇 음식점은 OTHERS 라고 따로 null 값을 가진 메뉴가 존재 이 경우 저장하지 않음
            if (mapped_data.getPrice() == null
                || mapped_data.getImages() == null) continue;
            Menu data = Menu.builder()
                    .id(mapped_data.getId())
                    .images(Arrays.asList(mapped_data.getImages()))
                    .restaurant(restaurant)
                    .description(mapped_data.getDescription())
                    .name(mapped_data.getName())
                    .priority(mapped_data.getPriority())
                    .price(mapped_data.getPrice())
                    .build();
            menus.add(mapped_data);
            saveDatas.add(data);
        }
        menuRepository.saveAll(saveDatas);
        return menus;
    }
}
