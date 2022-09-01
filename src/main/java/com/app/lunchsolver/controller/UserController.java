package com.app.lunchsolver.controller;

import com.app.lunchsolver.config.auth.LoginUser;
import com.app.lunchsolver.dto.AddressDTO;
import com.app.lunchsolver.dto.GetRestaurantRequest;
import com.app.lunchsolver.dto.SessionUser;
import com.app.lunchsolver.entity.user.User;
import com.app.lunchsolver.service.UserServiceImpl;
import com.app.lunchsolver.util.BaseUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    private final HttpSession httpSession;

    @Autowired
    private BaseUtility utility;

    @PutMapping("/lon-lat")
    public String postXY(AddressDTO dto
            , @LoginUser SessionUser user){
        log.info("inside put /lon-lat");
        user.setX(dto.getX());
        user.setY(dto.getY());
        User newUser = userService.saveOrUpdateXY(user);
        httpSession.setAttribute("user", new SessionUser(newUser));

        return "redirect:/restaurant/near";
    }
}
