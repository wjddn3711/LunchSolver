package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.AddressDTO;
import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.RestaurantDTO;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface RestaurantService {
    void getRestaurantData(GetRestaurantRequest request) throws UnsupportedEncodingException;
    void getRestaurantDetail(String url);


    List<RestaurantDTO> getRestaurantDTO(AddressDTO request);
}
