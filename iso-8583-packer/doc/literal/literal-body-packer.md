# `LiteralBodyPacker` examples

The following example shows how to define a field with body in the `Literal` format
```Java
    private FieldBuilder fixedLengthLiteral() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineBodyPacker(LiteralBodyPacker.getInstance())
            .defineStringer(BinaryToHexStringer.getInstance())
            .defineLen(2);
    }
```

The field structure
```XML

<f type="VAL" bodyPacker="LiteralBodyPacker" len="2"/>
```

The following example shows how to pack the field value
```Java
            FieldBuilder fieldBuilder = fixedLengthLiteral();
            fieldBuilder.validateStructure();
    
            byte[] value = HexService.hex2byte("FFFF");
            ValueHolder valueHolder =
                ValueHolder.newInstance(fieldBuilder.getCurrentField())
                .setValue(value);
            
            valueHolder.validateData();
    
            byte[] valueBytes = valueHolder.pack();
            assertArrayEquals(value, valueBytes);
```

And the following example shows how to unpack the field bytes to a value
```Java
            byte[] bytes = HexService.hex2byte("6162");
            FieldBuilder fieldBuilder = fixedLengthLiteral();
            MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
            assertArrayEquals(bytes, msgValue.getBodyValue(byte[].class));
```

The packed `FieldValue` then looks like
```XML

<f val="FFFF" valHex="FFFF"/>
```

Some examples of packed values
```
Examples of values packed with LiteralBodyPacker
Value '0A' packed to bytes as hex: 0A and unpacked again as hex: 0A
Value 'AA' packed to bytes as hex: AA and unpacked again as hex: AA
Value '0ABC' packed to bytes as hex: 0ABC and unpacked again as hex: 0ABC
Value 'A12345BCDE' packed to bytes as hex: A12345BCDE and unpacked again as hex: A12345BCDE
Value 'FF' packed to bytes as hex: FF and unpacked again as hex: FF
```

The source of the test is located in GitHub [LiteralBodyPackerTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/literal/LiteralBodyPackerTest.java)

More examples see [complex-example.md](../complex-example.md).