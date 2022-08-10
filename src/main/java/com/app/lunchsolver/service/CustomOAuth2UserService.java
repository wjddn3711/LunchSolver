package com.app.lunchsolver.service;


import com.app.lunchsolver.dto.OAuthAttributes;
import com.app.lunchsolver.entity.user.User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface CustomOAuth2UserService extends OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    OAuth2User loadUser(OAuth2UserRequest userRequest);
    User saveOrUpdate(OAuthAttributes attributes);
}
