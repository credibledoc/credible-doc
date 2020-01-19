# `LiteralTagPacker` examples

Some [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
with [MsgValue](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgValue.java)
pairs contain `TAG` subfields, see the [field-types.md](../field-types.md) page with description of field types.

The following example shows how to define `TAG` in the `Literal` format
```Java
    private FieldBuilder createField() {
        return FieldBuilder.builder(MsgFieldType.MSG)
            .defineChildrenTagPacker(LiteralTagPacker.getInstance(1))
            .defineName("root")
            
            .createChild(MsgFieldType.TAG_VAL)
            .defineName(FIELD_1_NAME)
            .defineHeaderTag("01")
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .jumpToRoot()
            .validateStructure();
    }
```

The field structure
```XML
<f type="MSG" name="root" childTagPacker="LiteralTagPacker(1)">
    <f type="TAG_VAL" tag="01" name="field_1" bodyPacker="BcdBodyPacker" len="2"/>
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
    <f name="field_1" tag="01" val="1234" tagHex="01"/>
</f>
```

Some examples of packed values
```
Examples of Strings packed with LiteralTagPacker class
numBytes: 1
numBytes '1', String '01' packed as bytes 01
numBytes '1', String '0015' cannot be packed, exception: Packed bytes with length '2' is greater than required tagLength '1'.
numBytes '1', String 'F2C90A' cannot be packed, exception: Packed bytes with length '3' is greater than required tagLength '1'.
numBytes '1', String 'FFF5D233' cannot be packed, exception: Packed bytes with length '4' is greater than required tagLength '1'.
numBytes: 2
numBytes '2', String '01' packed as bytes 0001
numBytes '2', String '0015' packed as bytes 0015
numBytes '2', String 'F2C90A' cannot be packed, exception: Packed bytes with length '3' is greater than required tagLength '2'.
numBytes '2', String 'FFF5D233' cannot be packed, exception: Packed bytes with length '4' is greater than required tagLength '2'.
numBytes: 3
numBytes '3', String '01' packed as bytes 000001
numBytes '3', String '0015' packed as bytes 000015
numBytes '3', String 'F2C90A' packed as bytes F2C90A
numBytes '3', String 'FFF5D233' cannot be packed, exception: Packed bytes with length '4' is greater than required tagLength '3'.
```

More examples see [complex-example.md](../complex-example.md).