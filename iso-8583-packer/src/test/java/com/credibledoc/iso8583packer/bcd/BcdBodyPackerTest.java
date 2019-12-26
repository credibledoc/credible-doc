package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.FieldFiller;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BcdBodyPackerTest {

    private FieldBuilder fixedLengthBcd() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.LEFT_PADDED_0);
    }

    @Test
    public void pack() {
        String value = "123";

        FieldBuilder fieldBuilder = fixedLengthBcd();

        fieldBuilder.validateStructure();
        
        FieldFiller fieldFiller = FieldFiller.from(fieldBuilder.getCurrentField())
            .setValue(value);
        
        fieldFiller.validateData();

        byte[] valueBytes = fieldFiller.pack();
        assertEquals("0123", HexService.hexString(valueBytes));
    }

    @Test
    public void unpack() {
        String packedValue = "0123";

        FieldBuilder fieldBuilder = fixedLengthBcd();

        fieldBuilder.validateStructure();
        
        MsgValue msgValue = FieldFiller.from(fieldBuilder.getCurrentField())
            .unpack(HexService.hex2byte(packedValue));

        String expectedValue = "123";
        String unpackedValue = (String) msgValue.getBodyValue();
        assertEquals(expectedValue, unpackedValue);
    }
}
