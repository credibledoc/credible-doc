package com.credibledoc.iso8583packer.builder;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.validator.TestValidatorService;

public class TestFieldBuilder extends FieldBuilder {
    
    @Override
    protected void createDefaultServices() {
        super.createDefaultServices();
        super.validator = TestValidatorService.getInstance();
    }

    public static FieldBuilder builder(MsgFieldType msgFieldType) {
        TestFieldBuilder testFieldBuilder = new TestFieldBuilder();
        testFieldBuilder.msgField = new MsgField();
        testFieldBuilder.msgField.setType(msgFieldType);

        // Technical domain
        testFieldBuilder.createDefaultServices();

        return testFieldBuilder;
    }
}
