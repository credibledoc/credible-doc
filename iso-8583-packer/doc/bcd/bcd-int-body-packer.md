# `BcdIntBodyPacker` examples

The following example shows how to define a field with body in [BCD](https://en.wikipedia.org/wiki/Binary-coded_decimal) format
where a value is Integer.
```Java
    private FieldBuilder fixedLengthBcdInt() {
        return FieldBuilder.builder(MsgFieldType.VAL)
                .defineLen(2)
                .defineBodyPacker(BcdIntBodyPacker.getInstance(2));
    }
```

The field structure
```XML

<f type="VAL" bodyPacker="BcdIntBodyPacker" len="2"/>
```

The following example shows how to pack the field value
```Java
            FieldBuilder fieldBuilder = fixedLengthBcdInt();
            fieldBuilder.validateStructure();
    
            int value = 123;
            ValueHolder valueHolder =
                ValueHolder.newInstance(fieldBuilder.getCurrentField())
                .setValue(value);
            
            valueHolder.validateData();
    
            byte[] valueBytes = valueHolder.pack();
            String bytesHex = HexService.bytesToHex(valueBytes);
            assertEquals("0123", bytesHex);
```

The packed `FieldValue` then looks like the next example
```XML

<f val="123" valHex="0123"/>
```

Some examples of packed values
```
Examples of values packed with BcdIntBodyPacker 
BcdIntBodyPacker with numBytes '1'
Value '1' packed to bytes as hex: 01
Value '12' packed to bytes as hex: 12
Value '123' cannot be packed to bytes. Exception: Length '3' of value '123' is greater than the packer is able to pack because it has defined numBytes '1'.
Value '1234' cannot be packed to bytes. Exception: Length '4' of value '1234' is greater than the packer is able to pack because it has defined numBytes '1'.
BcdIntBodyPacker with numBytes '2'
Value '1' packed to bytes as hex: 0001
Value '12' packed to bytes as hex: 0012
Value '123' packed to bytes as hex: 0123
Value '1234' packed to bytes as hex: 1234
BcdIntBodyPacker with numBytes '3'
Value '1' packed to bytes as hex: 000001
Value '12' packed to bytes as hex: 000012
Value '123' packed to bytes as hex: 000123
Value '1234' packed to bytes as hex: 001234
BcdIntBodyPacker with numBytes '4'
Value '1' packed to bytes as hex: 00000001
Value '12' packed to bytes as hex: 00000012
Value '123' packed to bytes as hex: 00000123
Value '1234' packed to bytes as hex: 00001234
```

The source of the test is located in GitHub [BcdIntBodyPackerTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdIntBodyPackerTest.java)

More examples see [complex-example.md](../complex-example.md).