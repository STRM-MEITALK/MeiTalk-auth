package com.meitalk.auth.controller;

import com.meitalk.auth.model.user.ResRemoveOauth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Slf4j
public class MainController {

    @Value("${base.uri}")
    private String baseUri;

    /**
     * facebook remove user hook
     *
     * @param httpServletRequest
     * @param id
     * @return ResRemoveOauth2User
     */
    @GetMapping("/remove")
    public ResRemoveOauth2User removeOauth2User(HttpServletRequest httpServletRequest, @RequestParam("id") String id) {
        log.info(httpServletRequest.getRemoteAddr());
        log.info(baseUri);
        return ResRemoveOauth2User.builder()
                .url(baseUri + "/oauth2/remove?id=" + id)
                .confirmation_code("0")
                .build();
    }

}
