package com.credibledoc.iso8583packer.asciihex;

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

import static org.junit.Assert.*;

public class AsciiLengthPackerTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    Logger logger = LoggerFactory.getLogger(AsciiLengthPackerTest.class);

    private FieldBuilder createField() {
        return
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(AsciiLengthPacker.getInstance(2))
                .defineBodyPacker(AsciiBodyPacker.getInstance());
    }

    /**
     * Used in documentation
     */
    @Test
    public void packUnpack() {
        String value = "abc";
        ValueHolder valueHolder = ValueHolder.newInstance(createField().getCurrentField());
        valueHolder.setValue(value);

        byte[] bytes = valueHolder.pack();
        String lengthHex = "3033";
        String valueHex = "616263";
        assertEquals(lengthHex + valueHex, HexService.bytesToHex(bytes));

        MsgValue msgValue = ValueHolder.unpack(bytes, 0, createField().getCurrentField());
        assertEquals(value, msgValue.getBodyValue(String.class));

        Visualizer visualizer = DumpService.getInstance();
        String msgFieldDump = "MsgField structure dump: " + visualizer.dumpMsgField(createField().getCurrentField());
        logger.info(msgFieldDump);
        
        String msgValueDump = visualizer.dumpMsgValue(createField().getCurrentField(), msgValue, false);
        logger.info("MsgValue structure dump: {}", msgValueDump);
    }

    @Test
    public void testPack() {
        AsciiLengthPacker asciiLengthPacker = AsciiLengthPacker.getInstance(2);
        byte[] bytes = asciiLengthPacker.pack(12);
        assertEquals("3132", HexService.bytesToHex(bytes));
    }

    @Test
    public void testUnpack() {
        AsciiLengthPacker asciiLengthPacker = AsciiLengthPacker.getInstance(3);
        byte[] bytes = HexService.hex2byte("313233");
        assertEquals(123, asciiLengthPacker.unpack(bytes, 0));
    }

    /**
     * Used in documentation
     */
    @Test
    public void packedExamples() {
        StringBuilder stringBuilder = new StringBuilder("Examples of integers packed with " +
            AsciiLengthPacker.class.getSimpleName() + " class" + LINE_SEPARATOR);
        List<Integer> lenList = Arrays.asList(1, 12, 123, 1234);
        List<Integer> lenLengthList = Arrays.asList(1, 2, 3);
        for (int lenLength : lenLengthList) {
            String info = "numBytes: " + lenLength + LINE_SEPARATOR;
            stringBuilder.append(info);
            AsciiLengthPacker asciiLengthPacker = AsciiLengthPacker.getInstance(lenLength);
            for (int len : lenList) {
                String packedString;
                try {
                    byte[] packedLen = asciiLengthPacker.pack(len);
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
        assertFalse(stringBuilder.toString().contains("ERROR"));
        logger.info(stringBuilder.toString());
    }
}
