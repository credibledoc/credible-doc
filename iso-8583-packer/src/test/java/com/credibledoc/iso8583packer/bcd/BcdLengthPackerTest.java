package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.FieldFiller;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class BcdLengthPackerTest {
    Logger logger = LoggerFactory.getLogger(BcdLengthPackerTest.class);

    private FieldBuilder createField() {
        return
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(BcdLengthPacker.getInstance(2))
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
    }

    /**
     * Used in documentation
     */
    @Test
    public void packUnpack() {
        String value = "123";
        FieldFiller fieldFiller = FieldFiller.newInstance(createField().getCurrentField());
        fieldFiller.setValue(value);
        assertEquals(value, fieldFiller.getValue(String.class));

        byte[] bytes = fieldFiller.pack();
        String lengthHex = "0002";
        String valueHex = "0123";
        assertEquals(lengthHex + valueHex, HexService.bytesToHex(bytes));

        MsgValue msgValue = FieldFiller.unpack(bytes, 0, createField().getCurrentField());
        assertEquals(value, msgValue.getBodyValue(String.class));
        
        String msgFieldDump = "MsgField structure dump: " + DumpService.dumpMsgField(createField().getCurrentField());
        logger.info(msgFieldDump);
        
        String msgValueDump = DumpService.dumpMsgValue(createField().getCurrentField(), msgValue, false);
        logger.info("MsgValue structure dump: {}", msgValueDump);
    }

    @Test
    public void testPack() {
        BcdLengthPacker bcdLengthPacker = BcdLengthPacker.getInstance(1);
        // lenLength 9999 should not be used there
        byte[] bytes = bcdLengthPacker.pack(12, 9999);
        assertEquals("12", HexService.bytesToHex(bytes));
    }

    @Test
    public void testUnpack() {
        BcdLengthPacker bcdLengthPacker = BcdLengthPacker.getInstance(2);
        byte[] bytes = HexService.hex2byte("0123");
        // lenLength 9999 should not be used there
        assertEquals(123, bcdLengthPacker.unpack(bytes, 0, 9999));
    }
}
