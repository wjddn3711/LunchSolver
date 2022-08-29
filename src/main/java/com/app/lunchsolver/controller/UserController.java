package com.app.lunchsolver.controller;

import com.app.lunchsolver.config.auth.LoginUser;
import com.app.lunchsolver.dto.AddressDTO;
import com.app.lunchsolver.dto.SessionUser;
import com.app.lunchsolver.entity.user.User;
import com.app.lunchsolver.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


    @PostMapping("/postXY")
    public String postXY(AddressDTO dto
            , @LoginUser SessionUser user){
        log.info("inside postXY");
        log.info(user.getEmail());
        log.info(user.getName());
        user.setX(dto.getX());
        user.setY(dto.getY());
        User newUser = userService.saveOrUpdateXY(user);
        httpSession.setAttribute("user", new SessionUser(newUser));

        return "redirect:/restaurant/addNearest";
    }


}
