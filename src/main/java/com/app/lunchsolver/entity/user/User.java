package com.app.lunchsolver.entity.user;

import com.app.lunchsolver.dto.SessionUser;
import com.app.lunchsolver.entity.BaseTimeEntity;
import com.app.lunchsolver.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Table(name="users")
@Entity
@ToString
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column
    private Double x;

    @Column
    private Double y;

    @Builder
    public User(String name, String email, String picture, Role role) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
    }

    @Builder(builderMethodName = "userXY")
    public User(SessionUser sessionUser){
        this.name = sessionUser.getName();
        this.email = sessionUser.getEmail();
        this.picture = sessionUser.getPicture();
        this.role = Role.USER;
        this.x = sessionUser.getX();
        this.y = sessionUser.getY();
    }

    public User update(String name, String picture){
        this.name = name;
        this.picture = picture;
        return this;
    }

    public User updateXY(Double x, Double y){
        this.x = x;
        this.y = y;
        return this;
    }
    public String getRoleKey(){
        return this.role.getKey();
    }
}