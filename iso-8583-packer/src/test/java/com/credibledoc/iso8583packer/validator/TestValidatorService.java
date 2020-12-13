package com.credibledoc.iso8583packer.validator;

import com.credibledoc.iso8583packer.message.MsgField;

public class TestValidatorService extends ValidatorService {
    private static final TestValidatorService instance = new TestValidatorService();

    public static Validator getInstance() {
        return instance;
    }

    @Override
    public void validateStructure(MsgField current) {
        // empty
    }
}
