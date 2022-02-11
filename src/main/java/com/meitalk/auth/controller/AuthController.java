package com.meitalk.auth.controller;


import com.meitalk.auth.config.JwtTokenProvider;
import com.meitalk.auth.exception.*;
import com.meitalk.auth.model.ResponseBuilder;
import com.meitalk.auth.model.ResponseWithData;
import com.meitalk.auth.model.user.*;
import com.meitalk.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    @Value("${base.uri}")
    private String baseUri;
    @Value("${oauth2.login.uri}")
    private String oauthLoginUri;
    @Value("${apple.auth.url}")
    private String appleAuthUrl;
    @Value("${apple.client.id}")
    private String appleClientId;
    @Value("${apple.redirect.url}")
    private String appleRedirectUrl;
    @Value("${cookie.domain}")
    String domain;

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;

    /**
     * signup
     *
     * @param req     : user info
     * @param request : remote addr
     * @return ResponseWithData
     */
    @PostMapping("/join")
    public ResponseWithData<?> join(@RequestBody ReqUserJoin req, HttpServletRequest request) {
        CustomUserDetails user = userService.getUserByEmail(req.getMailId());
        if (user != null) {
            log.info("already use email : {}", user);
            return ResponseWithData.builder()
                    .data(null)
                    .response(ResponseBuilder.builder()
                            .output(-1)
                            .result("already join user")
                            .build())
                    .build();
        }
        return userService.joinUser(req, request.getRemoteAddr());
    }

    /**
     * signin
     *
     * @param req      : user info
     * @param response : use cookie
     * @return ResponseWithData
     */
    @PostMapping("/login")
    public ResponseWithData<?> login(@RequestBody ReqUserLogin req, HttpServletResponse response) {
        CustomUserDetails user = userService.getUserByEmail(req.getMailId());
        if (user == null) {
            log.info("not found user mail : {}", req.getMailId());
            return ResponseWithData.builder()
                    .response(ResponseBuilder.builder()
                            .output(-1)
                            .result("not found user")
                            .build())
                    .data(null)
                    .build();
        }
        if (!userService.userPasswordMatch(req.getUserPw(), user.getPassword())) {
            log.info("not match password : {}", req.getMailId());
            return ResponseWithData.builder()
                    .response(ResponseBuilder.builder()
                            .output(-2)
                            .result("password is fail")
                            .build())
                    .data(null)
                    .build();
        }
        if (user.getBlock().equalsIgnoreCase("Y")) {
            log.info("block user : {}", req.getMailId());
            return ResponseWithData.builder()
                    .response(ResponseBuilder.builder()
                            .output(-3)
                            .result("block user")
                            .build())
                    .data(null)
                    .build();
        }
        if (user.getEmailVerification().equalsIgnoreCase("N")) {
            log.info("email verification not yet : {}", req.getMailId());
            int emailVerification = userService.getEmailVerificationByUserNo(user.getUserId());
            if (emailVerification == -1) {
                log.info("email verification mail expired. must resend mail. : {}", req.getMailId());
                return ResponseWithData.builder()
                        .response(ResponseBuilder.builder()
                                .output(-4)
                                .result("email verification expired")
                                .build())
                        .data(user)
                        .build();
            } else if (emailVerification == -2) {
                log.info("must email verification. : {}", req.getMailId());
                return ResponseWithData.builder()
                        .response(ResponseBuilder.builder()
                                .output(-5)
                                .result("email verification is N")
                                .build())
                        .data(user)
                        .build();
            } else {
                log.error("database error. : {}", req.getMailId());
                return ResponseWithData.builder()
                        .response(ResponseBuilder.builder()
                                .output(-6)
                                .result("DB error ! check db")
                                .build())
                        .data(user)
                        .build();
            }
        }
        String accessToken = jwtTokenProvider.createToken(user.getUserId(), user.getUserEmail(), user.getUsername(), user.getAuthorities().toArray()[0].toString());
        String refreshToken = "null";
        try {
            String redisRefreshToken = String.valueOf(redisTemplate.opsForValue().get(user.getUserEmail()));
            if (redisRefreshToken.equals("null")) {
                refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(), user.getUserEmail(), user.getUsername(), user.getAuthorities().toArray()[0].toString());
                redisTemplate.opsForValue().set(user.getUserEmail(), refreshToken, jwtTokenProvider.refreshTokenValidTime, TimeUnit.MILLISECONDS);
            } else {
                refreshToken = redisRefreshToken;
            }
        } catch (Exception e) {
            log.error("redis server error. check redis server : {},", user);
            return ResponseWithData.builder()
                    .response(ResponseBuilder.builder()
                            .output(-7)
                            .result("redis exception. please check redis server.")
                            .build())
                    .data(null)
                    .build();
        }
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
        log.info("login success : {}", user);
        return ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(0)
                        .result("success")
                        .build())
                .data(user)
                .build();
    }

    /**
     * access token reissued
     *
     * @param req      : refresh token
     * @param response : use cookie
     * @return ResponseWithData
     */
    @PostMapping("/access-token/issued")
    public ResponseWithData<?> accessTokenIssued(@RequestBody ReqAccessTokenIssued req,
                                                 HttpServletResponse response) throws ExpireTokenException, UnSupportedTokenException, WrongTokenException, WrongTypeTokenException, UnknownErrorException {
        Authentication refreshTokenAuthentication = jwtTokenProvider.getAuthentication(req.getRefreshToken());
        CustomUserDetails tokenUserDetails = (CustomUserDetails) refreshTokenAuthentication.getPrincipal();
        String redisRefreshTokenValue = String.valueOf(redisTemplate.opsForValue().get(tokenUserDetails.getUserEmail()));
        if (req.getRefreshToken().equals(redisRefreshTokenValue)) {
            log.info("access token reissued success : {}", tokenUserDetails);
            String accessToken = jwtTokenProvider.createToken(tokenUserDetails.getUserId(), tokenUserDetails.getUserEmail(), tokenUserDetails.getUsername(), tokenUserDetails.getRole());
            return ResponseWithData.builder()
                    .response(ResponseBuilder.builder()
                            .output(0)
                            .result("success")
                            .build())
                    .data(accessToken)
                    .build();
        }
        log.info("access token reissued fail. : {}", tokenUserDetails);
        return ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(-1)
                        .result("refresh token not same")
                        .build())
                .data(null)
                .build();
    }

    /**
     * refresh token reissued
     * for streamer
     *
     * @param req
     * @return ResponseWithData
     */
    @PostMapping("/refresh-token/issued")
    public ResponseWithData<?> refreshTokenIssued(@RequestBody ReqRefreshTokenIssued req) throws ExpireTokenException, WrongTokenException, UnSupportedTokenException, WrongTypeTokenException, UnknownErrorException {
        Authentication refreshTokenAuthentication = jwtTokenProvider.getAuthentication(req.getRefreshToken());
        CustomUserDetails tokenUserDetails = (CustomUserDetails) refreshTokenAuthentication.getPrincipal();
        String redisRefreshTokenValue = String.valueOf(redisTemplate.opsForValue().get(tokenUserDetails.getUserEmail()));
        if (req.getRefreshToken().equals(redisRefreshTokenValue)) {
            log.info("refresh token reissued success : {}", tokenUserDetails);
            String refreshToken = jwtTokenProvider.createRefreshToken(tokenUserDetails.getUserId(), tokenUserDetails.getUserEmail(), tokenUserDetails.getUsername(), tokenUserDetails.getRole());
            redisTemplate.opsForValue().set(tokenUserDetails.getUserEmail(), refreshToken, jwtTokenProvider.refreshTokenValidTime, TimeUnit.MILLISECONDS);
            return ResponseWithData.builder()
                    .response(ResponseBuilder.builder()
                            .output(0)
                            .result("success")
                            .build())
                    .data(refreshToken)
                    .build();
        }
        log.info("refresh token reissued fail : {}", tokenUserDetails);
        return ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(-1)
                        .result("refresh token not same")
                        .build())
                .data(null)
                .build();
    }

    /**
     * OAuth login url
     *
     * @param provider : google, facebook, apple
     * @return ResponseWithData
     */
    @GetMapping("/oauth2/{provider}")
    public ResponseWithData<?> getOauth2ProviderLoginUrl(@PathVariable String provider) {
        if (provider.equalsIgnoreCase("apple")) {
            String reqUrl = appleAuthUrl + "/auth/authorize?client_id="
                    + appleClientId + "&redirect_uri="
                    + appleRedirectUrl + "&response_type=code id_token&response_mode=form_post&scope=email name";
            return ResponseWithData.builder()
                    .response(ResponseBuilder.builder()
                            .output(0)
                            .result("success")
                            .build())
                    .data(reqUrl)
                    .build();
        }
        return ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(0)
                        .result("success")
                        .build())
                .data(baseUri + oauthLoginUri + provider)
                .build();
    }

    /**
     * Role Streamer set
     *
     * @param req : user email
     * @return ResponseWithData
     */
    @PostMapping("/streamer")
    public ResponseWithData<?> setRoleStreamer(@RequestBody ReqSetRoleByUserMail req) {
        return userService.setRoleStreamer(req);
    }

}
