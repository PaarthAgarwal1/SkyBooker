package com.skybooker.AuthService.exception;

public class InvalidRegistrationKeyException extends RuntimeException{
    public InvalidRegistrationKeyException(String msg){
        super(msg);
    }
}
