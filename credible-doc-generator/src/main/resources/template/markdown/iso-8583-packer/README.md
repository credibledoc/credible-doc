# Module `iso-8583-packer`

The module is a library for creation, packing and unpacking ISO 8583 messages.

## Usage
The module is the source of the `&&beginPlaceholder {
                                          "className": "com.credibledoc.substitution.content.generator.pom.JarNameContentGenerator",
                                          "description": "Latest name and version of substitution-reporting artifact in Maven Central Repository",
                                          "parameters": {
                                              "url": "https://repo1.maven.org/maven2/com/credibledoc/iso-8583-packer/maven-metadata.xml",
                                              "nameAndVersionSeparator": "-"
                                          }
                                   } &&endPlaceholder` library.

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
                <version>&&beginPlaceholder {
                        "className": "com.credibledoc.substitution.content.generator.pom.JarNameContentGenerator",
                        "description": "Latest version of iso-8583-packer artifact in Maven Central Repository",
                        "parameters": {
                            "url": "https://repo1.maven.org/maven2/com/credibledoc/iso-8583-packer/maven-metadata.xml",
                            "versionOnly": "true"
                        }
                 } &&endPlaceholder</version>
            </dependency>
        </dependencies>
    
    </project>

## Examples

### Fixed - length value without tag

The code below will create a new instance of the [FieldBuilder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldBuilder.java) with a single field.
The field will contain 2 bytes data in [BCD](https://en.wikipedia.org/wiki/Binary-coded_decimal) format.

```Java
&&beginPlaceholder {
                        "className": "com.credibledoc.substitution.content.generator.code.MethodSourceContentGenerator",
                        "description": "Example of fixed length BCD value definition",
                        "parameters": {
                            "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdBodyPackerTest.java",
                            "beginString": "            FieldBuilder.builder(MsgFieldType.VAL)",
                            "endString": "                .defineBodyPacker(BcdBodyPacker.LEFT_PADDED_0);",
                            "indentation": ""
                        }
                 } &&endPlaceholder
```

The field can be filled with data by [FieldFiller](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldFiller.java),
for example:
```Java
&&beginPlaceholder {
                        "className": "com.credibledoc.substitution.content.generator.code.MethodSourceContentGenerator",
                        "description": "Example of fixed length BCD value filling",
                        "parameters": {
                            "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdBodyPackerTest.java",
                            "beginString": "        String value = \"123\";",
                            "endString": "            .setValue(value);",
                            "indentation": "    "
                        }
                 } &&endPlaceholder
```

The filled object can be packed to bytes
```Java
&&beginPlaceholder {
                        "className": "com.credibledoc.substitution.content.generator.code.MethodSourceContentGenerator",
                        "description": "Example of fixed length BCD value packing",
                        "parameters": {
                            "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdBodyPackerTest.java",
                            "beginString": "        byte[] valueBytes = fieldFiller.pack();",
                            "endString": "        assertEquals(\"0123\", bytesHex);",
                            "indentation": "    "
                        }
                 } &&endPlaceholder
```

The defined MsgField can be used for unpacking from bytes to an object
```Java
&&beginPlaceholder {
                        "className": "com.credibledoc.substitution.content.generator.code.MethodSourceContentGenerator",
                        "description": "Example of fixed length BCD value unpacking",
                        "parameters": {
                            "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdBodyPackerTest.java",
                            "beginString": "        String packedHex = \"0456\";",
                            "endString": "        assertEquals(expectedValue, unpackedValue);",
                            "indentation": "    "
                        }
                 } &&endPlaceholder
```

You can find the complete example here: [BcdBodyPackerTest](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdBodyPackerTest.java)

More complex message definition with inner subfields is described in the [complex-example.md](doc/complex-example.md)

## Field types
The [FieldBuilder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldBuilder.java)
is able to create different field types, with and without headers, see the [field-types.md](doc/field-types.md).