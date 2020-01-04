# `EbcdicDecimalTagPacker` examples

Some [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
with [MsgValue](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgValue.java)
pairs contain `TAG` subfields, see the [field-types.md](../field-types.md) page with description of field types.

The following example shows how to define `TAG` in the [EBCDIC](https://en.wikipedia.org/wiki/EBCDIC) format
```Java
    private FieldBuilder createField() {
        return FieldBuilder.builder(MsgFieldType.MSG)
            .defineChildrenTagPacker(EbcdicDecimalTagPacker.getInstance(1))
            .defineName("root")
            
            .createChild(MsgFieldType.TAG_VAL)
            .defineName(FIELD_1_NAME)
            .defineHeaderTag(1)
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .jumpToRoot()
            .validateStructure();
    }
```

The field structure
```XML
<f type="MSG" name="root" childTagPacker="EbcdicDecimalTagPacker(1)">
    <f type="TAG_VAL" tag="1" name="field_1" bodyPacker="BcdBodyPacker" len="2"/>
</f>
```

The following example shows packing and unpacking of the field value
```Java
        String value = "1234";
        MsgField field1 = createField().jumpToChild(FIELD_1_NAME).getCurrentField();
        ValueHolder valueHolder = ValueHolder.newInstance(field1);
        valueHolder.setValue(value);

        byte[] bytes = valueHolder.pack();
        String tagHex = "F1";
        String valueHex = "1234";
        assertEquals(tagHex + valueHex, HexService.bytesToHex(bytes));

        MsgField msgField = createField().getCurrentField();
        MsgValue msgValue = ValueHolder.unpack(bytes, 0, msgField);
        ValueHolder valueHolderUnpacked = ValueHolder.newInstance(msgValue, msgField);
        valueHolderUnpacked.jumpToChild(FIELD_1_NAME);
        String unpackedValue = valueHolderUnpacked.getValue(String.class);
        assertEquals(value, unpackedValue);
```

The packed `FieldValue` with `TAG` looks like
```XML
<f name="root">
    <f name="field_1" tag="1" val="1234" tagHex="F1" valHex="1234"/>
</f>
```

Some examples of packed values
```
Examples of integers packed with EbcdicDecimalTagPacker class
numBytes: 1
numBytes '1', integer '1' packed as bytes F1
numBytes '1', integer '12' cannot be packed, exception: Packed bytes with length '2' is greater than required packedLength '1'.
numBytes '1', integer '123' cannot be packed, exception: Packed bytes with length '3' is greater than required packedLength '1'.
numBytes '1', integer '1234' cannot be packed, exception: Packed bytes with length '4' is greater than required packedLength '1'.
numBytes: 2
numBytes '2', integer '1' packed as bytes F0F1
numBytes '2', integer '12' packed as bytes F1F2
numBytes '2', integer '123' cannot be packed, exception: Packed bytes with length '3' is greater than required packedLength '2'.
numBytes '2', integer '1234' cannot be packed, exception: Packed bytes with length '4' is greater than required packedLength '2'.
numBytes: 3
numBytes '3', integer '1' packed as bytes F0F0F1
numBytes '3', integer '12' packed as bytes F0F1F2
numBytes '3', integer '123' packed as bytes F1F2F3
numBytes '3', integer '1234' cannot be packed, exception: Packed bytes with length '4' is greater than required packedLength '3'.
```

More examples see [complex-example.md](../complex-example.md).