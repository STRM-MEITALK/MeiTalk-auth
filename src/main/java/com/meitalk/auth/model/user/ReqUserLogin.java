package com.meitalk.auth.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReqUserLogin {

    private String mailId;
    private String userPw;

}
