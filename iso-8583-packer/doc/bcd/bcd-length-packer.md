# BcdLengthPacker examples

Some MsgFields have defined the `LEN` subfield, for example
```
LEN VAL
 02 123
```
or
```
TAG LEN VAL
 60  02 543
```
or
```
LEN TAG VAL
 03  60 543
```

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
            FieldFiller fieldFiller = FieldFiller.newInstance(createField().getCurrentField());
            fieldFiller.setValue(value);
            assertEquals(value, fieldFiller.getValue(String.class));
    
            byte[] bytes = fieldFiller.pack();
            String lengthHex = "0002";
            String valueHex = "0123";
            assertEquals(lengthHex + valueHex, HexService.bytesToHex(bytes));
    
            MsgValue msgValue = FieldFiller.unpack(bytes, 0, createField().getCurrentField());
            assertEquals(value, msgValue.getBodyValue(String.class));
```

The packed `FieldValue` with `lenHex` looks like
```XML
<f val="123" lenHex="0002" bytesHex="0123"/>
```

More examples see [complex-example.md](../complex-example.md).