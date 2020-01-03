package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class HexBodyPackerTest {
    private static final Logger logger = LoggerFactory.getLogger(HexBodyPackerTest.class);

    /**
     * Used in documentation
     */
    private FieldBuilder fixedLengthHex() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineLen(2)
            .defineBodyPacker(HexBodyPacker.getInstance());
    }
    
    /**
     * Used in documentation
     */
    @Test
    public void pack() {
        FieldBuilder fieldBuilder = fixedLengthHex();
        fieldBuilder.validateStructure();

        String value = "0123";
        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField())
            .setValue(value);
        
        valueHolder.validateData();

        byte[] valueBytes = valueHolder.pack();
        assertEquals(value, HexService.bytesToHex(valueBytes));
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
        FieldBuilder fieldBuilder = fixedLengthHex();
        fieldBuilder.validateStructure();

        String packedValue = "0123";
        MsgValue msgValue = ValueHolder.newInstance(fieldBuilder.getCurrentField())
            .unpack(HexService.hex2byte(packedValue));

        String unpackedValue = (String) msgValue.getBodyValue();
        assertEquals(packedValue, unpackedValue);
    }
    
    @Test(expected = PackerRuntimeException.class)
    public void testBadType() {
        byte[] bytes = new byte[2];
        byte[] value = HexService.hex2byte("112233");
        HexBodyPacker.getInstance().pack(value, bytes, 0);
    }
    
    @Test(expected = PackerRuntimeException.class)
    public void testLongValue() {
        byte[] bytes = new byte[2];
        String value = "112233";
        HexBodyPacker.getInstance().pack(value, bytes, 0);
    }

    @Test
    public void testPack() {
        byte[] bytes = new byte[1];
        String data = "AB";
        HexBodyPacker.getInstance().pack(data, bytes, 0);
        assertArrayEquals(HexService.hex2byte(data), bytes);
    }

    @Test(expected = PackerRuntimeException.class)
    public void testUnpackShort() {
        byte[] ab = HexService.hex2byte("61");
        HexBodyPacker.getInstance().unpack(ab, 0, 2);
    }

    /**
     * Used in documentation
     */
    @Test
    public void testMsgFieldStructure() {
        MsgField msgField = fixedLengthHex().getCurrentField();
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

        HexBodyPacker hexBodyPacker = HexBodyPacker.getInstance();
        generateLines(stringBuilder, values, hexBodyPacker);

        assertTrue(stringBuilder.length() > 100);
        assertFalse(stringBuilder.toString().contains("ERROR"));

        logger.info("Examples of values packed with HexBodyPacker\n{}Examples end.", stringBuilder.toString());
    }

    protected void generateLines(StringBuilder stringBuilder, List<String> values, HexBodyPacker hexBodyPacker) {
        for (String stringValue : values) {
            try {
                int packedLength = hexBodyPacker.getPackedLength(stringValue);
                byte[] bytes = new byte[packedLength];
                hexBodyPacker.pack(stringValue, bytes, 0);

                String unpackedString = hexBodyPacker.unpack(bytes, 0, packedLength);
                String line = "Value '" + stringValue + "' packed to bytes as hex: " + HexService.bytesToHex(bytes) +
                    " and unpacked again as String: " + unpackedString + System.lineSeparator();
                stringBuilder.append(line);
            } catch (Exception e) {
                String line = "Value '" + stringValue + "' cannot be packed to bytes. Exception: " + e.getMessage() +
                    "" + System.lineSeparator();
                stringBuilder.append(line);
            }
        }
    }
}
