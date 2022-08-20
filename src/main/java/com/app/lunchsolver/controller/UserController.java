package com.app.lunchsolver.controller;

import com.app.lunchsolver.dto.AddressRequest;
import com.app.lunchsolver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/user/api")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/getFullAddress")
    public String getFullAddress(AddressRequest request){
        String result = userService.getAddress(request.getX(), request.getY());
        log.info(request.toString());
        return result;
    }
}
