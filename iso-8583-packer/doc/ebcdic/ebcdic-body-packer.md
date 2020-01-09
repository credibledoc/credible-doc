# `EbcdicBodyPacker` examples

The following example shows how to define a field with body in the [EBCDIC](https://en.wikipedia.org/wiki/EBCDIC) format
```Java
    private FieldBuilder fixedLengthEbcdic() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineBodyPacker(EbcdicBodyPacker.getInstance())
            .defineLen(2);
    }
```

The field structure
```XML

<f type="VAL" bodyPacker="EbcdicBodyPacker" len="2"/>
```

The following example shows how to pack the field value
```Java
            FieldBuilder fieldBuilder = fixedLengthEbcdic();
            fieldBuilder.validateStructure();
    
            String value = "He";
            ValueHolder valueHolder =
                ValueHolder.newInstance(fieldBuilder.getCurrentField())
                .setValue(value);
            
            valueHolder.validateData();
    
            byte[] valueBytes = valueHolder.pack();
            assertArrayEquals(HexService.hex2byte("C885"), valueBytes);
```

And the following example shows how to unpack the field bytes to a value
```Java
            byte[] bytes = HexService.hex2byte("C885");
            FieldBuilder fieldBuilder = fixedLengthEbcdic();
            MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
            assertEquals("He", msgValue.getBodyValue(String.class));
```

The packed `FieldValue` then looks like the next example
```XML

<f val="He" valHex="C885"/>
```

Some examples of packed values
```
Examples of values packed with EbcdicBodyPacker
Value '0A' packed to bytes as hex: F0C1 and unpacked again as String: '0A'
Value 'Hello' packed to bytes as hex: C885939396 and unpacked again as String: 'Hello'
Value 'A b' packed to bytes as hex: C14082 and unpacked again as String: 'A b'
```

The source of the test is located in GitHub [EbcdicBodyPackerTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ebcdic/EbcdicBodyPackerTest.java)

More examples see [complex-example.md](../complex-example.md).