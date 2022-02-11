package com.meitalk.auth.model.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Oauth2LoginUrl {

    private String provider;
    private String url;

    @Builder
    public Oauth2LoginUrl(String provider, String url) {
        this.provider = provider;
        this.url = url;
    }
}
