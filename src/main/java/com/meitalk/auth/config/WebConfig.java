package com.meitalk.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:[*]",
                        "https://localhost:[*]",
//                        front local
                        "http://local.meitalk.club:[*]",
                        "https://local.meitalk.club:[*]",
                        "https://local.meitalk.club",
//                        front dev
                        "https://strweb.meitalk.club",
                        "http://strweb.meitalk.club:[*]",
//                        apple login
                        "https://appleid.apple.com",
//                        prod server
                        "https://web.glornd.com"
                )
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("access-token", "refresh-token", "Set-Cookie")
                .allowCredentials(true);
    }
}
