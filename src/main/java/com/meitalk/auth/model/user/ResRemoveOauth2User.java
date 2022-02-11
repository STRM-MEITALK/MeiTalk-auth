package com.meitalk.auth.model.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResRemoveOauth2User {

    private String url;
    private String confirmation_code;

    @Builder
    public ResRemoveOauth2User(String url, String confirmation_code) {
        this.url = url;
        this.confirmation_code = confirmation_code;
    }
}
