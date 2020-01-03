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
            .getCurrentField();

        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineTagNum(2)
            .defineName(PAN_02_NAME)
            .defineStringer(StringStringer.getInstance())
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2));
        
        FieldBuilder.from(bitmap)
            .createChild(MsgFieldType.LEN_VAL)
            .defineTagNum(3)
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
        String expectedBitmapHex = "6000000000000040";
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
```

The message definition can be described as XML by calling the `DumpService.dumpMsgField(isoMsgField)` method
```XML
<f type="MSG" name="msg">
    <f type="VAL" name="mti" bodyPacker="BcdBodyPacker" len="2"/>
    <f type="BIT_SET" name="bitmap" bitMapPacker="IfbBitmapPacker" len="16">
        <f type="LEN_VAL" tagNum="2" name="PAN_02" lengthPacker="EbcdicDecimalLengthPacker" bodyPacker="BcdBodyPacker"/>
        <f type="LEN_VAL" tagNum="3" name="Processing_code_03" lengthPacker="BcdLengthPacker" bodyPacker="BcdBodyPacker"/>
        <f type="LEN_VAL" tagNum="58" name="field_58" lengthPacker="EbcdicDecimalLengthPacker" maxLen="999" childTagLen="2" childTagPacker="EbcdicDecimalTagPacker">
            <f type="LEN_TAG_VAL" tagNum="35" name="rate_request_reference" bodyPacker="AsciiBodyPacker"/>
            <f type="LEN_TAG_VAL" tagNum="37" name="dcc_data" bodyPacker="EbcdicBodyPacker" childTagLen="0">
                <f type="VAL" tagNum="1" name="dcc_status" bodyPacker="EbcdicBodyPacker" len="1"/>
                <f type="VAL" tagNum="2" name="currency_code" bodyPacker="EbcdicBodyPacker" len="3"/>
                <f type="VAL" tagNum="5" name="transaction_amount" bodyPacker="EbcdicBodyPacker" len="12"/>
                <f type="VAL" tagNum="17" name="conversion_rate" bodyPacker="EbcdicBodyPacker" len="8"/>
            </f>
            <f type="LEN_TAG_VAL" tagNum="53" name="non_loyalty_group" bodyPacker="EbcdicBodyPacker"/>
            <f type="LEN_TAG_VAL" tagNum="90" name="90" bodyPacker="EbcdicBodyPacker"/>
            <f type="LEN_TAG_VAL" tagNum="92" name="92" bodyPacker="EbcdicBodyPacker"/>
            <f type="LEN_TAG_VAL" tagNum="93" name="93" bodyPacker="EbcdicBodyPacker"/>
            <f type="LEN_TAG_VAL" tagNum="95" name="95" bodyPacker="EbcdicBodyPacker"/>
            <f type="LEN_TAG_VAL" tagNum="97" name="97" bodyPacker="EbcdicBodyPacker"/>
            <f type="LEN_TAG_VAL" tagNum="98" name="pos_terminal_capabilities" bodyPacker="EbcdicBodyPacker"/>
        </f>
    </f>
</f>
```

The message values can be described as XML by calling the `DumpService.dumpMsgValue(isoMsgField, msgValue, false)` method
```XML
<f name="msg">
    <f name="mti" val="0200" valHex="0200"/>
    <f name="bitmap" bitmapHex="6000000000000040">
        <f name="PAN_02" tagNum="2" val="123456781234567" lenHex="F0F8" valHex="123456781234567F"/>
        <f name="Processing_code_03" tagNum="3" val="32" lenHex="01" valHex="32"/>
        <f name="field_58" tagNum="58" lenHex="F0F6F9">
            <f name="rate_request_reference" tagNum="35" val="018F1AEE03E404843C" lenHex="F0F2F0" tagHex="F3F5" valHex="30313846...38343343"/>
            <f name="dcc_data" tagNum="37" lenHex="F0F2F6" tagHex="F3F7">
                <f name="dcc_status" tagNum="1" val="U" valHex="E4"/>
                <f name="currency_code" tagNum="2" val="978" valHex="F9F7F8"/>
                <f name="transaction_amount" tagNum="5" val="000000005555" valHex="F0F0F0F0...F5F5F5F5"/>
                <f name="conversion_rate" tagNum="17" val="40011670" valHex="F4F0F0F1F1F6F7F0"/>
            </f>
            <f name="non_loyalty_group" tagNum="53" val="003021" lenHex="F0F0F8" tagHex="F5F3" valHex="F0F0F3F0F2F1"/>
            <f name="pos_terminal_capabilities" tagNum="98" val="8" lenHex="F0F0F3" tagHex="F9F8" valHex="F8"/>
        </f>
    </f>
</f>
```

The complete example is located on GitHub [FieldBuilderTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/FieldBuilderTest.java).