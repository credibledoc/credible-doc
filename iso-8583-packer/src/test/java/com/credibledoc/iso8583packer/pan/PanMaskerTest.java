package com.credibledoc.iso8583packer.pan;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class PanMaskerTest {
    Logger logger = LoggerFactory.getLogger(PanMaskerTest.class);
    
    @Test
    public void testCreateField() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.VAL)
            .defineName("PAN_field")
            .defineLen(10)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineMasker(PanMasker.getInstance())
            .validateStructure();

        MsgField msgField = fieldBuilder.getCurrentField();
        
        ValueHolder valueHolder = ValueHolder.newInstance(msgField);
        String pan = "1234567890123456789";
        valueHolder.setValue(pan);

        MsgValue msgValue = valueHolder.getCurrentMsgValue();

        DumpService dumpService = DumpService.getInstance();
        String msgFieldStructure = dumpService.dumpMsgField(msgField);
        String msgValueStructure = dumpService.dumpMsgValue(msgField, msgValue, false);
        String maskedMsgValueStructure = dumpService.dumpMsgValue(msgField, msgValue, true);
        logger.info("MsgField structure dump: {}\nMsgValue without masking\n{}\nMasked MsgValue\n{}The end of the example",
            msgFieldStructure, msgValueStructure, maskedMsgValueStructure);
    }

    @Test
    public void maskHex() {
        PanMasker panMasker = PanMasker.getInstance();
        String masked = panMasker.maskHex("12345678");
        assertEquals("1234****", masked);
    }

    @Test
    public void maskValue() {
        PanMasker panMasker = PanMasker.getInstance();
        String masked = panMasker.maskValue("12345678");
        assertEquals("1234****", masked);
    }
}
