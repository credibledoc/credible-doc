# `IfaBitmapPackerExtendedTest` examples


The following example shows how to define a field with `IFA` `bitmap` format
```Java
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
```

The defined structure can be shown as XML by calling the `DumpService.getInstance().dumpMsgValue(isoMsgField, msgValue, true)` method
```XML
    
    <f type="MSG" name="MSG">
        <f type="VAL" name="MTI" bodyPacker="BcdBodyPacker" len="2"/>
        <f type="BIT_SET" name="BITMAP" bitMapPacker="IfaBitmapPacker">
            <f type="LEN_VAL" fieldNum="2" name="PAN" lengthPacker="EbcdicDecimalLengthPacker" bodyPacker="BcdBodyPacker"/>
            <f type="LEN_VAL" fieldNum="66" name="SettlementCode" lengthPacker="EbcdicDecimalLengthPacker" bodyPacker="BcdBodyPacker"/>
            <f type="LEN_VAL" fieldNum="130" name="TertiaryField" lengthPacker="EbcdicDecimalLengthPacker" bodyPacker="BcdBodyPacker"/>
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
```

The packed `MsgValue` then looks like the next example
```XML
         
        <f name="MSG">
            <f name="MTI" val="0200"/>
            <f name="BITMAP" bitmapHex="433030303030303030303030303030304330303030303030303030303030303034303030303030303030303030303030" bitSet="{1, 2, 65, 66, 130}">
                <f name="PAN" fieldNum="2" val="123456781234567" lenHex="F0F8" valHex="123456781234567F"/>
                <f name="SettlementCode" fieldNum="66" val="2222" lenHex="F0F2"/>
                <f name="TertiaryField" fieldNum="130" val="3333" lenHex="F0F2"/>
            </f>
        </f>
```

The source of the test is located in GitHub [IfaBitmapPackerExtendedTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ifa/IfaBitmapPackerExtendedTest.java)

More examples see [complex-example.md](../complex-example.md).