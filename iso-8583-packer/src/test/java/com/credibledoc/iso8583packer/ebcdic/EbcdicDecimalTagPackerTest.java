package com.credibledoc.iso8583packer.ebcdic;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.dump.Visualizer;
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

public class EbcdicDecimalTagPackerTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String FIELD_1_NAME = "field_1";
    Logger logger = LoggerFactory.getLogger(EbcdicDecimalTagPackerTest.class);

    private FieldBuilder createField() {
        return FieldBuilder.builder(MsgFieldType.MSG)
            .defineChildrenTagPacker(EbcdicDecimalTagPacker.getInstance(1))
            .defineName("root")
            
            .createChild(MsgFieldType.TAG_VAL)
            .defineName(FIELD_1_NAME)
            .defineHeaderTag(1)
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
        String tagHex = "F1";
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
        EbcdicDecimalTagPacker ebcdicDecimalTagPacker = EbcdicDecimalTagPacker.getInstance(1);
        ebcdicDecimalTagPacker.pack(12);
    }

    @Test
    public void testPack() {
        EbcdicDecimalTagPacker ebcdicDecimalTagPacker = EbcdicDecimalTagPacker.getInstance(2);
        byte[] bytes = ebcdicDecimalTagPacker.pack(12);
        assertEquals("F1F2", HexService.bytesToHex(bytes));
    }

    @Test
    public void testUnpack() {
        EbcdicDecimalTagPacker ebcdicDecimalTagPacker = EbcdicDecimalTagPacker.getInstance(3);
        byte[] bytes = HexService.hex2byte("F1F2F3");
        int tag = ebcdicDecimalTagPacker.unpack(bytes, 0);
        assertEquals(123, tag);
    }

    @Test(expected = PackerRuntimeException.class)
    public void testUnpackSmaller() {
        EbcdicDecimalTagPacker ebcdicDecimalTagPacker = EbcdicDecimalTagPacker.getInstance(4);
        byte[] bytes = HexService.hex2byte("F1F2F3");
        ebcdicDecimalTagPacker.unpack(bytes, 0);
    }

    /**
     * Used in documentation
     */
    @Test
    public void packedExamples() {
        StringBuilder stringBuilder = new StringBuilder("Examples of integers packed with " +
            EbcdicDecimalTagPacker.class.getSimpleName() + " class" + LINE_SEPARATOR);
        List<Integer> lenList = Arrays.asList(1, 12, 123, 1234);
        List<Integer> lenTagList = Arrays.asList(1, 2, 3);
        for (int lenTag : lenTagList) {
            String info = "numBytes: " + lenTag + LINE_SEPARATOR;
            stringBuilder.append(info);
            EbcdicDecimalTagPacker ebcdicDecimalTagPacker = EbcdicDecimalTagPacker.getInstance(lenTag);
            for (int len : lenList) {
                String packedString;
                try {
                    byte[] packedLen = ebcdicDecimalTagPacker.pack(len);
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
