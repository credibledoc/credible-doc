package com.credibledoc.iso8583packer.validator;

import com.credibledoc.iso8583packer.message.MsgField;

public class TestValidator extends ValidatorService {
    private static TestValidator instance;

    public static Validator getInstance() {
        if (instance == null) {
            instance = new TestValidator();
        }
        return instance;
    }

    @Override
    public void validateStructure(MsgField current) {
        // empty
    }
}
