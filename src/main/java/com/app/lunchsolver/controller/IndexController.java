package com.app.lunchsolver.controller;

import com.app.lunchsolver.config.auth.LoginUser;
import com.app.lunchsolver.dto.AddressDTO;
import com.app.lunchsolver.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@Controller
@Slf4j
public class IndexController {
    private final HttpSession httpSession;

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user){
        if(user!=null){
            model.addAttribute("userName", user.getName());
            return "main";
        }
        return "index";
    }

    @GetMapping("/main")
    public String main(){
        return "main";
    }

    @PostMapping("/main")
    public String postXY(@RequestParam double x,
            @RequestParam double y
            , @LoginUser SessionUser user){
        log.info(x+""+y);
        user.setX(x);
        user.setY(y);

        log.info(user.toString());
        return "restaurant";
    }

//    @GetMapping("/success")
//    public String xy(){
//        // 만약 db 에 x,y 가 있다면
//        return "xy";
//    }
}
