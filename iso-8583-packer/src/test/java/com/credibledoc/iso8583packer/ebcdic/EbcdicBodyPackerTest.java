package com.credibledoc.iso8583packer.ebcdic;

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

public class EbcdicBodyPackerTest {
    private static final Logger logger = LoggerFactory.getLogger(EbcdicBodyPackerTest.class);

    /**
     * Used in documentation
     */
    private FieldBuilder fixedLengthEbcdic() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineBodyPacker(EbcdicBodyPacker.getInstance())
            .defineLen(2);
    }
    
    /**
     * Used in documentation
     */
    @Test
    public void pack() {
        FieldBuilder fieldBuilder = fixedLengthEbcdic();
        fieldBuilder.validateStructure();

        String value = "He";
        ValueHolder valueHolder =
            ValueHolder.newInstance(fieldBuilder.getCurrentField())
            .setValue(value);
        
        valueHolder.validateData();

        byte[] valueBytes = valueHolder.pack();
        assertArrayEquals(HexService.hex2byte("C885"), valueBytes);
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
        byte[] bytes = HexService.hex2byte("C885");
        FieldBuilder fieldBuilder = fixedLengthEbcdic();
        MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
        assertEquals("He", msgValue.getBodyValue(String.class));
    }

    @Test(expected = PackerRuntimeException.class)
    public void testLongValue() {
        byte[] bytes = new byte[2];
        byte[] value = HexService.hex2byte("112233");
        EbcdicBodyPacker.getInstance().pack(value, bytes, 0);
    }

    @Test
    public void testPack() {
        String data = "Hello";
        byte[] bytes = new byte[data.length()];
        EbcdicBodyPacker.getInstance().pack(data, bytes, 0);
        assertArrayEquals(HexService.hex2byte("C885939396"), bytes);
    }

    @Test(expected = PackerRuntimeException.class)
    public void testUnpackShort() {
        byte[] ab = HexService.hex2byte("61");
        EbcdicBodyPacker.getInstance().unpack(ab, 0, 2);
    }

    /**
     * Used in documentation
     */
    @Test
    public void testMsgFieldStructure() {
        MsgField msgField = fixedLengthEbcdic().getCurrentField();
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
        List<String> values = Arrays.asList("0A", "Hello", "A b");
        
        EbcdicBodyPacker ebcdicBodyPacker = EbcdicBodyPacker.getInstance();
        generateLines(stringBuilder, values, ebcdicBodyPacker);
        
        assertTrue(stringBuilder.length() > 100);
        assertFalse(stringBuilder.toString().contains("ERROR"));
        
        logger.info("Examples of values packed with EbcdicBodyPacker\n{}Examples end.", stringBuilder.toString());
    }

    protected void generateLines(StringBuilder stringBuilder, List<String> values, EbcdicBodyPacker ebcdicBodyPacker) {
        for (String value : values) {
            try {
                int packedLength = ebcdicBodyPacker.getPackedLength(value);
                byte[] bytes = new byte[packedLength];
                ebcdicBodyPacker.pack(value, bytes, 0);

                String unpackedString = ebcdicBodyPacker.unpack(bytes, 0, bytes.length);
                String line = "Value '" + value + "' packed to bytes as hex: " + HexService.bytesToHex(bytes) +
                    " and unpacked again as String: '" + unpackedString + "'" + System.lineSeparator();
                stringBuilder.append(line);
            } catch (Exception e) {
                String line = "Value '" + value + "' cannot be packed to bytes. Exception: " + e.getMessage() +
                    "" + System.lineSeparator();
                stringBuilder.append(line);
            }
        }
    }
}
