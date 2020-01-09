# `BinaryLengthPacker` description and examples

Some MsgFields have defined the `LEN` subfield, see the [field-types.md](../field-types.md) description.

The following example shows how to define a field length in the binary format
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of the binary length definition",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/binary/BinaryLengthPackerTest.java",
        "beginString": "            FieldBuilder.builder(MsgFieldType.LEN_VAL)",
        "endString": ".defineBodyPacker(BcdBodyPacker.leftPadding0());",
        "indentation": ""
    }
} &&endPlaceholder
```

The field structure then looks like the next example
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgField dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "BinaryLengthPackerTest - MsgField structure dump: ",
        "includeBeginString": "false",
        "endString": "\"/>",
        "indentation": ""
    }
} &&endPlaceholder
```

The following example shows how to pack and unpack a value to the defined field
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of binary length packing and unpacking",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/binary/BinaryLengthPackerTest.java",
        "beginString": "        String value = \"123456789\";",
        "endString": "assertEquals(value, msgValue.getBodyValue(String.class));",
        "indentation": "    "
    }
} &&endPlaceholder
```

The packed `FieldValue` with `lenHex` looks like the next example
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgValue dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "BinaryLengthPackerTest - MsgValue structure dump: ",
        "includeBeginString": "false",
        "endString": "\"/>",
        "indentation": ""
    }
} &&endPlaceholder
```

Some examples of packed values
```
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Some examples of packed values",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "Examples of integers packed with BinaryLengthPacker",
        "includeBeginString": "true",
        "endString": "Examples end.",
        "includeEndString": "false",
        "indentation": ""
    }
} &&endPlaceholder
```

More examples see [complex-example.md](../complex-example.md).