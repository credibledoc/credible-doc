package com.credibledoc.iso8583packer.order;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import org.junit.Assert;
import org.junit.Test;

public class OrderTest {

    private static final String ROOT = "root";

    @Test
    public void orderTest() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName(ROOT)
            
            .createChild(MsgFieldType.VAL)
            .defineName("first")
            .defineLen(1)
            .defineBodyPacker(BcdBodyPacker.noPadding())

            .cloneToSibling()
            .defineName("second")

            .cloneToSibling()
            .defineName("third")
            
            .validateStructure();

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder, true);
        valueHolder.setValue("11", ROOT, "first");
        valueHolder.setValue("33", ROOT, "third");
        valueHolder.setValue("22", ROOT, "second");
        
        byte[] packed = valueHolder.pack();
        String expectedHex = "112233";
        Assert.assertEquals(expectedHex, HexService.bytesToHex(packed));
    }
}
