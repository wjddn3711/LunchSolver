package com.app.lunchsolver.controller;

import com.app.lunchsolver.config.auth.LoginUser;
import com.app.lunchsolver.dto.*;
import com.app.lunchsolver.service.RestaurantService;
import com.app.lunchsolver.util.BaseUtility;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private BaseUtility utility;

    @SneakyThrows
    @GetMapping("/near")
    public String addNearestRestaurant(@LoginUser SessionUser user){
        // 신규 주변 음식점 정보가 있다면 insert, 이후 restaurant view 로 이동
        GetRestaurantRequest request = GetRestaurantRequest.builder()
                                        .x(String.valueOf(user.getX()))
                                        .y(String.valueOf(user.getY()))
                                        .bounds(utility.getBoundary(user.getX(), user.getY()))
                                        .build();
        restaurantService.getRestaurantData(request);
        return "redirect:/restaurant/main"; // 메인 페이지로 이동
    }

    @GetMapping("/main")
    public String main(@LoginUser SessionUser user
                        , Model model
                        , Pageable pageable){
        // 현재 위치 기준 가까운 순으로 정렬
        AddressDTO request = AddressDTO.builder()
                .x(user.getX())
                .y(user.getY())
                .build();
        // 가까운 음식점 정보를 가져옴
        Page<RestaurantDTO> datas = restaurantService.getRestaurantDTO(request, pageable);
        model   .addAttribute("restaurants", datas);

        return "restaurant";
    }

        @GetMapping("/{id}")
        public String findById(@PathVariable long id, Model model){
            List<MenuDTO> menus = restaurantService.findMenusById(id);
            model.addAttribute("menus", menus);
            return "restaurant-detail";
        }
}
