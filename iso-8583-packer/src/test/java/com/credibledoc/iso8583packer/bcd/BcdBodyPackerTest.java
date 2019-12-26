package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.FieldFiller;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BcdBodyPackerTest {

    /**
     * Used in documentation
     */
    private FieldBuilder fixedLengthBcd() {
        return
            FieldBuilder.builder(MsgFieldType.VAL)
                .defineLen(2)
                .defineBodyPacker(BcdBodyPacker.LEFT_PADDED_0);
    }

    /**
     * Used in documentation
     */
    @Test
    public void pack() {
        FieldBuilder fieldBuilder = fixedLengthBcd();
        fieldBuilder.validateStructure();

        String value = "123";
        FieldFiller fieldFiller =
            FieldFiller.from(fieldBuilder.getCurrentField())
            .setValue(value);
        
        fieldFiller.validateData();

        byte[] valueBytes = fieldFiller.pack();
        String bytesHex = HexService.hexString(valueBytes);
        assertEquals("0123", bytesHex);
    }

    /**
     * Used in documentation
     */
    @Test
    public void unpack() {
        FieldBuilder fieldBuilder = fixedLengthBcd();

        fieldBuilder.validateStructure();

        String packedHex = "0456";
        byte[] packedBytes = HexService.hex2byte(packedHex);
        
        MsgValue msgValue =
            FieldFiller.from(fieldBuilder.getCurrentField())
            .unpack(packedBytes);
        String unpackedValue = (String) msgValue.getBodyValue();

        String expectedValue = "456";
        assertEquals(expectedValue, unpackedValue);
    }
}
