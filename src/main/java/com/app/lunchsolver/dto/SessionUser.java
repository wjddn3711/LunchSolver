package com.app.lunchsolver.dto;


import com.app.lunchsolver.entity.user.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private String name;
    private String email;
    private String picture;

    // oauth 에서 지정해준 session user 에서 로그인 완료 후 x,y 값을 받아 올 수 있도록 함
    @Setter
    private Double x;
    @Setter
    private Double y;

    public SessionUser(User user){
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
        this.x = user.getX();
        this.y = user.getY();
    }
}