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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IfaBitmapPackerExtendedTest {
    private static final Logger logger = LoggerFactory.getLogger(IfaBitmapPackerExtendedTest.class);

    private static final String MTI_NAME = "MTI";
    private static final String BITMAP_NAME = "BITMAP";
    private static final String PAN_NAME = "PAN";
    private static final String SETTLEMENT_CODE = "SettlementCode";
    private static final String TERTIARY_FIELD = "TertiaryField";
    private static final String MSG = "MSG";

    /**
     * Used in documentation
     */
    @Test
    public void tertiaryBitsetTest() {
        // definition
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName(MSG);

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
            .defineHeaderBitmapPacker(IfaBitmapPacker.getInstance(24))
            .getCurrentField();

        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(2)
            .defineName(PAN_NAME)
            .defineStringer(StringStringer.getInstance())
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2));

        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(66)
            .defineName(SETTLEMENT_CODE)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2));

        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(130)
            .defineName(TERTIARY_FIELD)
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

        String settlementCodeValue = "2222";
        valueHolder.jumpAbsolute(MSG, BITMAP_NAME, SETTLEMENT_CODE).setValue(settlementCodeValue);

        String tertiaryFieldValue = "3333";
        valueHolder.jumpAbsolute(MSG, BITMAP_NAME, TERTIARY_FIELD).setValue(tertiaryFieldValue);

        // packing
        byte[] bytes = valueHolder.jumpToRoot().pack();
        String expectedBitmapHex = "433030303030303030303030303030304330303030303030303030303030303034303030303030303030303030303030";
        String expectedPanLengthHex = "F0F8";
        char padding = BcdBodyPacker.FILLER_F;
        String expectedLenTwoBytesHex = "F0F2";
        String expectedHex =
                mtiValue +
                expectedBitmapHex +
                expectedPanLengthHex + pan + padding +
                expectedLenTwoBytesHex + settlementCodeValue +
                expectedLenTwoBytesHex + tertiaryFieldValue;
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
        assertNotNull(bitmapPair);

        ValueHolder panValueHolder = ValueHolder.newInstance(bitmapPair).jumpToChild(PAN_NAME);
        assertNotNull(panValueHolder);

        String unpackedPanString = panValueHolder.getValue(String.class);
        assertEquals(pan, unpackedPanString);

        Visualizer visualizer = DumpService.getInstance();
        String msgFieldDump = visualizer.dumpMsgField(isoMsgField);
        logger.info("Root msgField dump: \n{}{}", msgFieldDump, "End of msgField dump.");

        String msgValueDump = DumpService.getInstance().dumpMsgValue(isoMsgField, msgValue, true);
        logger.info("Root msgValue dump: \n{}{}", msgValueDump, "End of msgValue dump.");
    }
}
