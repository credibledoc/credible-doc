package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.bcd.BcdLengthPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgPair;
import com.credibledoc.iso8583packer.message.MsgValue;
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

        String msgFieldDump = DumpService.dumpMsgField(isoMsgField);
        logger.info("Root msgField dump: \n{}{}", msgFieldDump, "End of msgField dump.");

        String msgValueDump = DumpService.dumpMsgValue(isoMsgField, msgValue, false);
        logger.info("Root msgValue dump: \n{}{}", msgValueDump, "End of msgValue dump.");
    }
}
