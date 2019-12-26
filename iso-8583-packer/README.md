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

```Java
    private FieldBuilder fixedLengthBcd() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.LEFT_PADDED_0);
    }
```