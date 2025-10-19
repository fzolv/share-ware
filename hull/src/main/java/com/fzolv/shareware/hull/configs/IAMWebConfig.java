package com.fzolv.shareware.hull.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j
@Configuration
public class IAMWebConfig implements WebMvcConfigurer {

    @Value("#{'${allowed.origins:*}'.split(',')}")
    List<String> allowedOrigins;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("addCorsMappings");
        final String[] arrOrigin = allowedOrigins.toArray(new String[0]);
        log.info("addCorsMappings() : configuring CORS for origins[" + allowedOrigins + "]");
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowCredentials(true)
                .allowedOrigins(arrOrigin);
    }
}
