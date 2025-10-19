package com.fzolv.shareware.hull.security;

import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);

        String email = (String) oauthUser.getAttributes().get("email");
        if (email == null) {
            return oauthUser;
        }

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getRole() == null) {
            return oauthUser;
        }

        String roleName = "ROLE_" + user.getRole().name();
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(roleName)),
                oauthUser.getAttributes(),
                "email"
        );
    }
}


