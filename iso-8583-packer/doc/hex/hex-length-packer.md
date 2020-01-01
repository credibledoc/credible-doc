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
integer '1' packed as bytes 01
integer '255' packed as bytes 81FF
integer '12345' packed as bytes 823039
integer '65535' packed as bytes 82FFFF
integer '123456' cannot be packed, exception: Body bytes length '123456' is greater than '65535' bytes
```

More examples see [complex-example.md](../complex-example.md).