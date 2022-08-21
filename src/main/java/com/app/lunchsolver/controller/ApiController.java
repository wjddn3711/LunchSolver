package com.app.lunchsolver.controller;

import com.app.lunchsolver.dto.AddressRequest;
import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.service.RestaurantService;
import com.app.lunchsolver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController()
@RequestMapping("/user/api")
@Slf4j
public class ApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RestaurantService restaurantService;

    @PostMapping("/getFullAddress")
    public String getFullAddress(AddressRequest request){
        String result = userService.getAddress(request.getX(), request.getY());
        log.info(request.toString());
        return result;
    }

    @PutMapping("/getRestaurantData")
    public void getRestaurantData(GetRestaurantRequest request){
        try {
            String bounds = String.format("%s;%s;%f;%f",
                    request.getX(),
                    request.getY(),
                    Double.parseDouble(request.getX())+0.0241399,
                    Double.parseDouble(request.getY())+0.0193742);
            log.info("bounds : "+bounds);
            request.setBounds(bounds);
            restaurantService.getRestaurantData(request);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
