# `AsciiLengthPacker` examples

Some MsgFields have defined the `LEN` subfield, see the [field-types.md](../field-types.md) description.

The following example shows how to define a field length in ASCII format
```Java
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(AsciiLengthPacker.getInstance(2))
                .defineBodyPacker(AsciiBodyPacker.getInstance());
```

The field structure
```XML
<f type="LEN_VAL" lengthPacker="AsciiLengthPacker" bodyPacker="AsciiBodyPacker"/>
```

The following example shows packing and unpacking of the field value
```Java
            String value = "abc";
            ValueHolder valueHolder = ValueHolder.newInstance(createField().getCurrentField());
            valueHolder.setValue(value);
    
            byte[] bytes = valueHolder.pack();
            String lengthHex = "3033";
            String valueHex = "616263";
            assertEquals(lengthHex + valueHex, HexService.bytesToHex(bytes));
    
            MsgValue msgValue = ValueHolder.unpack(bytes, 0, createField().getCurrentField());
            assertEquals(value, msgValue.getBodyValue(String.class));
```

The packed `FieldValue` with `lenHex` looks like the next example
```XML
<f val="abc" lenHex="3033" valHex="616263"/>
```

Some examples of packed values
```
Examples of integers packed with AsciiLengthPacker class
numBytes: 1
numBytes '1', integer '1' packed as bytes 31
numBytes '1', integer '12' cannot be packed, exception: Cannot pack bodyBytesLength '12' to a byte array with length '1' bytes because the value requires '2' bytes for packing.
numBytes '1', integer '123' cannot be packed, exception: Cannot pack bodyBytesLength '123' to a byte array with length '1' bytes because the value requires '3' bytes for packing.
numBytes '1', integer '1234' cannot be packed, exception: Cannot pack bodyBytesLength '1234' to a byte array with length '1' bytes because the value requires '4' bytes for packing.
numBytes: 2
numBytes '2', integer '1' packed as bytes 3031
numBytes '2', integer '12' packed as bytes 3132
numBytes '2', integer '123' cannot be packed, exception: Cannot pack bodyBytesLength '123' to a byte array with length '2' bytes because the value requires '3' bytes for packing.
numBytes '2', integer '1234' cannot be packed, exception: Cannot pack bodyBytesLength '1234' to a byte array with length '2' bytes because the value requires '4' bytes for packing.
numBytes: 3
numBytes '3', integer '1' packed as bytes 303031
numBytes '3', integer '12' packed as bytes 303132
numBytes '3', integer '123' packed as bytes 313233
numBytes '3', integer '1234' cannot be packed, exception: Cannot pack bodyBytesLength '1234' to a byte array with length '3' bytes because the value requires '4' bytes for packing.
```

More examples see [complex-example.md](../complex-example.md).