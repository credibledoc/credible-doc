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
import com.credibledoc.iso8583packer.stringer.StringStringer;
import com.credibledoc.iso8583packer.validator.TestValidator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FieldBuilderTest {

    private static final Logger logger = LoggerFactory.getLogger(FieldBuilderTest.class);

    private static final String PAN_02_NAME = "PAN_02";
    private static final String BITMAP_NAME = "bitmap";
    private static final String MTI_NAME = "mti";
    private static final String PROCESSING_CODE_03_NAME = "Processing_code_03";

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
            .crateSibling(MsgFieldType.BIT_SET)
            .defineName(BITMAP_NAME)
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance())
            .defineLen(16)
            .defineParent(isoMsgField)
            .getCurrentField();

        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineTagNum(2)
            .defineName(PAN_02_NAME)
            .defineStringer(StringStringer.INSTANCE)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2));
        
        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineTagNum(3)
            .defineName(PROCESSING_CODE_03_NAME)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(BcdLengthPacker.getInstance(1));
        
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
        
        // packing
        byte[] bytes = valueHolder.jumpToRoot().pack();
        String expectedBitmapHex = "6000000000000000";
        String expectedPanLengthHex = "F0F8";
        char padding = BcdBodyPacker.FILLER_F;
        String expectedProcessingCodeLenHex = "01";
        String expectedHex =
            mtiValue +
            expectedBitmapHex +
            expectedPanLengthHex + pan + padding +
            expectedProcessingCodeLenHex + processingCode;
        assertEquals(expectedHex, HexService.bytesToHex(bytes));
        
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
        
        ValueHolder panFiller = ValueHolder.newInstance(bitmapPair).jumpToChild(PAN_02_NAME);
        assertNotNull(panFiller);
        
        String unpackedPanString = panFiller.getValue(String.class);
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

        fieldBuilder.setValidator(TestValidator.getInstance());
        fieldBuilder.validateStructure();
        // exception not thrown
    }
    
    @Test
    public void customFieldBuilderTest() {
        FieldBuilder testFieldBuilder = TestFieldBuilder.builder(MsgFieldType.TAG_VAL)
            .defineName("field");
        
        assertEquals(TestFieldBuilder.class, testFieldBuilder.getClass());
        assertEquals(TestValidator.class, testFieldBuilder.validator.getClass());

        testFieldBuilder.setValidator(TestValidator.getInstance());
        testFieldBuilder.validateStructure();
        // exception not thrown
    }
}
