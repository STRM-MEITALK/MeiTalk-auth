package com.meitalk.auth.config.auth;

import com.meitalk.auth.config.JwtTokenProvider;
import com.meitalk.auth.mapper.UserMapper;
import com.meitalk.auth.model.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${oauth.redirect.uri}")
    String redirectUri;
    @Value("${cookie.domain}")
    String domain;

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    private final UserMapper userMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();
        CustomUserDetails userDetails = userMapper.selectUserByOauth2Key(authentication.getName());

        String accessToken = jwtTokenProvider.createToken(userDetails.getUserId(), userDetails.getUserEmail(), userDetails.getUsername(), userDetails.getRole());
        String refreshToken = "null";
        try {
            String redisRefreshToken = String.valueOf(redisTemplate.opsForValue().get(userDetails.getUserEmail()));
            if (redisRefreshToken.equals("null")) {
                refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getUserId(), userDetails.getUserEmail(), userDetails.getUsername(), userDetails.getRole());
                redisTemplate.opsForValue().set(userDetails.getUserEmail(), refreshToken, jwtTokenProvider.refreshTokenValidTime, TimeUnit.MILLISECONDS);
            } else {
                refreshToken = redisRefreshToken;
            }
        } catch (Exception e) {
            log.error("redis server error. check redis server : {},", user);
        }
//        header set
        ResponseCookie accessTokenCookie = ResponseCookie.from("access-token", accessToken)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(false)
                .domain(domain)
                .build();
        ResponseCookie refrshTokenCookie = ResponseCookie.from("refresh-token", refreshToken)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(false)
                .domain(domain)
                .build();

        response.setHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refrshTokenCookie.toString());

        log.info("OAuth2 login success : {}", userDetails);

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

}
