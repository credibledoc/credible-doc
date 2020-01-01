# `HexLengthPacker` description and examples

Some MsgFields have defined the `LEN` subfield, see the [field-types.md](../field-types.md) description.

The following example shows how to define a field length in the `hex` format
```Java
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(HexLengthPacker.getInstance())
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
```

The field structure then looks like
```XML
<f type="LEN_VAL" lengthPacker="HexLengthPacker" bodyPacker="BcdBodyPacker"/>
```

The following example shows how to pack and unpack a value to the defined field
```Java
            String value = "123456789";
            ValueHolder valueHolder = ValueHolder.newInstance(createField().getCurrentField());
            valueHolder.setValue(value);
    
            byte[] bytes = valueHolder.pack();
            String lengthHex = "05";
            String valueHex = "0123456789";
            assertEquals(lengthHex + valueHex, HexService.bytesToHex(bytes));
    
            MsgValue msgValue = ValueHolder.unpack(bytes, 0, createField().getCurrentField());
            assertEquals(value, msgValue.getBodyValue(String.class));
```

The packed `FieldValue` with `lenHex` looks like
```XML
<f val="123456789" lenHex="05" valHex="0123456789"/>
```

Some examples of packed values
```
Examples of integers packed with HexLengthPacker class
numBytes: 1
numBytes '1', integer '1' packed as bytes 01
numBytes '1', integer '255' packed as bytes 81FF
numBytes '1', integer '12345' packed as bytes 823039
numBytes '1', integer '65535' packed as bytes 82FFFF
numBytes '1', integer '123456' packed as bytes 8201E240
numBytes '1', integer '123456789' packed as bytes 82075BCD15
numBytes '1', integer '1234567899' packed as bytes 82499602DB
numBytes '1', integer '2147483647' packed as bytes 827FFFFFFF
numBytes: 2
numBytes '2', integer '1' packed as bytes 01
numBytes '2', integer '255' packed as bytes 81FF
numBytes '2', integer '12345' packed as bytes 823039
numBytes '2', integer '65535' packed as bytes 82FFFF
numBytes '2', integer '123456' packed as bytes 8201E240
numBytes '2', integer '123456789' packed as bytes 82075BCD15
numBytes '2', integer '1234567899' packed as bytes 82499602DB
numBytes '2', integer '2147483647' packed as bytes 827FFFFFFF
numBytes: 3
numBytes '3', integer '1' packed as bytes 01
numBytes '3', integer '255' packed as bytes 81FF
numBytes '3', integer '12345' packed as bytes 823039
numBytes '3', integer '65535' packed as bytes 82FFFF
numBytes '3', integer '123456' packed as bytes 8201E240
numBytes '3', integer '123456789' packed as bytes 82075BCD15
numBytes '3', integer '1234567899' packed as bytes 82499602DB
numBytes '3', integer '2147483647' packed as bytes 827FFFFFFF
```

More examples see [complex-example.md](../complex-example.md).