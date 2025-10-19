package com.fzolv.shareware.hull.security;

import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OAuth2User oAuth2User)) {
            throw new AuthenticationServiceException("OAuth2 principal is missing");
        }
        String email = (String) oAuth2User.getAttributes().get("email");
        if (email == null) {
            throw new AuthenticationServiceException("Email not present in OAuth2 user attributes");
        }
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new AuthenticationServiceException("User not found for email: " + email);
        }

        String token = jwtService.issueToken(user, Duration.ofHours(8).toSeconds());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String body = "{\"token\":\"" + token + "\"}";
        response.getWriter().write(body);
        response.getWriter().flush();
    }
}


