# `AsciiBodyPacker` examples

The following example shows how to define a field with body in [ASCII](https://en.wikipedia.org/wiki/ASCII) format
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of ASCII body definition",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/asciihex/AsciiBodyPackerTest.java",
        "beginString": "    private FieldBuilder fixedLengthAscii() {",
        "endString": "    }",
        "indentation": ""
    }
} &&endPlaceholder
```

The field structure
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgField dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "AsciiBodyPackerTest - MsgField structure dump: ",
        "includeBeginString": "false",
        "endString": "\"/>",
        "indentation": ""
    }
} &&endPlaceholder
```

The following example shows how to pack the field value
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of ASCII value packing",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/asciihex/AsciiBodyPackerTest.java",
        "beginString": "        FieldBuilder fieldBuilder = fixedLengthAscii();",
        "endString": "assertEquals(\"6162\", bytesHex);",
        "indentation": "    "
    }
} &&endPlaceholder
```

And the following example shows how to unpack the field bytes to a value
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of ASCII value unpacking",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/asciihex/AsciiBodyPackerTest.java",
        "beginString": "        byte[] bytes = HexService.hex2byte",
        "endString": "assertEquals(\"ab\", msgValue.getBodyValue(String.class));",
        "indentation": "    "
    }
} &&endPlaceholder
```

The packed `FieldValue` then looks like the next example
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgValue dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "AsciiBodyPackerTest - MsgValue structure dump: ",
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
        "beginString": "Examples of values packed with AsciiBodyPacker",
        "includeBeginString": "true",
        "endString": "Examples end.",
        "includeEndString": "false",
        "indentation": ""
    }
} &&endPlaceholder
```

The source of the test is located in GitHub [AsciiBodyPackerTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/asciihex/AsciiBodyPackerTest.java)

More examples see [complex-example.md](../complex-example.md).