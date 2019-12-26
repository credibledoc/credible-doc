# Complex example of the `MsgField` structure definition

The next example contains definition of ISO 8583 message

```Java
            FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.BIT_SET)
                .defineName("root")
                .defineHeaderBitMapPacker(IfbBitmapPacker.L16);
            
            MsgField root = fieldBuilder.getCurrentField();
            
            FieldBuilder.from(root)
                .createChild(MsgFieldType.LEN_VAL_BIT_SET)
                .defineTagNum(2)
                .defineName(PAN_02_NAME)
                .defineBodyPacker(BcdBodyPacker.RIGHT_PADDED_F)
                .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.LL);
            
            fieldBuilder.validateStructure();
    
            // filling with data
            String pan = "123456781234567";
            FieldFiller fieldFiller = FieldFiller.from(root)
                .jumpToChild(PAN_02_NAME).setValue(pan);
            
            // packing
            byte[] bytes = fieldFiller.jumpToRoot().pack();
            String expectedBitmapHex = "4000000000000000";
            String expectedLengthHex = "F0F8";
            char padding = BcdBodyPacker.PADDING_F;
            String expectedHex = expectedBitmapHex + expectedLengthHex + pan + padding;
            assertEquals(expectedHex, HexService.hexString(bytes));
            
            // unpacking
            MsgValue msgValue = FieldFiller.unpack(bytes, 0, root);
            assertEquals(1, msgValue.getChildren().size());
            
            // data browsing
            Object unpackedPan = msgValue.getChildren().get(0).getBodyValue();
            assertEquals(pan, unpackedPan);
            
            // another approach of data browsing
            FieldFiller filler = FieldFiller.get(msgValue, root);
            String unpackedPanString = filler
                .jumpToChild(PAN_02_NAME)
                .getBodyValue(String.class);
            assertEquals(pan, unpackedPanString);
```