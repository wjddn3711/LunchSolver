package com.app.lunchsolver.service;

import com.app.lunchsolver.util.BaseUtility;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

@Service
@Slf4j
public class UserServiceImpl implements UserService{
    @Value("${kakaoAk.key}")
    private String authorization_key;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseUtility utility;

    @Override
    public void getXY(String query) {
        String url = "https://dapi.kakao.com/v2/local/search/address.json";
        UriComponents uri = UriComponentsBuilder.newInstance()
                .fromHttpUrl(url)
                .queryParam("query",query)
                .build();

        HttpHeaders httpHeaders = utility.getDefaultHeader();
        httpHeaders.add("Authorization", String.format("KakaoAK %s",authorization_key));
        HttpEntity requestMessage = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                uri.toUriString(),
                HttpMethod.GET,
                requestMessage,
                String.class);

        JSONObject datas = new JSONObject(response.getBody().toString());
        JSONObject addressData = datas.getJSONArray("documents").getJSONObject(0).getJSONObject("address");
        double x = Math.round(Double.parseDouble(addressData.getString("x")) *10000000)/10000000.0;
        double y = Math.round(Double.parseDouble(addressData.getString("y")) *10000000)/10000000.0;
        log.info("x는 "+x);
        log.info("y는 "+y);
    }

    @Override
    public void getAddress(double x, double y) {
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
        JSONObject datas = new JSONObject(response.getBody().toString());
        JSONObject addressData = datas.getJSONArray("documents").getJSONObject(0).getJSONObject("address_name");
    }

}
