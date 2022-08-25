package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.AddressRequest;
import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.RestaurantDTO;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface RestaurantService {
    void getRestaurantData(GetRestaurantRequest request) throws UnsupportedEncodingException;
    void getRestaurantDetail(String url);

    void getRestaurantDTO();

    List<RestaurantDTO> getRestaurantDTO(AddressRequest request);
}
