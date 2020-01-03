# `EbcdicDecimalLengthPacker` examples

Some [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
with [MsgValue](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgValue.java)
pairs contain `LEN` subfields, see the [field-types.md](../field-types.md) page with description of field types.

The following example shows how to define a field length in the [EBCDIC](https://en.wikipedia.org/wiki/EBCDIC) format
```Java
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2))
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
```

The field structure
```XML
<f type="LEN_VAL" lengthPacker="EbcdicDecimalLengthPacker" bodyPacker="BcdBodyPacker"/>
```

The following example shows packing and unpacking of the field value
```Java
            String value = "123";
            ValueHolder valueHolder = ValueHolder.newInstance(createField().getCurrentField());
            valueHolder.setValue(value);
    
            byte[] bytes = valueHolder.pack();
            String lengthHex = "F0F2";
            String valueHex = "0123";
            assertEquals(lengthHex + valueHex, HexService.bytesToHex(bytes));
    
            MsgValue msgValue = ValueHolder.unpack(bytes, 0, createField().getCurrentField());
            assertEquals(value, msgValue.getBodyValue(String.class));
```

The packed `FieldValue` with `lenHex` looks like
```XML
<f val="123" lenHex="F0F2" valHex="0123"/>
```

Some examples of packed values
```
Examples of integers packed with EbcdicDecimalLengthPacker class
numBytes: 1
numBytes '1', integer '1' packed as bytes F1
numBytes '1', integer '12' cannot be packed, exception: The bodyBytesLength '12' cannot be packed to '1' bytes because it is longer and '2' bytes is needed for packing the 'F1F2' value.
numBytes '1', integer '123' cannot be packed, exception: The bodyBytesLength '123' cannot be packed to '1' bytes because it is longer and '3' bytes is needed for packing the 'F1F2F3' value.
numBytes '1', integer '1234' cannot be packed, exception: The bodyBytesLength '1234' cannot be packed to '1' bytes because it is longer and '4' bytes is needed for packing the 'F1F2F3F4' value.
numBytes: 2
numBytes '2', integer '1' packed as bytes F0F1
numBytes '2', integer '12' packed as bytes F1F2
numBytes '2', integer '123' cannot be packed, exception: The bodyBytesLength '123' cannot be packed to '2' bytes because it is longer and '3' bytes is needed for packing the 'F1F2F3' value.
numBytes '2', integer '1234' cannot be packed, exception: The bodyBytesLength '1234' cannot be packed to '2' bytes because it is longer and '4' bytes is needed for packing the 'F1F2F3F4' value.
numBytes: 3
numBytes '3', integer '1' packed as bytes F0F0F1
numBytes '3', integer '12' packed as bytes F0F1F2
numBytes '3', integer '123' packed as bytes F1F2F3
numBytes '3', integer '1234' cannot be packed, exception: The bodyBytesLength '1234' cannot be packed to '3' bytes because it is longer and '4' bytes is needed for packing the 'F1F2F3F4' value.
```

More examples see [complex-example.md](../complex-example.md).