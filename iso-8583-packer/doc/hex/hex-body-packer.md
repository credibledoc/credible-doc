# `HexBodyPacker` examples

The following example shows how to define a field with body in the `Hex` format
```Java
    private FieldBuilder fixedLengthHex() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineLen(2)
            .defineBodyPacker(HexBodyPacker.getInstance());
    }
```

The field structure
```XML

<f type="VAL" bodyPacker="HexBodyPacker" len="2"/>
```

The following example shows how to pack the field value
```Java
            FieldBuilder fieldBuilder = fixedLengthHex();
            fieldBuilder.validateStructure();
    
            String value = "0123";
            ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField())
                .setValue(value);
            
            valueHolder.validateData();
    
            byte[] valueBytes = valueHolder.pack();
            assertEquals(value, HexService.bytesToHex(valueBytes));
```

And the following example shows how to unpack the field bytes to a value
```Java
            FieldBuilder fieldBuilder = fixedLengthHex();
            fieldBuilder.validateStructure();
    
            String value = "0123";
            ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField())
                .setValue(value);
            
            valueHolder.validateData();
    
            byte[] valueBytes = valueHolder.pack();
            assertEquals(value, HexService.bytesToHex(valueBytes));
```

The packed `FieldValue` then looks like
```XML

<f val="0123" valHex="0123"/>
```

Some examples of packed values
```
Examples of values packed with HexBodyPacker
Value '0A' packed to bytes as hex: 0A and unpacked again as String: 0A
Value 'AA' packed to bytes as hex: AA and unpacked again as String: AA
Value '0ABC' packed to bytes as hex: 0ABC and unpacked again as String: 0ABC
Value 'A12345BCDE' packed to bytes as hex: A12345BCDE and unpacked again as String: A12345BCDE
Value 'FF' packed to bytes as hex: FF and unpacked again as String: FF
```

The source of the test is located in GitHub [HexBodyPackerTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/hex/HexBodyPackerTest.java)

More examples see [complex-example.md](../complex-example.md).