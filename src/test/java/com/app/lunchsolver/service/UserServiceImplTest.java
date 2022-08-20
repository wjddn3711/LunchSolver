package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.GetRestaurantResponse;
import com.app.lunchsolver.dto.KaKaoMapResponse;
import com.app.lunchsolver.util.BaseUtility;
import com.google.gson.Gson;
import org.json.JSONObject;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class UserServiceImplTest {
    @Autowired
    private BaseUtility utility;

    @Autowired
    private RestTemplate restTemplate;
    @Value("${naverGeo.clientId}")
    private String clientId;
    @Value("${naverGeo.clientSecret}")
    private String clientSecret;


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

    @Test
    @DisplayName("xy를받아주소값반환받는다")
    public void  xy를받아주소값반환() throws Exception {
        // given
        double x = 126.9863309;
        double y = 37.563398;
        String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

        UriComponents uri = UriComponentsBuilder.newInstance()
                .fromHttpUrl(url)
                .queryParam("x",x)
                .queryParam("y",y)
                .build();

        HttpHeaders httpHeaders = utility.getDefaultHeader();
        httpHeaders.add("Authorization", String.format("KakaoAK %s",authorization_key));

        // when
        HttpEntity requestMessage = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                uri.toUriString(),
                HttpMethod.GET,
                requestMessage,
                String.class);
        // then
        // 해당 JObject와 Response 객체간의 매핑
        Gson gson = new Gson();
        KaKaoMapResponse mapped_data = gson.fromJson(response.getBody().toString(),KaKaoMapResponse.class);
        String target = mapped_data.documents.get(0).address_name;
        log.info("target : "+target);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    }


}