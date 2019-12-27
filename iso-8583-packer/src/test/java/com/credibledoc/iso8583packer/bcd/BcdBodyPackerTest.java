package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.FieldFiller;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BcdBodyPackerTest {

    /**
     * Used in documentation
     */
    private FieldBuilder fixedLengthBcd() {
        return
            FieldBuilder.builder(MsgFieldType.VAL)
                .defineLen(2)
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
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
            FieldFiller.newInstance(fieldBuilder.getCurrentField())
            .setValue(value);
        
        fieldFiller.validateData();

        byte[] valueBytes = fieldFiller.pack();
        String bytesHex = HexService.bytesToHex(valueBytes);
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
            FieldFiller.newInstance(fieldBuilder.getCurrentField())
            .unpack(packedBytes);
        String unpackedValue = (String) msgValue.getBodyValue();

        String expectedValue = "456";
        assertEquals(expectedValue, unpackedValue);
    }

    @Test
    public void testPack() {
        byte[] bytes = new byte[2];
        BcdBodyPacker.noPadding().pack("1234", bytes, 0);
        assertEquals("1234", HexService.bytesToHex(bytes));

        try {
            byte[] bytes2 = new byte[2];
            BcdBodyPacker.noPadding().pack("123", bytes2, 0);
            assertNull("Should not be reached, exception expected.", HexService.bytesToHex(bytes2));
        } catch (Exception e) {
            assertEquals(PackerRuntimeException.class, e.getClass());
        }

        byte[] bytes3 = new byte[2];
        BcdBodyPacker.leftPaddingF().pack("1234", bytes3, 0);
        assertEquals("1234", HexService.bytesToHex(bytes3));

        byte[] bytes4 = new byte[2];
        BcdBodyPacker.leftPaddingF().pack("123", bytes4, 0);
        assertEquals("F123", HexService.bytesToHex(bytes4));

        byte[] bytes5 = new byte[2];
        BcdBodyPacker.rightPaddingF().pack("1234", bytes5, 0);
        assertEquals("1234", HexService.bytesToHex(bytes5));

        byte[] bytes6 = new byte[2];
        BcdBodyPacker.rightPaddingF().pack("123", bytes6, 0);
        assertEquals("123F", HexService.bytesToHex(bytes6));

        byte[] bytes7 = new byte[2];
        BcdBodyPacker.leftPadding0().pack("1234", bytes7, 0);
        assertEquals("1234", HexService.bytesToHex(bytes7));

        byte[] bytes8 = new byte[2];
        BcdBodyPacker.leftPadding0().pack("123", bytes8, 0);
        assertEquals("0123", HexService.bytesToHex(bytes8));
    }

    @Test
    public void testUnpack() {
        byte[] bytes1234 = HexService.hex2byte("1234");
        byte[] bytes0123 = HexService.hex2byte("0123");
        byte[] bytesLeftF = HexService.hex2byte("F123");
        byte[] bytesRightF = HexService.hex2byte("123F");
 
        String result1 = BcdBodyPacker.noPadding().unpack(bytes1234, 0, 2);
        assertEquals("1234", result1);
        
        String result2 = BcdBodyPacker.leftPadding0().unpack(bytes0123, 0, 2);
        assertEquals("123", result2);
        
        String result3 = BcdBodyPacker.leftPaddingF().unpack(bytesLeftF, 0, 2);
        assertEquals("123", result3);
        
        String result4 = BcdBodyPacker.rightPaddingF().unpack(bytesRightF, 0, 2);
        assertEquals("123", result4);
        
        String result5 = BcdBodyPacker.leftPadding0().unpack(bytes1234, 0, 2);
        assertEquals("1234", result5);
        
        String result6 = BcdBodyPacker.leftPaddingF().unpack(bytes1234, 0, 2);
        assertEquals("1234", result6);
        
        String result7 = BcdBodyPacker.rightPaddingF().unpack(bytes1234, 0, 2);
        assertEquals("1234", result7);
    }
}
