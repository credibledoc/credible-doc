package com.credibledoc.iso8583packer.binary;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
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

public class BinaryLengthPackerTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    Logger logger = LoggerFactory.getLogger(BinaryLengthPackerTest.class);

    private FieldBuilder createField() {
        return
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(BinaryLengthPacker.getInstance(1))
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
        BinaryLengthPacker binaryLengthPacker = BinaryLengthPacker.getInstance(1);
        byte[] bytes = binaryLengthPacker.pack(12);
        assertEquals("0C", HexService.bytesToHex(bytes));
    }

    @Test
    public void testUnpack() {
        BinaryLengthPacker binaryLengthPacker = BinaryLengthPacker.getInstance(2);
        byte[] bytes = HexService.hex2byte("0123");
        assertEquals(291, binaryLengthPacker.unpack(bytes, 0));
    }

    /**
     * Used in documentation
     */
    @Test
    public void packedExamples() {
        StringBuilder stringBuilder = new StringBuilder("Examples of integers packed with " +
            BinaryLengthPacker.class.getSimpleName() + " class" + LINE_SEPARATOR);
        List<Integer> lenList = Arrays.asList(1, 255, 12345, 65535, 123456, 123456789);
        List<Integer> lenLengthList = Arrays.asList(1, 2, 3);
        for (int lenLength : lenLengthList) {
            String info = "numBytes: " + lenLength + LINE_SEPARATOR;
            stringBuilder.append(info);
            BinaryLengthPacker binaryLengthPacker = BinaryLengthPacker.getInstance(lenLength);
            for (int len : lenList) {
                String packedString;
                try {
                    byte[] packedLen = binaryLengthPacker.pack(len);
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
