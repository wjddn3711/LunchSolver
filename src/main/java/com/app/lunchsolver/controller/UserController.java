package com.app.lunchsolver.controller;

import com.app.lunchsolver.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;

@RestController("/api/v1/user")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

}
