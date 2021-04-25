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
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.stringer.StringStringer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class IfaBitmapAdaptablePackerTest {
    private static final Logger logger = LoggerFactory.getLogger(IfaBitmapAdaptablePackerTest.class);

    private static final String BITMAP_NAME = "BITMAP";
    private static final String PAN_NAME = "PAN";
    private static final String SETTLEMENT_CODE = "SettlementCode";
    private static final String TERTIARY_FIELD = "TertiaryField";
    private static final String MSG = "MSG";

    @Test
    public void adaptableBitsetTest() {
        MsgField isoMsgField = defineStructure();

        // filling with data
        ValueHolder valueHolder = ValueHolder.newInstance(isoMsgField);

        // primary bitMap
        String pan = "123456781234567";
        valueHolder.setValue(pan, MSG, BITMAP_NAME, PAN_NAME);
        
        String primaryHex = HexService.bytesToHex(valueHolder.pack());
        String expectedPrimaryHex = "34303030303030303030303030303030F0F8123456781234567F";
        assertEquals(primaryHex, expectedPrimaryHex);

        MsgValue primaryUnpacked = ValueHolder.newInstance(isoMsgField).unpack(HexService.hex2byte(primaryHex));
        ValueHolder primaryValueHolder = ValueHolder.newInstance(primaryUnpacked, isoMsgField);
        assertEquals(pan, primaryValueHolder.getValue(MSG, BITMAP_NAME, PAN_NAME));

        // secondary bitMap
        String settlementCodeValue = "2222";
        valueHolder.setValue(settlementCodeValue, MSG, BITMAP_NAME, SETTLEMENT_CODE);
        String secondaryHex = HexService.bytesToHex(valueHolder.pack());
        String expectedSecondaryHex = "4330303030303030303030303030303034303030303030303030303030303030F0F8123456781234567FF0F22222";
        assertEquals(expectedSecondaryHex, secondaryHex);
        
        MsgValue secondaryUnpacked = ValueHolder.newInstance(isoMsgField).unpack(HexService.hex2byte(secondaryHex));
        ValueHolder secondaryValueHolder = ValueHolder.newInstance(secondaryUnpacked, isoMsgField);
        assertEquals(settlementCodeValue, secondaryValueHolder.getValue(MSG, BITMAP_NAME, SETTLEMENT_CODE));
        
        // tertiary bitMap
        String tertiaryFieldValue = "3333";
        valueHolder.setValue(tertiaryFieldValue, MSG, BITMAP_NAME, TERTIARY_FIELD);
        String tertiaryHex = HexService.bytesToHex(valueHolder.pack());
        String expectedTertiaryHex = "433030303030303030303030303030304330303030303030303030303030303034303030303030303030303030303030F0F8123456781234567FF0F22222F0F23333";
        assertEquals(expectedTertiaryHex, tertiaryHex);

        MsgValue tertiaryUnpacked = ValueHolder.newInstance(isoMsgField).unpack(HexService.hex2byte(tertiaryHex));
        ValueHolder tertiaryValueHolder = ValueHolder.newInstance(tertiaryUnpacked, isoMsgField);
        assertEquals(tertiaryFieldValue, tertiaryValueHolder.getValue(MSG, BITMAP_NAME, TERTIARY_FIELD));

        Visualizer visualizer = DumpService.getInstance();
        String msgFieldDump = visualizer.dumpMsgField(isoMsgField);
        logger.info("Root msgField dump: \n{}{}", msgFieldDump, "End of msgField dump.");

        String msgValueDump = DumpService.getInstance().dumpMsgValue(isoMsgField, valueHolder.jumpToRoot().getCurrentMsgValue(), true);
        logger.info("Root msgValue dump: \n{}{}", msgValueDump, "End of msgValue dump.");
    }

    private MsgField defineStructure() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName(MSG);

        MsgField isoMsgField = fieldBuilder.getCurrentField();

        MsgField bitmap = FieldBuilder.from(isoMsgField)
            .createChild(MsgFieldType.BIT_SET)
            .defineName(BITMAP_NAME)
            .defineHeaderBitmapPacker(IfaBitmapPacker.getInstance())
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
        return isoMsgField;
    }
}
