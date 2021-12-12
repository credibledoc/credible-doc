package com.credibledoc.iso8583packer.ifa;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgPair;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.stringer.StringStringer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IfaBitmapPackerTest {
    private static final Logger logger = LoggerFactory.getLogger(IfaBitmapPackerTest.class);

    private static final String MTI_NAME = "MTI";
    private static final String BITMAP_NAME = "BITMAP";
    private static final String PAN_NAME = "PAN";

    @Test
    public void pack1() {
        IfaBitmapPacker ifaBitmapPacker = IfaBitmapPacker.getInstance(1);
        BitSet bitSet = new BitSet();
        bitSet.set(2);
        bitSet.set(4);
        byte[] bytes = ifaBitmapPacker.pack(bitSet);
        String hex = HexService.bytesToHex(bytes);
        String expected = "3530";
        assertEquals(expected, hex);
    }

    @Test
    public void unpack1() {
        IfaBitmapPacker ifaBitmapPacker = IfaBitmapPacker.getInstance(1);
        MsgValue msgValue = new MsgValue();
        String hex = "3530";
        byte[] bytes = HexService.hex2byte(hex);
        int unpackedLen = ifaBitmapPacker.unpack(msgValue, bytes, 0);
        assertEquals(2, unpackedLen);
        BitSet bitSet = msgValue.getBitSet();
        assertNotNull(bitSet);
        assertFalse(bitSet.get(1));
        assertTrue(bitSet.get(2));
        assertFalse(bitSet.get(3));
        assertTrue(bitSet.get(4));
        assertFalse(bitSet.get(5));
        assertFalse(bitSet.get(6));
        assertFalse(bitSet.get(7));
        assertFalse(bitSet.get(8));
    }

    @Test
    public void pack8() {
        IfaBitmapPacker ifaBitmapPacker = IfaBitmapPacker.getInstance(8);
        BitSet bitSet = new BitSet();
        bitSet.set(2);
        bitSet.set(4);
        byte[] bytes = ifaBitmapPacker.pack(bitSet);
        String hex = HexService.bytesToHex(bytes);
        String expected = "35303030303030303030303030303030";
        assertEquals(expected, hex);
    }

    @Test
    public void unpack8() {
        IfaBitmapPacker ifaBitmapPacker = IfaBitmapPacker.getInstance(8);
        MsgValue msgValue = new MsgValue();
        String hex = "35303030303030303030303030303030";
        byte[] bytes = HexService.hex2byte(hex);
        int unpackedLen = ifaBitmapPacker.unpack(msgValue, bytes, 0);
        assertEquals(16, unpackedLen);
        BitSet bitSet = msgValue.getBitSet();
        assertNotNull(bitSet);
        assertFalse(bitSet.get(0));
        assertFalse(bitSet.get(1));
        assertTrue(bitSet.get(2));
        assertFalse(bitSet.get(3));
        assertTrue(bitSet.get(4));
        assertFalse(bitSet.get(5));
    }

    @Test
    public void pack16() {
        IfaBitmapPacker ifaBitmapPacker = IfaBitmapPacker.getInstance(16);
        BitSet bitSet = new BitSet();
        bitSet.set(2);
        bitSet.set(4);
        bitSet.set(66);
        byte[] bytes = ifaBitmapPacker.pack(bitSet);
        String hex = HexService.bytesToHex(bytes);
        String expected = "4430303030303030303030303030303034303030303030303030303030303030";
        assertEquals(expected, hex);
    }

    @Test
    public void unpack16() {
        IfaBitmapPacker ifaBitmapPacker = IfaBitmapPacker.getInstance(16);
        MsgValue msgValue = new MsgValue();
        String hex = "4430303030303030303030303030303034303030303030303030303030303030";
        byte[] bytes = HexService.hex2byte(hex);
        int unpackedLen = ifaBitmapPacker.unpack(msgValue, bytes, 0);
        assertEquals(32, unpackedLen);
        BitSet bitSet = msgValue.getBitSet();
        assertNotNull(bitSet);
        assertFalse(bitSet.get(0));
        assertTrue(bitSet.get(1));
        assertTrue(bitSet.get(2));
        assertFalse(bitSet.get(3));
        assertTrue(bitSet.get(4));
        assertFalse(bitSet.get(5));
        assertTrue(bitSet.get(66));
    }

    @Test
    public void builder() {
        // definition
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("msg");

        MsgField isoMsgField = fieldBuilder.getCurrentField();

        MsgField mti = FieldBuilder.builder(MsgFieldType.VAL)
            .defineName(MTI_NAME)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineLen(2)
            .defineParent(isoMsgField)
            .getCurrentField();

        MsgField bitmap = FieldBuilder.from(mti)
            .createSibling(MsgFieldType.BIT_SET)
            .defineName(BITMAP_NAME)
            .defineHeaderBitmapPacker(IfaBitmapPacker.getInstance(16))
            .getCurrentField();

        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(2)
            .defineName(PAN_NAME)
            .defineStringer(StringStringer.getInstance())
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2));

        fieldBuilder.validateStructure();

        // filling with data
        ValueHolder valueHolder = ValueHolder.newInstance(isoMsgField);

        String mtiValue = "0200";
        valueHolder.jumpToChild(MTI_NAME).setValue(mtiValue);

        String pan = "123456781234567";
        valueHolder.jumpToSibling(BITMAP_NAME)
            .jumpToChild(PAN_NAME).setValue(pan);

        // packing
        byte[] bytes = valueHolder.jumpToRoot().pack();
        String expectedBitmapHex = "4330303030303030303030303030303030303030303030303030303030303030";
        String expectedPanLengthHex = "F0F8";
        char padding = BcdBodyPacker.FILLER_F;
        String expectedHex =
            mtiValue +
            expectedBitmapHex +
            expectedPanLengthHex + pan + padding;
        String packedHex = HexService.bytesToHex(bytes);
        assertEquals(expectedHex, packedHex);

        // unpacking
        MsgValue msgValue = ValueHolder.unpack(bytes, 0, isoMsgField);
        assertEquals(2, msgValue.getChildren().size());

        // data browsing
        MsgPair rootPair = ValueHolder.newInstance(msgValue, isoMsgField).getCurrentPair();
        assertNotNull(rootPair);

        String mtiString = ValueHolder.newInstance(rootPair).jumpToChild(MTI_NAME).getValue(String.class);
        assertEquals(mtiValue, mtiString);

        MsgPair bitmapPair = ValueHolder.newInstance(rootPair).jumpToChild(BITMAP_NAME).getCurrentPair();
        assertNotNull(bitmap);

        ValueHolder panValueHolder = ValueHolder.newInstance(bitmapPair).jumpToChild(PAN_NAME);
        assertNotNull(panValueHolder);

        String unpackedPanString = panValueHolder.getValue(String.class);
        assertEquals(pan, unpackedPanString);

        Visualizer visualizer = DumpService.getInstance();
        String msgFieldDump = visualizer.dumpMsgField(isoMsgField);
        logger.info("Root msgField dump: \n{}", msgFieldDump);

        String msgValueDump = DumpService.getInstance().dumpMsgValue(isoMsgField, msgValue, true);
        logger.info("Root msgValue dump: \n{}", msgValueDump);
    }
}
