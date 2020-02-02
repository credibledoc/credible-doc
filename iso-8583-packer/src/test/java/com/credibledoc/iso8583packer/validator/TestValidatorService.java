package com.credibledoc.iso8583packer.validator;

import com.credibledoc.iso8583packer.message.MsgField;

public class TestValidatorService extends ValidatorService {
    private static TestValidatorService instance;

    public static Validator getInstance() {
        if (instance == null) {
            instance = new TestValidatorService();
        }
        return instance;
    }

    @Override
    public void validateStructure(MsgField current) {
        // empty
    }
}
