package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.dump.Visualizer;
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

public class BcdLengthPackerTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();
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
        ValueHolder valueHolder = ValueHolder.newInstance(createField().getCurrentField());
        valueHolder.setValue(value);

        byte[] bytes = valueHolder.pack();
        String lengthHex = "0002";
        String valueHex = "0123";
        assertEquals(lengthHex + valueHex, HexService.bytesToHex(bytes));

        MsgValue msgValue = ValueHolder.unpack(bytes, 0, createField().getCurrentField());
        assertEquals(value, msgValue.getBodyValue(String.class));

        Visualizer visualizer = new DumpService();
        String msgFieldDump = "MsgField structure dump: " + visualizer.dumpMsgField(createField().getCurrentField());
        logger.info(msgFieldDump);
        
        String msgValueDump = visualizer.dumpMsgValue(createField().getCurrentField(), msgValue, false);
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

    /**
     * Used in documentation
     */
    @Test
    public void packedExamples() {
        StringBuilder stringBuilder = new StringBuilder("Examples of integers packed with " +
            BcdLengthPacker.class.getSimpleName() + " class" + LINE_SEPARATOR);
        List<Integer> lenList = Arrays.asList(1, 12, 123, 1234, 12345, 123456, 1234567);
        List<Integer> lenLengthList = Arrays.asList(1, 2, 3);
        for (int lenLength : lenLengthList) {
            String info = "numBytes: " + lenLength + LINE_SEPARATOR;
            stringBuilder.append(info);
            BcdLengthPacker bcdLengthPacker = BcdLengthPacker.getInstance(lenLength);
            for (int len : lenList) {
                String packedString;
                try {
                    byte[] packedLen = bcdLengthPacker.pack(len, null);
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
