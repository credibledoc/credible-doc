package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HexLengthPackerTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    Logger logger = LoggerFactory.getLogger(HexLengthPackerTest.class);

    private FieldBuilder createField() {
        return
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(HexLengthPacker.getInstance())
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
    }

    /**
     * Used in documentation
     */
    @Test
    public void packUnpack() {
        String value = "123456789";
        ValueHolder valueHolder = ValueHolder.newInstance(createField().getCurrentField());
        valueHolder.setValue(value);

        byte[] bytes = valueHolder.pack();
        String lengthHex = "05";
        String valueHex = "0123456789";
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
        HexLengthPacker hexLengthPacker = HexLengthPacker.getInstance();
        byte[] bytes = hexLengthPacker.pack(12, 1);
        assertEquals("0C", HexService.bytesToHex(bytes));
    }

    @Test
    public void testUnpack() {
        HexLengthPacker hexLengthPacker = HexLengthPacker.getInstance();
        byte[] bytes = HexService.hex2byte("82FFFF");
        int lenLength = hexLengthPacker.calculateLenLength(bytes, 0);
        assertEquals(3, lenLength);
        int length = hexLengthPacker.unpack(bytes, 0, lenLength);
        assertEquals(65535, length);
    }

    /**
     * Used in documentation
     */
    @Test
    public void packedExamples() {
        StringBuilder stringBuilder = new StringBuilder("Examples of integers packed with " +
            HexLengthPacker.class.getSimpleName() + " class" + LINE_SEPARATOR);
        List<Integer> lenList = Arrays.asList(1, 255, 12345, 65535, 123456, 123456789, 1234567899, Integer.MAX_VALUE);
        List<Integer> lenLengthList = Arrays.asList(1, 2, 3);
        for (int lenLength : lenLengthList) {
            String info = "numBytes: " + lenLength + LINE_SEPARATOR;
            stringBuilder.append(info);
            HexLengthPacker hexLengthPacker = HexLengthPacker.getInstance();
            for (int len : lenList) {
                String packedString;
                try {
                    byte[] packedLen = hexLengthPacker.pack(len, lenLength);
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
