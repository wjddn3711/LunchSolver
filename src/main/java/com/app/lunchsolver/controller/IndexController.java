package com.app.lunchsolver.controller;

import com.app.lunchsolver.config.auth.LoginUser;
import com.app.lunchsolver.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@Controller
@Slf4j
public class IndexController {
    private final HttpSession httpSession;

    @GetMapping
    public String index(Model model, @LoginUser SessionUser user){
        log.info("user : "+user);
        if(user!=null){
            model.addAttribute("userName", user.getName());
        }
        return "index";
    }

    @GetMapping("/xy")
    public String xy(){
        return "xy";
    }
}
