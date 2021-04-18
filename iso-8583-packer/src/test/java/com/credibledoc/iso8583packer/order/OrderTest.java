package com.credibledoc.iso8583packer.order;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.message.IsoMsg;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import org.junit.Assert;
import org.junit.Test;

public class OrderTest {

    @Test
    public void orderTest() {

        String ROOT = "root";
        String FIRST = "first";
        String THIRD = "third";
        String SECOND = "second";
        
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName(ROOT)
            
            .createChild(MsgFieldType.VAL)
            .defineName(FIRST)
            .defineLen(1)
            .defineBodyPacker(BcdBodyPacker.noPadding())

            .cloneToSibling()
            .defineName(SECOND)

            .cloneToSibling()
            .defineName(THIRD)
            
            .validateStructure();

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder, true);
        valueHolder.setValue("11", ROOT, FIRST);
        valueHolder.setValue("33", ROOT, THIRD);
        valueHolder.setValue("22", ROOT, SECOND);
        
        byte[] packed = valueHolder.pack();
        String expectedHex = "112233";
        Assert.assertEquals(expectedHex, HexService.bytesToHex(packed));
    }

    @Test
    public void orderNumsTest() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.BIT_SET)
            .defineName("BIT_SET")
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance(24))

            .createChild(MsgFieldType.VAL)
            .defineFieldNum(1)
            .defineName("1")
            .defineLen(1)
            .defineBodyPacker(BcdBodyPacker.noPadding())

            .cloneToSibling()
            .defineFieldNum(133)
            .defineName("3")

            .cloneToSibling()
            .defineFieldNum(22)
            .defineName("2")

            .validateStructure();
        
        IsoMsg isoMsg = new IsoMsg();
        isoMsg.setPackager(fieldBuilder.getCurrentField());

        isoMsg.set(1, "11");
        isoMsg.set(133, "33");
        isoMsg.set(22, "22");
        
        byte[] packed = isoMsg.pack();
        String expectedBitSetHex = "800004000000000080000000000000000800000000000000";
        String expectedValueHex = "112233";
        Assert.assertEquals(expectedBitSetHex + expectedValueHex, HexService.bytesToHex(packed));
    }
}
