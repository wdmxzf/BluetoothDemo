package com.zhg.bluetooth.entity;



public class ErrorModel {

    private int errorCode;

    private String errorMessage;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode( int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
