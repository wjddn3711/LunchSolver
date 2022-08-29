package com.app.lunchsolver.controller;

import com.app.lunchsolver.config.auth.LoginUser;
import com.app.lunchsolver.dto.AddressDTO;
import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.RestaurantDTO;
import com.app.lunchsolver.dto.SessionUser;
import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.entity.restaurant.RestaurantsRepository;
import com.app.lunchsolver.service.RestaurantService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantsRepository restaurantsRepository;

    @SneakyThrows
    @GetMapping("/addNearest")
    public String addNearestRestaurant(@LoginUser SessionUser user){
        // 신규 주변 음식점 정보가 있다면 insert, 이후 restaurant view 로 이동
        GetRestaurantRequest request = GetRestaurantRequest.builder()
                                        .x(String.valueOf(user.getX()))
                                        .y(String.valueOf(user.getY()))
                                        .build();
        restaurantService.getRestaurantData(request);
        return "redirect:/restaurant/main"; // 메인 페이지로 이동
    }

    @GetMapping("/main")
    public String main(@LoginUser SessionUser user
                        ,Model model){
        // 현재 위치 기준 가까운 순으로 정렬
        AddressDTO request = AddressDTO.builder()
                .x(user.getX())
                .y(user.getY())
                .build();
        // 가까운 음식점 정보를 가져옴
        List<RestaurantDTO> datas = restaurantService.getRestaurantDTO(request);
        model.addAttribute("restaurants", datas);

        return "restaurant";
    }
}
