package com.app.lunchsolver.service;

import com.app.lunchsolver.util.BaseUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestaurantServiceImpl implements RestaurantService{

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseUtility utility;

    private final String HOST = "pcmap.place.naver.com";

    public void getRestaurantData(String url){
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
}
