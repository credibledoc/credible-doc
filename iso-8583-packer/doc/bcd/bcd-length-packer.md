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
<f val="123" lenHex="0002" valHex="0123"/>
```

Some examples of packed values
```
Examples of integers packed with BcdLengthPacker class
numBytes: 1
Integer '1' packed as bytes '01'
Integer '15' packed as bytes '15'
Integer '16' packed as bytes '16'
Integer '17' packed as bytes '17'
Integer '98' packed as bytes '98'
Integer '123' packed as bytes 'cannot be packed, exception thrown'
numBytes: 2
Integer '1' packed as bytes '0001'
Integer '15' packed as bytes '0015'
Integer '16' packed as bytes '0016'
Integer '17' packed as bytes '0017'
Integer '98' packed as bytes '0098'
Integer '123' packed as bytes '0123'
numBytes: 3
Integer '1' packed as bytes '000001'
Integer '15' packed as bytes '000015'
Integer '16' packed as bytes '000016'
Integer '17' packed as bytes '000017'
Integer '98' packed as bytes '000098'
Integer '123' packed as bytes '000123'
```

More examples see [complex-example.md](../complex-example.md).