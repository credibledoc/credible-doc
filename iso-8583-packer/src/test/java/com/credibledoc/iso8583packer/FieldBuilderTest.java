package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldBuilderTest {

    private static final String PAN_02_NAME = "PAN_02";

    @Test
    public void builder() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.BIT_SET)
            .defineName("root")
            .defineHeaderBitMapPacker(IfbBitmapPacker.L8);
        
        MsgField root = fieldBuilder.getCurrentField();
        
        FieldBuilder.from(fieldBuilder.getCurrentField())
            .createChild(MsgFieldType.LEN_VAL_BIT_SET)
            .defineTagNum(2)
            .defineName(PAN_02_NAME)
            .defineBodyPacker(BcdBodyPacker.RIGHT_PADDED_F)
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.LL);
        
        fieldBuilder.validateStructure();

        String pan = "123456781234567";
        FieldFiller fieldFiller = FieldFiller.from(fieldBuilder.getCurrentField())
            .jumpToChild(PAN_02_NAME).setValue(pan);
        
        byte[] bytes = fieldFiller.jumpToRoot().pack();
        String expectedBitmapHex = "4000000000000000";
        String expectedLengthHex = "F0F8";
        String padding = "F";
        String expectedHex = expectedBitmapHex + expectedLengthHex + pan + padding;
        assertEquals(expectedHex, HexService.hexString(bytes));
        
        // unpacking
        MsgValue msgValue = FieldFiller.unpack(bytes, 0, root);
        assertEquals(1, msgValue.getChildren().size());
        assertEquals(pan, msgValue.getChildren().get(0).getBodyValue());
    }
}
