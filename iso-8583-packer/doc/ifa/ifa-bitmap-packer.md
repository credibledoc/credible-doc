# `IfaBitmapPacker` examples

The following example shows how to define a field with `IFA` `bitmap` format
```Java
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
```

The defined structure can be shown as XML by calling the `DumpService.getInstance().dumpMsgValue(isoMsgField, msgValue, true)` method
```XML
    
    <f type="MSG" name="msg">
        <f type="VAL" name="MTI" bodyPacker="BcdBodyPacker" len="2"/>
        <f type="BIT_SET" name="BITMAP" bitMapPacker="IfaBitmapPacker">
            <f type="LEN_VAL" fieldNum="2" name="PAN" lengthPacker="EbcdicDecimalLengthPacker" bodyPacker="BcdBodyPacker"/>
        </f>
    </f>
```

The following example shows how to set a value to the `bitmap` child
```Java
        ValueHolder valueHolder = ValueHolder.newInstance(isoMsgField);

        String mtiValue = "0200";
        valueHolder.jumpToChild(MTI_NAME).setValue(mtiValue);

        String pan = "123456781234567";
        valueHolder.jumpToSibling(BITMAP_NAME)
            .jumpToChild(PAN_NAME).setValue(pan);

        // packing
        byte[] bytes = valueHolder.jumpToRoot().pack();
        String expectedBitmapHex = "34303030303030303030303030303030";
        String expectedPanLengthHex = "F0F8";
        char padding = BcdBodyPacker.FILLER_F;
        String expectedHex =
            mtiValue +
            expectedBitmapHex +
            expectedPanLengthHex + pan + padding;
        String packedHex = HexService.bytesToHex(bytes);
        assertEquals(expectedHex, packedHex);
```

The packed `MsgValue` then looks like
```XML
         
        <f name="msg">
            <f name="MTI" val="0200" valHex="0200"/>
            <f name="BITMAP" bitmapHex="34303030303030303030303030303030">
                <f name="PAN" fieldNum="2" val="123456781234567" lenHex="F0F8" valHex="123456781234567F"/>
            </f>
        </f>
```

The source of the test is located in GitHub [IfaBitmapPackerTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ifa/IfaBitmapPackerTest.java)

More examples see [complex-example.md](../complex-example.md).