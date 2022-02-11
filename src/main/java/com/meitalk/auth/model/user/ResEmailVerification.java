package com.meitalk.auth.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResEmailVerification {

    private String id;
    private String userNo;
    private String userKey;
    private String verification;
    private String createTime;

}
