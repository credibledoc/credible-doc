# `BinaryLengthPacker` description and examples

Some MsgFields have defined the `LEN` subfield, see the [field-types.md](../field-types.md) description.

The following example shows how to define a field length in the binary format
```Java
            FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineHeaderLengthPacker(BinaryLengthPacker.getInstance(1))
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
```

The field structure then looks like the next example
```XML
<f type="LEN_VAL" lengthPacker="BinaryLengthPacker" bodyPacker="BcdBodyPacker"/>
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

The packed `FieldValue` with `lenHex` looks like the next example
```XML
<f val="123456789" lenHex="05" valHex="0123456789"/>
```

Some examples of packed values
```
Examples of integers packed with BinaryLengthPacker class
numBytes: 1
numBytes '1', integer '1' packed as bytes 01
numBytes '1', integer '255' packed as bytes FF
numBytes '1', integer '12345' cannot be packed, exception: The bodyBytesLength '12345' cannot be packed in bytes because it is greater than the maximum value '255' that can be packed in the bytes with the length '1'.
numBytes '1', integer '65535' cannot be packed, exception: The bodyBytesLength '65535' cannot be packed in bytes because it is greater than the maximum value '255' that can be packed in the bytes with the length '1'.
numBytes '1', integer '123456' cannot be packed, exception: The bodyBytesLength '123456' cannot be packed in bytes because it is greater than the maximum value '255' that can be packed in the bytes with the length '1'.
numBytes '1', integer '123456789' cannot be packed, exception: The bodyBytesLength '123456789' cannot be packed in bytes because it is greater than the maximum value '255' that can be packed in the bytes with the length '1'.
numBytes: 2
numBytes '2', integer '1' packed as bytes 0001
numBytes '2', integer '255' packed as bytes 00FF
numBytes '2', integer '12345' packed as bytes 3039
numBytes '2', integer '65535' packed as bytes FFFF
numBytes '2', integer '123456' cannot be packed, exception: The bodyBytesLength '123456' cannot be packed in bytes because it is greater than the maximum value '65535' that can be packed in the bytes with the length '2'.
numBytes '2', integer '123456789' cannot be packed, exception: The bodyBytesLength '123456789' cannot be packed in bytes because it is greater than the maximum value '65535' that can be packed in the bytes with the length '2'.
numBytes: 3
numBytes '3', integer '1' packed as bytes 000001
numBytes '3', integer '255' packed as bytes 0000FF
numBytes '3', integer '12345' packed as bytes 003039
numBytes '3', integer '65535' packed as bytes 00FFFF
numBytes '3', integer '123456' packed as bytes 01E240
numBytes '3', integer '123456789' cannot be packed, exception: The bodyBytesLength '123456789' cannot be packed in bytes because it is greater than the maximum value '16777215' that can be packed in the bytes with the length '3'.
```

More examples see [complex-example.md](../complex-example.md).