package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.dump.Visualizer;
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

public class HexTagPackerTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String FIELD_1_NAME = "field_1";
    Logger logger = LoggerFactory.getLogger(HexTagPackerTest.class);

    private FieldBuilder createField() {
        return FieldBuilder.builder(MsgFieldType.MSG)
            .defineChildrenTagLen(1)
            .defineChildrenTagPacker(HexTagPacker.getInstance())
            .defineName("root")
            
            .createChild(MsgFieldType.TAG_VAL)
            .defineName(FIELD_1_NAME)
            .defineTagNum(1)
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .jumpToRoot()
            .validateStructure();
    }

    /**
     * Used in documentation
     */
    @Test
    public void packUnpack() {
        String value = "1234";
        MsgField field1 = createField().jumpToChild(FIELD_1_NAME).getCurrentField();
        ValueHolder valueHolder = ValueHolder.newInstance(field1);
        valueHolder.setValue(value);

        byte[] bytes = valueHolder.pack();
        String tagHex = "01";
        String valueHex = "1234";
        assertEquals(tagHex + valueHex, HexService.bytesToHex(bytes));

        MsgField msgField = createField().getCurrentField();
        MsgValue msgValue = ValueHolder.unpack(bytes, 0, msgField);
        ValueHolder valueHolderUnpacked = ValueHolder.newInstance(msgValue, msgField);
        valueHolderUnpacked.jumpToChild(FIELD_1_NAME);
        String unpackedValue = valueHolderUnpacked.getValue(String.class);
        assertEquals(value, unpackedValue);

        Visualizer visualizer = DumpService.getInstance();
        String msgFieldDump = "MsgField structure dump: " + visualizer.dumpMsgField(msgField);
        logger.info(msgFieldDump);
        
        String msgValueDump = visualizer.dumpMsgValue(msgField, msgValue, false);
        logger.info("MsgValue structure dump: {}", msgValueDump);
    }

    @Test(expected = PackerRuntimeException.class)
    public void testPackLonger() {
        HexTagPacker hexTagPacker = HexTagPacker.getInstance();
        hexTagPacker.pack(1234, 1);
    }

    @Test
    public void testPack() {
        HexTagPacker hexTagPacker = HexTagPacker.getInstance();
        byte[] bytes = hexTagPacker.pack(12, 2);
        assertEquals("000C", HexService.bytesToHex(bytes));
    }

    @Test
    public void testUnpack() {
        HexTagPacker hexTagPacker = HexTagPacker.getInstance();
        byte[] bytes = HexService.hex2byte("Hello");
        int tagNum = hexTagPacker.unpack(bytes, 0, 3);
        assertEquals(16777215, tagNum);
    }

    @Test(expected = PackerRuntimeException.class)
    public void testUnpackSmaller() {
        HexTagPacker hexTagPacker = HexTagPacker.getInstance();
        byte[] bytes = HexService.hex2byte("F1F2F3");
        hexTagPacker.unpack(bytes, 0, 4);
    }

    /**
     * Used in documentation
     */
    @Test
    public void packedExamples() {
        StringBuilder stringBuilder = new StringBuilder("Examples of integers packed with " +
            HexTagPacker.class.getSimpleName() + " class" + LINE_SEPARATOR);
        List<Integer> lenList = Arrays.asList(1, 15, 16, 1122, 112233, 1122334455);
        List<Integer> lenTagList = Arrays.asList(1, 2, 3);
        for (int lenTag : lenTagList) {
            String info = "numBytes: " + lenTag + LINE_SEPARATOR;
            stringBuilder.append(info);
            HexTagPacker hexTagPacker = HexTagPacker.getInstance();
            for (int len : lenList) {
                String packedString;
                try {
                    byte[] packedLen = hexTagPacker.pack(len, lenTag);
                    packedString = "packed as bytes " + HexService.bytesToHex(packedLen);
                } catch (Exception e) {
                    assertEquals(PackerRuntimeException.class, e.getClass());
                    packedString = "cannot be packed, exception: " + e.getMessage();
                }
                String row ="numBytes '" + lenTag + "', integer '" + len + "' " + packedString + LINE_SEPARATOR;
                stringBuilder.append(row);
            }
        }
        stringBuilder.append("Examples end.");
        assertTrue(stringBuilder.length() > 100);
        assertFalse(stringBuilder.toString().contains("ERROR"));
        logger.info(stringBuilder.toString());
    }
}
