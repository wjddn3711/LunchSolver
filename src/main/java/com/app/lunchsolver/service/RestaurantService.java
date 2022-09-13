package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.AddressDTO;
import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.MenuDTO;
import com.app.lunchsolver.dto.RestaurantDTO;
import com.app.lunchsolver.entity.menu.Menu;
import com.app.lunchsolver.entity.restaurant.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface RestaurantService {
    void getRestaurantData(GetRestaurantRequest request) throws UnsupportedEncodingException;
    void getRestaurantDetail(String url);

    Page<RestaurantDTO> getRestaurantDTO(AddressDTO request, Pageable pageable);

    List<MenuDTO> findMenusById(long restaurantId);

    List<MenuDTO> crawlMenuDatas(long restaurantId, Restaurant restaurant);
}
