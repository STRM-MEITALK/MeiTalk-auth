package com.meitalk.auth.customException;

import com.meitalk.auth.exception.*;
import com.meitalk.auth.model.ResponseBuilder;
import com.meitalk.auth.model.ResponseWithData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(value = ExpireTokenException.class)
    public ResponseEntity<ResponseWithData> handleExpireAccessTokenException(ExpireTokenException e) {
        String msg = e.getMessage();
        log.warn(msg);
        return new ResponseEntity<>(ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(ExceptionCode.EXPIRED_TOKEN.getCode())
                        .result(ExceptionCode.EXPIRED_TOKEN.getMessage())
                        .build())
                .data(null)
                .build(),
                HttpStatus.OK);
    }

    @ExceptionHandler(value = UnSupportedTokenException.class)
    public ResponseEntity<ResponseWithData> handleUnSupportedTokenException(UnSupportedTokenException e) {
        String msg = e.getMessage();
        return new ResponseEntity<>(ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(ExceptionCode.UNSUPPORTED_TOKEN.getCode())
                        .result(ExceptionCode.UNSUPPORTED_TOKEN.getMessage())
                        .build())
                .data(null)
                .build(),
                HttpStatus.OK);
    }

    @ExceptionHandler(value = WrongTokenException.class)
    public ResponseEntity<ResponseWithData> handleWrongTokenException(WrongTokenException e) {
        String msg = e.getMessage();
        return new ResponseEntity<>(ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(ExceptionCode.WRONG_TOKEN.getCode())
                        .result(ExceptionCode.WRONG_TOKEN.getMessage())
                        .build())
                .data(null)
                .build(),
                HttpStatus.OK);
    }

    @ExceptionHandler(value = WrongTypeTokenException.class)
    public ResponseEntity<ResponseWithData> handleWrongTypeTokenException(WrongTypeTokenException e) {
        String msg = e.getMessage();
        log.warn(msg);
        return new ResponseEntity<>(ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(ExceptionCode.WRONG_TYPE_TOKEN.getCode())
                        .result(ExceptionCode.WRONG_TYPE_TOKEN.getMessage())
                        .build())
                .data(null)
                .build(),
                HttpStatus.OK);
    }

    @ExceptionHandler(value = UnknownErrorException.class)
    public ResponseEntity<ResponseWithData> handleUnknownErrorException(UnknownErrorException e) {
        String msg = e.getMessage();
        log.warn(msg);
        return new ResponseEntity<>(ResponseWithData.builder()
                .response(ResponseBuilder.builder()
                        .output(ExceptionCode.UNKNOWN_ERROR.getCode())
                        .result(ExceptionCode.UNKNOWN_ERROR.getMessage())
                        .build())
                .data(null)
                .build(),
                HttpStatus.OK);
    }

}
