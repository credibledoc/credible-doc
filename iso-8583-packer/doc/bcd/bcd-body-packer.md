# `BcdBodyPacker` examples

The following example shows how to define a field with body in [BCD](https://en.wikipedia.org/wiki/Binary-coded_decimal) format
```Java
    private FieldBuilder fixedLengthBcd() {
        return
            FieldBuilder.builder(MsgFieldType.VAL)
                .defineLen(2)
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
    }
```

The field structure
```XML

<f type="VAL" bodyPacker="BcdBodyPacker" len="2"/>
```

The following example shows how to pack the field value
```Java
            FieldBuilder fieldBuilder = fixedLengthBcd();
            fieldBuilder.validateStructure();
    
            String value = "123";
            ValueHolder valueHolder =
                ValueHolder.newInstance(fieldBuilder.getCurrentField())
                .setValue(value);
            
            valueHolder.validateData();
    
            byte[] valueBytes = valueHolder.pack();
            String bytesHex = HexService.bytesToHex(valueBytes);
            assertEquals("0123", bytesHex);
```

The packed `FieldValue` then looks like
```XML

<f val="123" valHex="0123"/>
```

Some examples of packed values
```
Examples of values packed with BcdBodyPacker 
BodyPacker type 'left padding with 0'
Value '1' packed to bytes as hex: 01
Value '12' packed to bytes as hex: 12
Value '123' packed to bytes as hex: 0123
Value '1234' packed to bytes as hex: 1234
BodyPacker type 'left padding with F'
Value '1' packed to bytes as hex: F1
Value '12' packed to bytes as hex: 12
Value '123' packed to bytes as hex: F123
Value '1234' packed to bytes as hex: 1234
BodyPacker type 'right padding with F'
Value '1' packed to bytes as hex: 1F
Value '12' packed to bytes as hex: 12
Value '123' packed to bytes as hex: 123F
Value '1234' packed to bytes as hex: 1234
BodyPacker type 'no padding'
Value '1' cannot be packed to bytes. Exception: Odd value length is not allowed with 'noPadding()' instance. Value '1' has odd length '1'. Please use even length value or another instance of the BcdBodyPacker class.
Value '12' packed to bytes as hex: 12
Value '123' cannot be packed to bytes. Exception: Odd value length is not allowed with 'noPadding()' instance. Value '123' has odd length '3'. Please use even length value or another instance of the BcdBodyPacker class.
Value '1234' packed to bytes as hex: 1234
```

The source of the test is located in GitHub [BcdBodyPackerTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdBodyPackerTest.java)

More examples see [complex-example.md](../complex-example.md).