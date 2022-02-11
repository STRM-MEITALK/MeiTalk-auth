package com.meitalk.auth.model.apple;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Key {

    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;

}
