package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.GetRestaurantRequest;

import java.io.UnsupportedEncodingException;

public interface RestaurantService {
    void getRestaurantData(GetRestaurantRequest request) throws UnsupportedEncodingException;
    void getRestaurantDetail(String url);

}
