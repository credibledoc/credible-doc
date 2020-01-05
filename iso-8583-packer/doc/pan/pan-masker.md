# `PanMasker` examples

The implementation of the [masker.md](../masking/masker.md) interface replaces the PAN with wildcards except the first few digits.

The following example shows how to define a `Masker`.
```Java
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.VAL)
            .defineName("PAN_field")
            .defineLen(10)
            .defineBodyPacker(BcdBodyPacker.rightPaddingF())
            .defineMasker(PanMasker.getInstance())
            .validateStructure();

        MsgField msgField = fieldBuilder.getCurrentField();
        
        ValueHolder valueHolder = ValueHolder.newInstance(msgField);
        String pan = "1234567890123456789";
        valueHolder.setValue(pan);

        MsgValue msgValue = valueHolder.getCurrentMsgValue();
```

The following example shows how to use the `Masker`.
```Java
        DumpService dumpService = DumpService.getInstance();
        String msgFieldStructure = dumpService.dumpMsgField(msgField);
        String msgValueStructure = dumpService.dumpMsgValue(msgField, msgValue, false);
        String maskedMsgValueStructure = dumpService.dumpMsgValue(msgField, msgValue, true);
```

The following example shows the field structure, field value without masking and the field value with masking
```XML
        <f type="VAL" name="PAN_field" bodyPacker="BcdBodyPacker" len="10"/>
        
        MsgValue without masking
        <f name="PAN_field" val="1234567890123456789" valHex="1234567890123456789F"/>
        
        Masked MsgValue
        <f name="PAN_field" val="1234***************" valHex="1234****************"/>
```

More examples see [complex-example.md](../complex-example.md).