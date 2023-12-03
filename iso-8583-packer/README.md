# Module `iso-8583-packer`

The module is a library for creation, packing and unpacking ISO 8583 messages,
and also for messages in non-standard formats.

## Usage
The module is the source of the `iso-8583-packer-1.0.50` library.

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
                <version>1.0.50</version>
            </dependency>
        </dependencies>
    
    </project>

## Examples

### Fixed - length value without a tag

The code below will create a new instance of the [FieldBuilder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldBuilder.java) with a single field.
The field will contain 2 bytes data in [BCD](https://en.wikipedia.org/wiki/Binary-coded_decimal) format.

```Java
            FieldBuilder.builder(MsgFieldType.VAL)
                .defineLen(2)
                .defineBodyPacker(BcdBodyPacker.leftPadding0());
```

The field can be filled with data by [ValueHolder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/ValueHolder.java),
for example:
```Java
            String value = "123";
            ValueHolder valueHolder =
                ValueHolder.newInstance(fieldBuilder.getCurrentField())
                .setValue(value);
```

The filled object can be packed into bytes
```Java
            byte[] valueBytes = valueHolder.pack();
            String bytesHex = HexService.bytesToHex(valueBytes);
            assertEquals("0123", bytesHex);
```

The defined MsgField can be used for unpacking from bytes to an object
```Java
            String packedHex = "0456";
            byte[] packedBytes = HexService.hex2byte(packedHex);
            
            MsgValue msgValue =
                ValueHolder.newInstance(fieldBuilder.getCurrentField())
                .unpack(packedBytes);
            String unpackedValue = (String) msgValue.getBodyValue();
    
            String expectedValue = "456";
            assertEquals(expectedValue, unpackedValue);
```

You can find the complete example here: [BcdBodyPackerTest](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdBodyPackerTest.java)

More complex message definition with inner subfields is described in the [complex-example.md](doc/complex-example.md) document.

## Field types
Every field contains a header (optional) and a body (mandatory), for example <b>02 01 03</b>, where
* <b>02</b> is header `TAG`
* <b>01</b> is header `LEN`
* <b>03</b> is body `VAL`

The [FieldBuilder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldBuilder.java)
is able to create different field types, with and without headers, see the [field-types.md](doc/field-types.md) document.

### `TAG` types and packers
The following page [tag-packer.md](doc/tag/tag-packer.md) describes some formats and types of `TAG` packers.

### `LEN` types and packers
The following [length-packer.md](doc/length/length-packer.md) page describes different `LEN` subfield types
and implementations of the `LengthPacker` interface.

### `VAL` types and packers
The following page [body-packer.md](doc/body/body-packer.md) contains description of different formats of body values.

### Masker
In a production environment it is necessary to mask private sensitive data before print it to a log file.
The following page [masker.md](doc/masking/masker.md) describes how to mask the data.

### Bitmap
The ISO 8583 standard uses [Bitmaps](https://en.wikipedia.org/wiki/ISO_8583#Bitmaps) for defining of field numbers placed on a message.

The following [bitmap-packer.md](doc/bitmap/bitmap-packer.md) page describes how to define and use bitmaps.

## Repeated fields
In some cases is required to repeat some values in messages, for example to send multiple products purchased by a customer.

The following example [repeated-tag-val.md](doc/repeated/repeated-tag-val.md)
explains how to pack and unpack multiple recurrent fields.