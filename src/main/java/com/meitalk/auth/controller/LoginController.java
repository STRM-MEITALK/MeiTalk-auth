package com.meitalk.auth.controller;

import com.meitalk.auth.config.AppleUtil;
import com.meitalk.auth.config.JwtTokenProvider;
import com.meitalk.auth.mapper.UserMapper;
import com.meitalk.auth.model.ResponseWithData;
import com.meitalk.auth.model.apple.AppleUserInfo;
import com.meitalk.auth.model.channel.ReqChannel;
import com.meitalk.auth.model.user.CustomUserDetails;
import com.meitalk.auth.model.user.UserJoinDto;
import com.meitalk.auth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/login")
@RestController
public class LoginController {

    final private JwtTokenProvider jwtTokenProvider;
    final private UserService userService;
    final private UserMapper userMapper;
    final private AppleUtil appleUtil;
    private final RedisTemplate redisTemplate;

    @Value("${cookie.domain}")
    String domain;
    @Value("${oauth.redirect.uri}")
    String redirectUri;
    @Value("${meitalk.api.server}")
    String meitalkApiServer;
    @Value("${meitalk.api.server.create-channel-url}")
    String createChannelUrl;

    /**
     * apple login redirect hook
     *
     * @param user     : first login -> request user
     * @param code     : apple code
     * @param id_token : apple id token
     * @param response : redirect
     */
    @RequestMapping(value = "/oauth2/apple", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void oauth2AppleLoginRedirect(String user, String code, String id_token,
                                         HttpServletResponse response) throws IOException {
        log.info("apple login start");
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject data = appleUtil.decodeFromIdToken(id_token);
        if (user != null) {
            log.info("first apple login user : {}", user);
            AppleUserInfo appleUserInfo = objectMapper.readValue(user, AppleUserInfo.class);
            CustomUserDetails customUserDetails = userService.getUserByEmail(appleUserInfo.getEmail());
            log.info("apple login -> same email for not signup : {}", customUserDetails);
            if (customUserDetails == null) {
                UserJoinDto dto = UserJoinDto.builder()
                        .mailId(appleUserInfo.getEmail())
                        .userName(appleUserInfo.getName().getLastName() + appleUserInfo.getName().getFirstName())
                        .platform("apple")
                        .userPicture("https://streaming-fe.s3.ap-northeast-2.amazonaws.com/assets/sampleUserProfile.png")
                        .oauth2Key(data.getAsString("sub"))
                        .googleOauth2Key(null)
                        .facebookOauth2Key(null)
                        .appleOauth2Key(data.getAsString("sub"))
                        .emailVerification("Y")
                        .privacyAgree("Y")
                        .build();
                log.info("apple signup : {}", dto);
                userMapper.insertUserJoin(dto);
                ReqChannel.CreateInternalChannel createInternalChannel = ReqChannel.CreateInternalChannel.builder()
                        .userNo(dto.getId())
                        .build();
                WebClient webClient = WebClient.builder()
                        .baseUrl(meitalkApiServer)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();
                ResponseWithData responseWithData = webClient.post()
                        .uri(createChannelUrl)
                        .body(Mono.just(createInternalChannel), ReqChannel.CreateInternalChannel.class)
                        .retrieve()
                        .bodyToMono(ResponseWithData.class)
                        .block();
                if (responseWithData.getResponse().getOutput() != 0) {
                    log.error("apple signup -> channel create error");
                }
            }
        }
        log.info("oauth2 key update for apple. : {}, {}", data.getAsString("email"), data.getAsString("sub"));
        userMapper.updateAppleOauth2Key(data.getAsString("email"), data.getAsString("sub"));
        CustomUserDetails userDetails = userMapper.selectUserByOauth2Key(data.getAsString("sub"));
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

        log.info("apple login success : {}", userDetails);

        response.sendRedirect(redirectUri);
    }

}
