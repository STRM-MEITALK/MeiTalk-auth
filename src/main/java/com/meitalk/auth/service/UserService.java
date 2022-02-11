package com.meitalk.auth.service;

import com.meitalk.auth.mapper.UserMapper;
import com.meitalk.auth.model.ResponseBuilder;
import com.meitalk.auth.model.ResponseWithData;
import com.meitalk.auth.model.channel.ReqChannel;
import com.meitalk.auth.model.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${meitalk.api.server}")
    String meitalkApiServer;
    @Value("${meitalk.api.server.create-channel-url}")
    String createChannelUrl;

    public CustomUserDetails getUserByEmail(String mailId) {
        CustomUserDetails user = userMapper.selectUserByEmail(mailId);
        return user;
    }

    public boolean userPasswordMatch(String reqPassword, String userPassword) {
        return passwordEncoder.matches(reqPassword, userPassword);
    }

    //    @Transactional(rollbackFor = Exception.class)
    public ResponseWithData<?> joinUser(ReqUserJoin req, String ip) {
        UserJoinDto dto = UserJoinDto.builder()
                .mailId(req.getMailId())
                .userName(req.getUserName())
                .userPw(passwordEncoder.encode(req.getUserPw()))
                .countryPhone(req.getCountryPhone())
                .phoneNum(req.getPhoneNum())
                .ipAddr(ip)
                .platform("web")
                .privacyAgree(req.getPrivacyAgree())
                .userPicture("https://streaming-fe.s3.ap-northeast-2.amazonaws.com/assets/sampleUserProfile.png")
                .oauth2Key("web")
                .googleOauth2Key(null)
                .facebookOauth2Key(null)
                .appleOauth2Key(null)
                .emailVerification("N")
                .build();
        int success = userMapper.insertUserJoin(dto);
        if (success != 1) {
            return ResponseWithData.builder()
                    .response(ResponseBuilder.builder()
                            .output(-2)
                            .result("database insert fail")
                            .build())
                    .data(null)
                    .build();
        }
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
            return ResponseWithData.builder()
                    .response(ResponseBuilder.builder()
                            .result("database insert fail")
                            .output(-2)
                            .build())
                    .data(null)
                    .build();
        }
        log.info("signup success : {}", dto);
        return ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(0)
                        .result("success")
                        .build())
                .data(null)
                .build();
    }

    public int getEmailVerificationByUserNo(String userNo) {
        ResEmailVerification res = userMapper.selectEmailVerificationByUserNoByFlag(userNo, "N");
        if (res.getVerification().equalsIgnoreCase("N")) {
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime createTime = LocalDateTime.parse(res.getCreateTime(), inputFormat);
            LocalDateTime expiredTime = createTime.plusDays(1);
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(expiredTime)) {
                return -1;
            } else {
                return -2;
            }
        }
        return 0;
    }

    public ResponseWithData<?> setRoleStreamer(ReqSetRoleByUserMail req) {
        log.info("Role update : USER to STREAMER : {}", req.getUserMail());
        CustomUserDetails userDetails = userMapper.selectUserByEmail(req.getUserMail());
        if (userDetails == null) {
            log.info("not found user : {}", req.getUserMail());
            return ResponseWithData.builder()
                    .data(null)
                    .response(ResponseBuilder.builder()
                            .output(-1)
                            .result("not found user")
                            .build())
                    .build();
        }
        int updateSuccess = userMapper.updateRole(req.getUserMail(), Role.STREAMER.getRole());
        if (updateSuccess != 1) {
            log.error("database error : {}", req.getUserMail());
            return ResponseWithData.builder()
                    .data(null)
                    .response(ResponseBuilder.builder()
                            .output(-2)
                            .result("database error")
                            .build())
                    .build();
        }
        return ResponseWithData.builder()
                .data(req.getUserMail())
                .response(ResponseBuilder.builder()
                        .output(0)
                        .result("success")
                        .build())
                .build();
    }
}
