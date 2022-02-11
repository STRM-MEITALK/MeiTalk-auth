package com.meitalk.auth.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ResponseWithData<T> {

    private ResponseBuilder response;
    private T data;

    @Builder
    public ResponseWithData(ResponseBuilder response, T data) {
        this.response = response;
        this.data = data;
    }
}
