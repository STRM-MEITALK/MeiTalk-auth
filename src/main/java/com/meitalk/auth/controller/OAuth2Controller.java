package com.meitalk.auth.controller;

import com.meitalk.auth.model.ResponseBuilder;
import com.meitalk.auth.model.ResponseWithData;
import com.meitalk.auth.model.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuth2Controller {

    /**
     * after OAuth2 login -> get user info for access-token
     *
     * @param userDetails : @AuthenticationPrincipal
     * @return ResponseWithData
     */
    @GetMapping("/user")
    public ResponseWithData<?> getUserInfoByAccessToken(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("OAuth2 login -> get user : {}", userDetails);
        return ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(0)
                        .result("success")
                        .build())
                .data(userDetails)
                .build();
    }

}
