package com.meitalk.auth.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReqAccessTokenIssued {

    private String refreshToken;

}
