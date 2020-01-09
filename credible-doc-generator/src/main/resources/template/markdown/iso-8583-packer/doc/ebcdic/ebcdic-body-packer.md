# `EbcdicBodyPacker` examples

The following example shows how to define a field with body in the [EBCDIC](https://en.wikipedia.org/wiki/EBCDIC) format
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of EBCDIC body definition",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ebcdic/EbcdicBodyPackerTest.java",
        "beginString": "    private FieldBuilder fixedLengthEbcdic() {",
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
        "beginString": "EbcdicBodyPackerTest - MsgField structure dump: ",
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
    "description": "Example of EBCDIC value packing",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ebcdic/EbcdicBodyPackerTest.java",
        "beginString": "        FieldBuilder fieldBuilder = fixedLengthEbcdic();",
        "endString": "assertArrayEquals(HexService.hex2byte(\"C885\"), valueBytes);",
        "indentation": "    "
    }
} &&endPlaceholder
```

And the following example shows how to unpack the field bytes to a value
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of EBCDIC value unpacking",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ebcdic/EbcdicBodyPackerTest.java",
        "beginString": "        byte[] bytes = HexService.hex2byte",
        "endString": "assertEquals(\"He\", msgValue.getBodyValue(String.class));",
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
        "beginString": "EbcdicBodyPackerTest - MsgValue structure dump: ",
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
        "beginString": "Examples of values packed with EbcdicBodyPacker",
        "includeBeginString": "true",
        "endString": "Examples end.",
        "includeEndString": "false",
        "indentation": ""
    }
} &&endPlaceholder
```

The source of the test is located in GitHub [EbcdicBodyPackerTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ebcdic/EbcdicBodyPackerTest.java)

More examples see [complex-example.md](../complex-example.md).