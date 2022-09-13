package com.app.lunchsolver.controller;

import com.app.lunchsolver.dto.AddressDTO;
import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.service.RestaurantService;
import com.app.lunchsolver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/user/api")
@Slf4j
public class ApiController {

    @Autowired
    private UserService userService;


    // 위경도를 통해 주소 반환
    @GetMapping("/search/reverse-geo")
    public String getFullAddress(AddressDTO request){
        String result = userService.getAddress(request.getX(), request.getY());
        log.info(request.toString());
        return result;
    }

    // 주소를 통해 위경도 반환
    @GetMapping("/search/geo")
    public AddressDTO getLonLat(@RequestParam(value ="fullAddress") String fullAddress){
        log.info("full address : "+fullAddress);
        return userService.getXY(fullAddress);
    }

//    // deprecated
//    @PutMapping("/getRestaurantData")
//    public void getRestaurantData(GetRestaurantRequest request, HttpServletResponse response){
//        try {
//            String bounds = String.format("%s;%s;%f;%f",
//                    request.getX(),
//                    request.getY(),
//                    Double.parseDouble(request.getX())+0.0241399,
//                    Double.parseDouble(request.getY())+0.0193742);
//            log.info("bounds : "+bounds);
//            request.setBounds(bounds);
//            restaurantService.getRestaurantData(request);
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
