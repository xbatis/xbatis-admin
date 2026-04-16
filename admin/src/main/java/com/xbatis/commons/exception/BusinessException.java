package com.xbatis.commons.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final int code;

    public BusinessException(String message) {
        this(HttpStatus.BAD_REQUEST, 4000, message);
    }

    public BusinessException(HttpStatus status, int code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
