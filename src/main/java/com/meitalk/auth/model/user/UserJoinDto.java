package com.meitalk.auth.model.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserJoinDto {

    private Long id;
    private String mailId;
    private String userPw;
    private String userName;
    private String countryPhone;
    private String phoneNum;
    private String ipAddr;
    //    private String role;
    private String platform;
    private String privacyAgree;
    private String userPicture;
    private String emailVerification;
    private String oauth2Key;
    private String googleOauth2Key;
    private String facebookOauth2Key;
    private String appleOauth2Key;

    @Builder
    public UserJoinDto(String mailId, String userPw, String userName, String countryPhone, String phoneNum, String ipAddr, String platform, String privacyAgree, String userPicture, String emailVerification, String oauth2Key, String googleOauth2Key, String facebookOauth2Key, String appleOauth2Key) {
        this.mailId = mailId;
        this.userPw = userPw;
        this.userName = userName;
        this.countryPhone = countryPhone;
        this.phoneNum = phoneNum;
        this.ipAddr = ipAddr;
        this.platform = platform;
        this.privacyAgree = privacyAgree;
        this.userPicture = userPicture;
        this.emailVerification = emailVerification;
        this.oauth2Key = oauth2Key;
        this.googleOauth2Key = googleOauth2Key;
        this.facebookOauth2Key = facebookOauth2Key;
        this.appleOauth2Key = appleOauth2Key;
    }
}
