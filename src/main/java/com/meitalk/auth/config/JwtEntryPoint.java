package com.meitalk.auth.config;

import com.meitalk.auth.exception.ExceptionCode;
import com.meitalk.auth.model.ResponseBuilder;
import com.meitalk.auth.model.ResponseWithData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtEntryPoint implements AuthenticationEntryPoint {
    @SneakyThrows
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        int exception = (int) request.getAttribute("exception");
        if (exception == -100) {
            setResponse(response, ExceptionCode.UNKNOWN_ERROR);
        }
//        wrong type token
        else if (exception == -99) {
            setResponse(response, ExceptionCode.WRONG_TYPE_TOKEN);
        }
//        token denied
        else if (exception == -88) {
            setResponse(response, ExceptionCode.EXPIRED_TOKEN);
        }
//        unsupported token
        else if (exception == -77) {
            setResponse(response, ExceptionCode.UNSUPPORTED_TOKEN);
        }
    }

    private void setResponse(HttpServletResponse response, ExceptionCode exceptionCode) throws IOException, JSONException {
        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setStatus(HttpServletResponse.SC_OK);

        ResponseWithData responseWithData = ResponseWithData.builder()
                .data(null)
                .response(ResponseBuilder.builder()
                        .output(exceptionCode.getCode())
                        .result(exceptionCode.getMessage())
                        .build())
                .build();

        response.getWriter().print(new ObjectMapper().writeValueAsString(responseWithData));
    }
}
