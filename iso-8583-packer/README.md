# Module `iso-8583-packer`

The module is a library for creation, packing and unpacking ISO 8583 messages.

## Usage
The module is the source of the `iso-8583-packer-1.0.11` library.

The library can be loaded from the [Maven Central Repository](https://mvnrepository.com/artifact/com.credibledoc/iso-8583-packer).

Example of Maven configuration in a `pom.xml` file

    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
        ... some mandatory tags omitted
    
        <dependencies>
            <dependency>
                <groupId>com.credibledoc</groupId>
                <artifactId>iso-8583-packer</artifactId>
                <version>1.0.11</version>
            </dependency>
        </dependencies>
    
    </project>

## Examples

### Fixed - length value without tag

The code below will create a new instance of the [FieldBuilder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldBuilder.java) with a single field.
The field will contain 2 bytes data in [BCD](https://en.wikipedia.org/wiki/Binary-coded_decimal) format.

```Java
            FieldBuilder.builder(MsgFieldType.VAL)
                .defineLen(2)
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
```

The field can be filled with data by [FieldFiller](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldFiller.java),
for example:
```Java
            String value = "123";
            FieldFiller fieldFiller =
                FieldFiller.newInstance(fieldBuilder.getCurrentField())
                .setValue(value);
```

The filled object can be packed to bytes
```Java
            byte[] valueBytes = fieldFiller.pack();
            String bytesHex = HexService.bytesToHex(valueBytes);
            assertEquals("0123", bytesHex);
```

The defined MsgField can be used for unpacking from bytes to an object
```Java
            String packedHex = "0456";
            byte[] packedBytes = HexService.hex2byte(packedHex);
            
            MsgValue msgValue =
                FieldFiller.newInstance(fieldBuilder.getCurrentField())
                .unpack(packedBytes);
            String unpackedValue = (String) msgValue.getBodyValue();
    
            String expectedValue = "456";
            assertEquals(expectedValue, unpackedValue);
```

You can find the complete example here: [BcdBodyPackerTest](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdBodyPackerTest.java)

More complex message definition with inner subfields is described in the [complex-example.md](doc/complex-example.md) document.

## Field types
The [FieldBuilder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldBuilder.java)
is able to create different field types, with and without headers, see the [field-types.md](doc/field-types.md) document.

### `LEN` types
Examples of `LEN` types

* [bcd-length-packer.md](doc/bcd/bcd-length-packer.md)
* [ebcdic-decimal-length-packer.md](doc/ebcdic/ebcdic-decimal-length-packer.md)