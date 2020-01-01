# Complex example of the `MsgField` structure definition

The following example contains the definition of an ISO 8583 message

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
        
        ValueHolder panValueHolder = ValueHolder.newInstance(bitmapPair).jumpToChild(PAN_02_NAME);
        assertNotNull(panValueHolder);
        
        String unpackedPanString = panValueHolder.getValue(String.class);
        assertEquals(pan, unpackedPanString);
```

The message definition can be described as XML by calling the `DumpService.dumpMsgField(isoMsgField)` method
```XML
<f type="MSG" name="msg">
    <f type="VAL" name="mti" bodyPacker="BcdBodyPacker" len="2"/>
    <f type="BIT_SET" name="bitmap" bitMapPacker="IfbBitmapPacker" len="16">
        <f type="LEN_VAL" tagNum="2" name="PAN_02" lengthPacker="EbcdicDecimalLengthPacker" bodyPacker="BcdBodyPacker"/>
        <f type="LEN_VAL" tagNum="3" name="Processing_code_03" lengthPacker="BcdLengthPacker" bodyPacker="BcdBodyPacker"/>
    </f>
</f>
```

The message values can be described as XML by calling the `DumpService.dumpMsgValue(isoMsgField, msgValue, false)` method
```XML
<f name="msg">
    <f name="mti" val="0200" valHex="0200"/>
    <f name="bitmap" bitmapHex="6000000000000000">
        <f name="PAN_02" tagNum="2" val="123456781234567" lenHex="F0F8" valHex="123456781234567F"/>
        <f name="Processing_code_03" tagNum="3" val="32" lenHex="01" valHex="32"/>
    </f>
</f>
```