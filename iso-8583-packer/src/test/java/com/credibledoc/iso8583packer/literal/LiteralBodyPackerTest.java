package com.credibledoc.iso8583packer.literal;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.binary.BinaryToHexStringer;
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

public class LiteralBodyPackerTest {
    private static final Logger logger = LoggerFactory.getLogger(LiteralBodyPackerTest.class);

    /**
     * Used in documentation
     */
    private FieldBuilder fixedLengthLiteral() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineBodyPacker(LiteralBodyPacker.getInstance())
            .defineStringer(BinaryToHexStringer.getInstance())
            .defineLen(2);
    }
    
    /**
     * Used in documentation
     */
    @Test
    public void pack() {
        FieldBuilder fieldBuilder = fixedLengthLiteral();
        fieldBuilder.validateStructure();

        byte[] value = HexService.hex2byte("FFFF");
        ValueHolder valueHolder =
            ValueHolder.newInstance(fieldBuilder.getCurrentField())
            .setValue(value);
        
        valueHolder.validateData();

        byte[] valueBytes = valueHolder.pack();
        assertArrayEquals(value, valueBytes);
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
        FieldBuilder fieldBuilder = fixedLengthLiteral();
        MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
        assertArrayEquals(bytes, msgValue.getBodyValue(byte[].class));
    }

    @Test(expected = PackerRuntimeException.class)
    public void testLongValue() {
        byte[] bytes = new byte[2];
        byte[] value = HexService.hex2byte("112233");
        LiteralBodyPacker.getInstance().pack(value, bytes, 0);
    }

    @Test
    public void testPack() {
        byte[] bytes = new byte[1];
        byte[] data = HexService.hex2byte("AB");
        LiteralBodyPacker.getInstance().pack(data, bytes, 0);
        assertArrayEquals(data, bytes);
    }

    @Test(expected = PackerRuntimeException.class)
    public void testUnpackShort() {
        byte[] ab = HexService.hex2byte("61");
        LiteralBodyPacker.getInstance().unpack(ab, 0, 2);
    }

    /**
     * Used in documentation
     */
    @Test
    public void testMsgFieldStructure() {
        MsgField msgField = fixedLengthLiteral().getCurrentField();
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
        List<String> values = Arrays.asList("0A", "AA", "0ABC", "A12345BCDE", "FF");
        
        LiteralBodyPacker leftPadding0 = LiteralBodyPacker.getInstance();
        generateLines(stringBuilder, values, leftPadding0);
        
        assertTrue(stringBuilder.length() > 100);
        
        logger.info("Examples of values packed with LiteralBodyPacker\n{}Examples end.", stringBuilder.toString());
    }

    protected void generateLines(StringBuilder stringBuilder, List<String> values, LiteralBodyPacker literalBodyPacker) {
        for (String stringValue : values) {
            try {
                byte[] value = HexService.hex2byte(stringValue);
                int packedLength = literalBodyPacker.getPackedLength(value);
                byte[] bytes = new byte[packedLength];
                literalBodyPacker.pack(value, bytes, 0);

                int len = literalBodyPacker.getPackedLength(bytes);
                byte[] unpackedBytes = literalBodyPacker.unpack(bytes, 0, len);
                String line = "Value '" + stringValue + "' packed to bytes as hex: " + HexService.bytesToHex(bytes) +
                    " and unpacked again as hex: " + HexService.bytesToHex(unpackedBytes) + System.lineSeparator();
                stringBuilder.append(line);
            } catch (Exception e) {
                String line = "Value '" + stringValue + "' cannot be packed to bytes. Exception: " + e.getMessage() +
                    "" + System.lineSeparator();
                stringBuilder.append(line);
            }
        }
    }
}
