package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgPair;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FieldBuilderTest {

    private static final String PAN_02_NAME = "PAN_02";
    private static final String BITMAP_NAME = "bitmap";
    private static final String MTI_NAME = "mti";

    /**
     * Used in documentation
     */
    @Test
    public void builder() {
        // definition
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("msg");

        MsgField root = fieldBuilder.getCurrentField();
        
        MsgField mti = FieldBuilder.builder(MsgFieldType.VAL)
            .defineName(MTI_NAME)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineLen(2)
            .defineParent(root)
            .getCurrentField();

        MsgField bitmap = FieldBuilder.from(mti)
            .crateSibling(MsgFieldType.BIT_SET)
            .defineName(BITMAP_NAME)
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance())
            .defineLen(16)
            .defineParent(root)
            .getCurrentField();
        
        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL_BIT_SET)
            .defineTagNum(2)
            .defineName(PAN_02_NAME)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.LL);
        
        fieldBuilder.validateStructure();

        // filling with data
        FieldFiller fieldFiller = FieldFiller.newInstance(root);
        
        String mtiValue = "0200";
        fieldFiller.jumpToChild(MTI_NAME).setValue(mtiValue);

        String pan = "123456781234567";
        fieldFiller.jumpToSibling(BITMAP_NAME)
            .jumpToChild(PAN_02_NAME).setValue(pan);
        
        // packing
        byte[] bytes = fieldFiller.jumpToRoot().pack();
        String expectedBitmapHex = "4000000000000000";
        String expectedLengthHex = "F0F8";
        char padding = BcdBodyPacker.FILLER_F;
        String expectedHex = mtiValue + expectedBitmapHex + expectedLengthHex + pan + padding;
        assertEquals(expectedHex, HexService.bytesToHex(bytes));
        
        // unpacking
        MsgValue msgValue = FieldFiller.unpack(bytes, 0, root);
        assertEquals(2, msgValue.getChildren().size());
        
        // data browsing
        MsgPair rootPair = FieldFiller.newInstance(msgValue, root).getCurrentPair();
        assertNotNull(rootPair);
        MsgPair bitmapPair = FieldFiller.newInstance(rootPair).jumpToChild(BITMAP_NAME).getCurrentPair();
        assertNotNull(bitmap);
        FieldFiller panFiller = FieldFiller.newInstance(bitmapPair).jumpToChild(PAN_02_NAME);
        assertNotNull(panFiller);
        String unpackedPanString = panFiller.getValue(String.class);
        assertEquals(pan, unpackedPanString);
    }
}
