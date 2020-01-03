package com.credibledoc.iso8583packer.asciihex;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
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

public class AsciiBodyPackerTest {
    private static final Logger logger = LoggerFactory.getLogger(AsciiBodyPackerTest.class);

    /**
     * Used in documentation
     */
    private FieldBuilder fixedLengthAscii() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineLen(2)
            .defineBodyPacker(AsciiBodyPacker.getInstance());
    }
    
    /**
     * Used in documentation
     */
    @Test
    public void pack() {
        FieldBuilder fieldBuilder = fixedLengthAscii();
        fieldBuilder.validateStructure();

        String value = "ab";
        ValueHolder valueHolder =
            ValueHolder.newInstance(fieldBuilder.getCurrentField())
            .setValue(value);
        
        valueHolder.validateData();

        byte[] valueBytes = valueHolder.pack();
        String bytesHex = HexService.bytesToHex(valueBytes);
        assertEquals("6162", bytesHex);
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
        byte[] bytes = HexService.hex2byte("6162");
        FieldBuilder fieldBuilder = fixedLengthAscii();
        MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
        assertEquals("ab", msgValue.getBodyValue(String.class));
    }

    @Test(expected = PackerRuntimeException.class)
    public void testLongValue() {
        byte[] bytes = new byte[2];
        AsciiBodyPacker.getInstance().pack("123", bytes, 0);
    }

    @Test
    public void testPack() {
        byte[] bytes = new byte[2];
        AsciiBodyPacker.getInstance().pack("ab", bytes, 0);
        assertEquals("6162", HexService.bytesToHex(bytes));
    }

    @Test
    public void testUnpack() {
        byte[] ab = HexService.hex2byte("6162");
        String result = AsciiBodyPacker.getInstance().unpack(ab, 0, 2);
        assertEquals("ab", result);
    }

    @Test(expected = PackerRuntimeException.class)
    public void testUnpackShort() {
        byte[] ab = HexService.hex2byte("61");
        AsciiBodyPacker.getInstance().unpack(ab, 0, 2);
    }

    /**
     * Used in documentation
     */
    @Test
    public void testMsgFieldStructure() {
        MsgField msgField = fixedLengthAscii().getCurrentField();
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
        List<String> values = Arrays.asList("a", "ab", "abc", "A 1234 bcd");
        
        AsciiBodyPacker leftPadding0 = AsciiBodyPacker.getInstance();
        generateLines(stringBuilder, values, leftPadding0);
        
        assertTrue(stringBuilder.length() > 100);
        assertFalse(stringBuilder.toString().contains("ERROR"));
        
        logger.info("Examples of values packed with AsciiBodyPacker\n{}Examples end.", stringBuilder.toString());
    }

    protected void generateLines(StringBuilder stringBuilder, List<String> values, AsciiBodyPacker leftPadding0) {
        for (String value : values) {
            try {
                int packedLength = leftPadding0.getPackedLength(value);
                byte[] bytes = new byte[packedLength];
                leftPadding0.pack(value, bytes, 0);
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
