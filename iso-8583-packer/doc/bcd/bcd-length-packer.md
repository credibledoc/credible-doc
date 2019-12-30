# `BcdLengthPacker` examples

Some MsgFields have defined the `LEN` subfield, see the [field-types.md](../field-types.md) description.

The following example shows how to define a field length in [BCD](https://en.wikipedia.org/wiki/Binary-coded_decimal) format
```Java
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(BcdLengthPacker.getInstance(2))
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
```

The field structure
```XML
<f type="LEN_VAL" lengthPacker="BcdLengthPacker" bodyPacker="BcdBodyPacker"/>
```

The following example shows packing and unpacking of the field value
```Java
            String value = "123";
            ValueHolder valueHolder = ValueHolder.newInstance(createField().getCurrentField());
            valueHolder.setValue(value);
    
            byte[] bytes = valueHolder.pack();
            String lengthHex = "0002";
            String valueHex = "0123";
            assertEquals(lengthHex + valueHex, HexService.bytesToHex(bytes));
    
            MsgValue msgValue = ValueHolder.unpack(bytes, 0, createField().getCurrentField());
            assertEquals(value, msgValue.getBodyValue(String.class));
```

The packed `FieldValue` with `lenHex` looks like
```XML
<f val="123" lenHex="0002" valHex="0123"/>
```

Some examples of packed values
```
Examples of integers packed with BcdLengthPacker class
numBytes: 1
numBytes '1', integer '1' packed as bytes 01
numBytes '1', integer '12' packed as bytes 12
numBytes '1', integer '123' cannot be packed, exception: The bodyBytesLength '123' cannot be packed to '1' bytes because it is longer and '2' bytes is needed for packing the '123' value.
numBytes '1', integer '1234' cannot be packed, exception: The bodyBytesLength '1234' cannot be packed to '1' bytes because it is longer and '2' bytes is needed for packing the '1234' value.
numBytes '1', integer '12345' cannot be packed, exception: The bodyBytesLength '12345' cannot be packed to '1' bytes because it is longer and '3' bytes is needed for packing the '12345' value.
numBytes '1', integer '123456' cannot be packed, exception: The bodyBytesLength '123456' cannot be packed to '1' bytes because it is longer and '3' bytes is needed for packing the '123456' value.
numBytes '1', integer '1234567' cannot be packed, exception: The bodyBytesLength '1234567' cannot be packed to '1' bytes because it is longer and '4' bytes is needed for packing the '1234567' value.
numBytes: 2
numBytes '2', integer '1' packed as bytes 0001
numBytes '2', integer '12' packed as bytes 0012
numBytes '2', integer '123' packed as bytes 0123
numBytes '2', integer '1234' packed as bytes 1234
numBytes '2', integer '12345' cannot be packed, exception: The bodyBytesLength '12345' cannot be packed to '2' bytes because it is longer and '3' bytes is needed for packing the '12345' value.
numBytes '2', integer '123456' cannot be packed, exception: The bodyBytesLength '123456' cannot be packed to '2' bytes because it is longer and '3' bytes is needed for packing the '123456' value.
numBytes '2', integer '1234567' cannot be packed, exception: The bodyBytesLength '1234567' cannot be packed to '2' bytes because it is longer and '4' bytes is needed for packing the '1234567' value.
numBytes: 3
numBytes '3', integer '1' packed as bytes 000001
numBytes '3', integer '12' packed as bytes 000012
numBytes '3', integer '123' packed as bytes 000123
numBytes '3', integer '1234' packed as bytes 001234
numBytes '3', integer '12345' packed as bytes 012345
numBytes '3', integer '123456' packed as bytes 123456
numBytes '3', integer '1234567' cannot be packed, exception: The bodyBytesLength '1234567' cannot be packed to '3' bytes because it is longer and '4' bytes is needed for packing the '1234567' value.
```

More examples see [complex-example.md](../complex-example.md).