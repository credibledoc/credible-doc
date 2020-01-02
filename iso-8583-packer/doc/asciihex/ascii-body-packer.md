# `AsciiBodyPacker` examples

The following example shows how to define a field with body in [ASCII](https://en.wikipedia.org/wiki/ASCII) format
```Java
    private FieldBuilder fixedLengthAscii() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineLen(2)
            .defineBodyPacker(AsciiBodyPacker.getInstance());
    }
```

The field structure
```XML

<f type="VAL" bodyPacker="AsciiBodyPacker" len="2"/>
```

The following example shows how to pack the field value
```Java
            FieldBuilder fieldBuilder = fixedLengthAscii();
            fieldBuilder.validateStructure();
    
            String value = "ab";
            ValueHolder valueHolder =
                ValueHolder.newInstance(fieldBuilder.getCurrentField())
                .setValue(value);
            
            valueHolder.validateData();
    
            byte[] valueBytes = valueHolder.pack();
            String bytesHex = HexService.bytesToHex(valueBytes);
            assertEquals("6162", bytesHex);
```

And the following example shows how to unpack the field bytes to a value
```Java
            byte[] bytes = HexService.hex2byte("6162");
            FieldBuilder fieldBuilder = fixedLengthAscii();
            MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
            assertEquals("ab", msgValue.getBodyValue(String.class));
```

The packed `FieldValue` then looks like
```XML

<f val="ab" valHex="6162"/>
```

Some examples of packed values
```
Examples of values packed with AsciiBodyPacker
Value 'a' packed to bytes as hex: 61
Value 'ab' packed to bytes as hex: 6162
Value 'abc' packed to bytes as hex: 616263
Value 'A 1234 bcd' packed to bytes as hex: 41203132333420626364
```

The source of the test is located in GitHub [AsciiBodyPackerTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/asciihex/AsciiBodyPackerTest.java)

More examples see [complex-example.md](../complex-example.md).