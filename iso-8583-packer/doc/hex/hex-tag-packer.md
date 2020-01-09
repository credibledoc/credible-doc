# `HexTagPacker` examples

Some [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
with [MsgValue](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgValue.java)
pairs contain `TAG` subfields, see the [field-types.md](../field-types.md) page with description of field types.

The following example shows how to define `TAG` in the [HEX](https://en.wikipedia.org/wiki/Hexadecimal) format
```Java
    private FieldBuilder createField() {
        return FieldBuilder.builder(MsgFieldType.MSG)
            .defineChildrenTagPacker(HexTagPacker.getInstance(1))
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
<f type="MSG" name="root" childTagPacker="HexTagPacker(1)">
    <f type="TAG_VAL" tag="1" name="field_1" bodyPacker="BcdBodyPacker" len="2"/>
</f>
```

The following example shows packing and unpacking of the field value
```Java
        String value = "1234";
        valueHolder.setValue(value).validateData();
        byte[] bytes = valueHolder.pack();
        String tagHex = "01";
        String valueHex = "1234";
        assertEquals(tagHex + valueHex, HexService.bytesToHex(bytes));

        MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
        ValueHolder valueHolderUnpacked = ValueHolder.newInstance(msgValue, fieldBuilder.getCurrentField());
        String unpackedValue = valueHolderUnpacked.getValue(String.class);
        assertEquals(value, unpackedValue);
```

The packed `FieldValue` with `TAG` looks like the next example
```XML
<f name="root">
    <f name="field_1" tag="1" val="1234" tagHex="01" valHex="1234"/>
</f>
```

Some examples of packed values
```
Examples of integers packed with HexTagPacker class
numBytes: 1
numBytes '1', integer '1' packed as bytes 01
numBytes '1', integer '15' packed as bytes 0F
numBytes '1', integer '16' packed as bytes 10
numBytes '1', integer '1122' cannot be packed, exception: Packed bytes with length '2' is greater than required tagLength '1'.
numBytes '1', integer '112233' cannot be packed, exception: Packed bytes with length '3' is greater than required tagLength '1'.
numBytes '1', integer '1122334455' cannot be packed, exception: Packed bytes with length '4' is greater than required tagLength '1'.
numBytes: 2
numBytes '2', integer '1' packed as bytes 0001
numBytes '2', integer '15' packed as bytes 000F
numBytes '2', integer '16' packed as bytes 0010
numBytes '2', integer '1122' packed as bytes 0462
numBytes '2', integer '112233' cannot be packed, exception: Packed bytes with length '3' is greater than required tagLength '2'.
numBytes '2', integer '1122334455' cannot be packed, exception: Packed bytes with length '4' is greater than required tagLength '2'.
numBytes: 3
numBytes '3', integer '1' packed as bytes 000001
numBytes '3', integer '15' packed as bytes 00000F
numBytes '3', integer '16' packed as bytes 000010
numBytes '3', integer '1122' packed as bytes 000462
numBytes '3', integer '112233' packed as bytes 01B669
numBytes '3', integer '1122334455' cannot be packed, exception: Packed bytes with length '4' is greater than required tagLength '3'.
```

More examples see [complex-example.md](../complex-example.md).