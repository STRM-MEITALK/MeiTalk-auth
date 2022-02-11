package com.meitalk.auth.model.channel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class ReqChannel {

    @Getter
    @Setter
    @ToString
    @Builder
    public static class CreateInternalChannel {
        private Long userNo;
    }

}
