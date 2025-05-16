package com.capstone.kakas.apiPayload.exception.handler;


import com.capstone.kakas.apiPayload.code.BaseErrorCode;
import com.capstone.kakas.apiPayload.exception.GeneralException;

public class TempHandler extends GeneralException {

    public TempHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
