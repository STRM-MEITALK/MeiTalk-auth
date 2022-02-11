package com.meitalk.auth.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReqUserJoin {

    private String mailId;
    private String userPw;
    private String userName;
    private String countryPhone;
    private String phoneNum;
//    private String ipAddr;
//    private String platform;
//    private String role;
    private String privacyAgree;

}
