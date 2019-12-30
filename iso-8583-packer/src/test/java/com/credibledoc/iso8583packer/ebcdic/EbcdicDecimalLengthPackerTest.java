package com.credibledoc.iso8583packer.ebcdic;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.FieldFiller;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EbcdicDecimalLengthPackerTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    Logger logger = LoggerFactory.getLogger(EbcdicDecimalLengthPackerTest.class);

    private FieldBuilder createField() {
        return
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2))
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

        byte[] bytes = fieldFiller.pack();
        String lengthHex = "F0F2";
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
        EbcdicDecimalLengthPacker ebcdicDecimalLengthPacker = EbcdicDecimalLengthPacker.getInstance(2);
        // lenLength 9999 should not be used there
        byte[] bytes = ebcdicDecimalLengthPacker.pack(12, 9999);
        assertEquals("F1F2", HexService.bytesToHex(bytes));
    }

    @Test
    public void testUnpack() {
        EbcdicDecimalLengthPacker ebcdicDecimalLengthPacker = EbcdicDecimalLengthPacker.getInstance(3);
        byte[] bytes = HexService.hex2byte("F1F2F3");
        // lenLength 9999 should not be used there
        assertEquals(123, ebcdicDecimalLengthPacker.unpack(bytes, 0, 9999));
    }

    /**
     * Used in documentation
     */
    @Test
    public void packedExamples() {
        StringBuilder stringBuilder = new StringBuilder("Examples of integers packed with " +
            EbcdicDecimalLengthPacker.class.getSimpleName() + " class" + LINE_SEPARATOR);
        List<Integer> lenList = Arrays.asList(1, 12, 123, 1234);
        List<Integer> lenLengthList = Arrays.asList(1, 2, 3);
        for (int lenLength : lenLengthList) {
            String info = "numBytes: " + lenLength + LINE_SEPARATOR;
            stringBuilder.append(info);
            EbcdicDecimalLengthPacker ebcdicDecimalLengthPacker = EbcdicDecimalLengthPacker.getInstance(lenLength);
            for (int len : lenList) {
                String packedString;
                try {
                    byte[] packedLen = ebcdicDecimalLengthPacker.pack(len, null);
                    packedString = "packed as bytes " + HexService.bytesToHex(packedLen);
                } catch (Exception e) {
                    assertEquals(PackerRuntimeException.class, e.getClass());
                    packedString = "cannot be packed, exception: " + e.getMessage();
                }
                String row ="numBytes '" + lenLength + "', integer '" + len + "' " + packedString + LINE_SEPARATOR;
                stringBuilder.append(row);
            }
        }
        stringBuilder.append("Examples end.");
        assertTrue(stringBuilder.length() > 100);
        logger.info(stringBuilder.toString());
    }
}