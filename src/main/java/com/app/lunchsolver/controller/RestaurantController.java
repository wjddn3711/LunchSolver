package com.app.lunchsolver.controller;

import com.app.lunchsolver.entity.restaurant.Restaurant;
import com.app.lunchsolver.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping("/main")
    public String getMain(Model model){
        return "";
    }
}
