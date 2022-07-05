package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.util.BaseUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import javax.swing.text.html.HTML;
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
    private final String HOST = "https://pcmap.place.naver.com";

    private final String x =  "126.9858499";
    private final String y =  "37.560042";
    private final String bounds = "126.9738873;37.5502692;126.9980272;37.5696434";




    @Test
    @DisplayName("")
    public void getRestaurantData () throws Exception {
        // given
        String url = "/restaurant/list";
        String _url = HOST+url;
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
            if(possible.contains("RestaurantListSummary") && Character.isDigit(possible.charAt(possible.length()-2))){
                restaurantList.add(possible);
            }
        }

        for (String s : restaurantList) {
            objectMapper.convertValue(target.get(s), GetRestaurantRequest.class);
        }
        // then

    }
}