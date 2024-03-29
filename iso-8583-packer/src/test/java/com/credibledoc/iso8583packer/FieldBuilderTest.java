package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.bcd.BcdLengthPacker;
import com.credibledoc.iso8583packer.builder.TestFieldBuilder;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgPair;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.field58.Field58;
import com.credibledoc.iso8583packer.stringer.StringStringer;
import com.credibledoc.iso8583packer.validator.TestValidatorService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FieldBuilderTest {

    private static final Logger logger = LoggerFactory.getLogger(FieldBuilderTest.class);

    private static final String PAN_02_NAME = "PAN_02";
    private static final String BITMAP_NAME = "bitmap";
    private static final String MTI_NAME = "mti";
    private static final String PROCESSING_CODE_03_NAME = "Processing_code_03";
    private static final String MSG_NAME = "msg";

    /**
     * Used in documentation
     */
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
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance(16))
            .getCurrentField();

        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(2)
            .defineName(PAN_02_NAME)
            .defineStringer(StringStringer.getInstance())
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2));
        
        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(3)
            .defineName(PROCESSING_CODE_03_NAME)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(BcdLengthPacker.getInstance(1));

        Field58.defineField58(bitmap);
        
        fieldBuilder.validateStructure();

        // filling with data
        ValueHolder valueHolder = ValueHolder.newInstance(isoMsgField);
        
        String mtiValue = "0200";
        valueHolder.jumpToChild(MTI_NAME).setValue(mtiValue);

        String pan = "123456781234567";
        String processingCode = "32";
        valueHolder.jumpToSibling(BITMAP_NAME)
            .jumpToChild(PAN_02_NAME).setValue(pan)
            .jumpToSibling(PROCESSING_CODE_03_NAME).setValue(processingCode);

        fillField58(valueHolder);

        // packing
        byte[] bytes = valueHolder.jumpToRoot().pack();
        String expectedBitmapHex = "E0000000000000400000000000000000";
        String expectedPanLengthHex = "F0F8";
        char padding = BcdBodyPacker.FILLER_F;
        String expectedProcessingCodeLenHex = "01";
        String expectedHex =
            mtiValue +
            expectedBitmapHex +
            expectedPanLengthHex + pan + padding +
            expectedProcessingCodeLenHex + processingCode;
        String packedHex = HexService.bytesToHex(bytes);
        assertTrue(packedHex.startsWith(expectedHex));
        
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
        
        ValueHolder panValueHolder = ValueHolder.newInstance(bitmapPair).jumpToChild(PAN_02_NAME);
        assertNotNull(panValueHolder);
        
        String unpackedPanString = panValueHolder.getValue(String.class);
        assertEquals(pan, unpackedPanString);

        ValueHolder processingCodeFieldHolder =
            ValueHolder.newInstance(bitmapPair).jumpToChild(PROCESSING_CODE_03_NAME);
        assertNotNull(processingCodeFieldHolder);

        String unpackedProcessingCode = processingCodeFieldHolder.getValue(String.class);
        assertEquals(processingCode, unpackedProcessingCode);

        Visualizer visualizer = DumpService.getInstance();
        String msgFieldDump = visualizer.dumpMsgField(isoMsgField);
        logger.info("Root msgField dump: \n{}{}", msgFieldDump, "End of msgField dump.");

        String msgValueDump = visualizer.dumpMsgValue(isoMsgField, msgValue, false);
        logger.info("Root msgValue dump: \n{}{}", msgValueDump, "End of msgValue dump.");
    }
    
    @Test
    public void msgValueOrderTest() {
        // definition
        MsgField isoMsgField = fieldsSortedByPosition(true);
        ValueHolder valueHolder = fillInData(isoMsgField);
        List<MsgValue> children = valueHolder.getCurrentMsgValue().getParent().getChildren();
        assertEquals(2, children.size());
        // check that the msgValues are ordered by the fieldNum definitions
        assertEquals(PAN_02_NAME, children.get(0).getName());
        assertEquals(PROCESSING_CODE_03_NAME, children.get(1).getName());

        MsgField orderedByPosition = fieldsSortedByPosition(false);
        ValueHolder valueHolderByPosition = fillInData(orderedByPosition);
        List<MsgValue> childrenByPosition = valueHolderByPosition.getCurrentMsgValue().getParent().getChildren();
        assertEquals(2, childrenByPosition.size());
        // check that the msgValues are ordered by the positions in MsgField
        assertEquals(PROCESSING_CODE_03_NAME, childrenByPosition.get(0).getName());
        assertEquals(PAN_02_NAME, childrenByPosition.get(1).getName());
    }

    private MsgField fieldsSortedByPosition(boolean sortByFieldNum) {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName(MSG_NAME);

        MsgField isoMsgField = fieldBuilder.getCurrentField();

        MsgField bitmap = FieldBuilder.builder(MsgFieldType.BIT_SET)
            .defineParent(isoMsgField)
            .defineName(BITMAP_NAME)
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance(16))
            .getCurrentField();

        FieldBuilder.from(bitmap, sortByFieldNum)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(3)
            .defineName(PROCESSING_CODE_03_NAME)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(BcdLengthPacker.getInstance(1));

        FieldBuilder.from(bitmap, sortByFieldNum)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(2)
            .defineName(PAN_02_NAME)
            .defineStringer(StringStringer.getInstance())
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2));

        fieldBuilder.validateStructure();
        return isoMsgField;
    }

    private ValueHolder fillInData(MsgField isoMsgField) {
        ValueHolder valueHolder = ValueHolder.newInstance(isoMsgField);

        String pan = "123456781234567";
        String processingCode = "32";
        valueHolder.jumpAbsolute(MSG_NAME, BITMAP_NAME, PROCESSING_CODE_03_NAME)
            .setValue(processingCode)
            .jumpToSibling(PAN_02_NAME).setValue(pan);
        return valueHolder;
    }

    @Test
    public void bitSetNullValue() {
        // definition
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName(MSG_NAME);

        MsgField isoMsgField = fieldBuilder.getCurrentField();
        
        MsgField bitmap = FieldBuilder.builder(MsgFieldType.BIT_SET)
            .defineParent(isoMsgField)
            .defineName(BITMAP_NAME)
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance(16))
            .getCurrentField();

        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(2)
            .defineName(PAN_02_NAME)
            .defineStringer(StringStringer.getInstance())
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2));

        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineFieldNum(3)
            .defineName(PROCESSING_CODE_03_NAME)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(BcdLengthPacker.getInstance(1));

        fieldBuilder.validateStructure();

        // filling with data
        ValueHolder valueHolder = ValueHolder.newInstance(isoMsgField);
        valueHolder.jumpAbsolute(MSG_NAME, BITMAP_NAME, PAN_02_NAME).setValue("123");
        String processingCode = "32";
        valueHolder.jumpAbsolute(MSG_NAME, BITMAP_NAME, PROCESSING_CODE_03_NAME)
            .setValue(processingCode)
            .jumpToSibling(PAN_02_NAME).setValue(null);
        
        byte[] bytes = valueHolder.jumpToRoot().pack();

        MsgValue unpacked = ValueHolder.unpack(bytes, 0, isoMsgField);
        assertFalse("Field with num 2 should be 'false'.", unpacked.getChildren().get(0).getBitSet().get(2));
    }

    protected void fillField58(ValueHolder valueHolder) {
        ValueHolder field58ValueHolder = valueHolder.copyValueHolder()
            .jumpToParent()
            .jumpToChild(Field58.F_58_NAME);

        String rateReferenceId = "018F1AEE03E404843C";
        field58ValueHolder.jumpToChild(Field58.RATE_REQUEST_REFERENCE_ID_35_NAME)
            .setValue(rateReferenceId);

        String dccStatus = "U";
        field58ValueHolder.jumpToSibling(Field58.DCC_DATA_37_NAME)
            .jumpToChild(Field58.DCC_STATUS_37_1)
            .setValue(dccStatus);

        String currencyCode = "978";
        field58ValueHolder.jumpToSibling(Field58.CURRENCY_CODE_37_2)
            .setValue(currencyCode);

        String amount = "000000005555";
        field58ValueHolder.jumpToSibling(Field58.TRANSACTION_AMOUNT_37_5)
            .setValue(amount);

        String conversionRate = "40011670";
        field58ValueHolder.jumpToSibling(Field58.CONVERSION_RATE_37_17)
            .setValue(conversionRate);

        String nonLoyaltyGroup = "003021";
        field58ValueHolder.jumpToParent().jumpToSibling(Field58.NON_LOYALTY_GROUP_53_NAME)
            .setValue(nonLoyaltyGroup);

        String posTerminalCapabilities = "8";
        field58ValueHolder.jumpToSibling(Field58.POS_TERMINAL_CAPABILITIES_98_NAME)
            .setValue(posTerminalCapabilities);
    }

    @Test
    public void cloneTest() {
        MsgField msgField = new MsgField();
        msgField.setType(MsgFieldType.VAL);

        FieldBuilder fieldBuilder = FieldBuilder.clone(msgField);
        assertNotNull(fieldBuilder);

        assertNotNull(fieldBuilder.getCurrentField());
        assertEquals(MsgFieldType.VAL, fieldBuilder.getCurrentField().getType());
    }

    @Test(expected = PackerRuntimeException.class)
    public void validateStructureTest() {
        MsgField msgField = new MsgField();
        msgField.setRoot(msgField);
        FieldBuilder.validateStructure(msgField);
    }

    @Test
    public void jumpTest() {
        FieldBuilder parentFieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("parent");
        
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.VAL)
            .defineName("first")
            .defineParent(parentFieldBuilder.getCurrentField());

        fieldBuilder.cloneToSibling()
            .defineName("second");

        assertEquals("second", fieldBuilder.getCurrentField().getName());

        FieldBuilder firstFieldBuilder = fieldBuilder.jumpToSibling("first");
        assertEquals("first", firstFieldBuilder.getCurrentField().getName());

        FieldBuilder parentFieldBuilder2 = fieldBuilder.jumpToParent();
        assertEquals("parent", parentFieldBuilder2.getCurrentField().getName());

        FieldBuilder secondFieldBuilder = fieldBuilder.jumpToChild("second");
        assertEquals("second", secondFieldBuilder.getCurrentField().getName());
        
        FieldBuilder rootFieldBuilder = fieldBuilder.jumpToRoot();
        assertEquals("parent", rootFieldBuilder.getCurrentField().getName());
    }

    @Test(expected = PackerRuntimeException.class)
    public void defineExactlyLenTest() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.TAG_VAL)
            .defineExactlyLen(3)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineName("field");

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField());
        valueHolder.setValue("123");
    }

    @Test
    public void otherValidatorTest() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.TAG_VAL)
            .defineExactlyLen(3)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineName("field");

        fieldBuilder.setValidator(TestValidatorService.getInstance());
        fieldBuilder.validateStructure();
        // exception not thrown
    }
    
    @Test
    public void customFieldBuilderTest() {
        FieldBuilder testFieldBuilder = TestFieldBuilder.builder(MsgFieldType.TAG_VAL)
            .defineName("field");
        
        assertEquals(TestFieldBuilder.class, testFieldBuilder.getClass());
        assertEquals(TestValidatorService.class, testFieldBuilder.validator.getClass());

        testFieldBuilder.setValidator(TestValidatorService.getInstance());
        testFieldBuilder.validateStructure();
        // exception not thrown
    }
}
