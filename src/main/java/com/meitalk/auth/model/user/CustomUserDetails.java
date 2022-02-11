package com.meitalk.auth.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@ToString
public class CustomUserDetails implements UserDetails {

    private String userId;
    private String mailId;
    private String userName;
    private String userPw;
    private String countryPhone;
    private String phoneNum;
    private String ipAddr;
    private String createTime;
    private String updateTime;
    private String role;
    private String block;
    private String platform;
    private String privacyAgree;
    private String userPicture;
    private String oauth2Key;
    private String googleOauth2Key;
    private String facebookOauth2Key;
    private String appleOauth2Key;
    private String emailVerification;
    private Long channelId;
    private Collection<SimpleGrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.role));
        return authorities;
    }

    public String getUserEmail(){
        return mailId;
    }

    @Override
    public String getPassword() {
        return userPw;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
