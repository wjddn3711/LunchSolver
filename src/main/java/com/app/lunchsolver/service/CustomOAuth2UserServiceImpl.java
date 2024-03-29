package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.OAuthAttributes;
import com.app.lunchsolver.dto.SessionUser;
import com.app.lunchsolver.entity.user.User;
import com.app.lunchsolver.entity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomOAuth2UserServiceImpl implements CustomOAuth2UserService {
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2UserService<OAuth2UserRequest,OAuth2User> delegate
                = new DefaultOAuth2UserService();
        log.info("userRequest: "+userRequest);
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.
                getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.
                getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.
                of(registrationId,userNameAttributeName,
                        oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes); // 만약 존재하는 id, 즉 email이라면 update, else save

        httpSession.setAttribute("user", new SessionUser(user));


        return new DefaultOAuth2User(
                Collections.singleton(new
                        SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());

    }

    @Override
    public User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(),
                        attributes.getPicture()))
                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }
}
