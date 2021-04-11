package com.credibledoc.iso8583packer.validator;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.navigator.NavigatorService;
import org.junit.Test;

public class ValidatorServiceTest {

    @Test(expected = PackerRuntimeException.class)
    public void validateFixedLengthType() {
        ValidatorService validatorService = new ValidatorService();
        NavigatorService navigatorService = NavigatorService.getInstance();
        validatorService.setNavigator(navigatorService);
        MsgField msgField = new MsgField();
        msgField.setType(MsgFieldType.LEN_VAL);
        msgField.setLen(1);
        validatorService.validateFixedLengthType(msgField);
    }
}
