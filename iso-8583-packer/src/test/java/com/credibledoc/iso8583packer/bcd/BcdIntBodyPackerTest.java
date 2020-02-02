package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class BcdIntBodyPackerTest {
    private static final Logger logger = LoggerFactory.getLogger(BcdIntBodyPackerTest.class);

    /**
     * Used in documentation
     */
    private FieldBuilder fixedLengthBcdInt() {
        return FieldBuilder.builder(MsgFieldType.VAL)
                .defineLen(2)
                .defineBodyPacker(BcdIntBodyPacker.getInstance(2));
    }
    
    /**
     * Used in documentation
     */
    @Test
    public void pack() {
        FieldBuilder fieldBuilder = fixedLengthBcdInt();
        fieldBuilder.validateStructure();

        int value = 123;
        ValueHolder valueHolder =
            ValueHolder.newInstance(fieldBuilder.getCurrentField())
            .setValue(value);
        
        valueHolder.validateData();

        byte[] valueBytes = valueHolder.pack();
        String bytesHex = HexService.bytesToHex(valueBytes);
        assertEquals("0123", bytesHex);
        valueHolder.jumpToRoot();
        MsgField msgField = valueHolder.getCurrentMsgField();
        MsgValue msgValue = valueHolder.getCurrentMsgValue();
        String dump = DumpService.getInstance().dumpMsgValue(msgField, msgValue, true);
        logger.info("MsgValue structure dump: \n{}", dump);
    }

    /**
     * Used in documentation
     */
    @Test
    public void unpack() {
        FieldBuilder fieldBuilder = fixedLengthBcdInt();
        fieldBuilder.validateStructure();

        String packedHex = "0456";
        byte[] packedBytes = HexService.hex2byte(packedHex);

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder);
        valueHolder.unpack(packedBytes);
        int unpackedValue = valueHolder.getValue(Integer.class);
        int expectedValue = 456;
        assertEquals(expectedValue, unpackedValue);
    }


    /**
     * Used in documentation
     */
    @Test
    public void testMsgFieldStructure() {
        MsgField msgField = fixedLengthBcdInt().getCurrentField();
        DumpService dumpService = DumpService.getInstance();
        String dump = dumpService.dumpMsgField(msgField);
        assertNotNull(dump);
        logger.info("MsgField structure dump: \n{}", dump);
    }

    /**
     * Used in documentation
     */
    @Test
    public void testExamples() {
        StringBuilder stringBuilder = new StringBuilder();
        List<Integer> values = Arrays.asList(1, 12, 123, 1234);
        List<Integer> sizes = Arrays.asList(1, 2, 3, 4);

        for (int numBytes : sizes) {
            BcdIntBodyPacker packer = BcdIntBodyPacker.getInstance(numBytes);
            stringBuilder.append("BcdIntBodyPacker with numBytes '")
                .append(numBytes).append("'").append(System.lineSeparator());
            generateLines(stringBuilder, values, packer);
        }
        
        assertTrue(stringBuilder.length() > 100);
        assertFalse(stringBuilder.toString().contains("ERROR"));
        
        logger.info("Examples of values packed with BcdIntBodyPacker \n{}Examples end.", stringBuilder.toString());
    }

    protected void generateLines(StringBuilder stringBuilder, List<Integer> values, BcdIntBodyPacker bcdIntBodyPacker) {
        for (Integer value : values) {
            try {
                int packedLength = bcdIntBodyPacker.getPackedLength(value);
                byte[] bytes = new byte[packedLength];
                bcdIntBodyPacker.pack(value, bytes, 0);
                String line = "Value '" + value + "' packed to bytes as hex: " + HexService.bytesToHex(bytes) +
                    "" + System.lineSeparator();
                stringBuilder.append(line);
            } catch (Exception e) {
                String line = "Value '" + value + "' cannot be packed to bytes. Exception: " + e.getMessage() +
                    "" + System.lineSeparator();
                stringBuilder.append(line);
            }
        }
    }
}
