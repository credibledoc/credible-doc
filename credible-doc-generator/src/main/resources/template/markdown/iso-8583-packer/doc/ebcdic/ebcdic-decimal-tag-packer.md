# `EbcdicDecimalTagPacker` examples

Some [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
with [MsgValue](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgValue.java)
pairs contain `TAG` subfields, see the [field-types.md](../field-types.md) page with description of field types.

The following example shows how to define `TAG` in the [EBCDIC](https://en.wikipedia.org/wiki/EBCDIC) format
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of EBCDIC tag definition",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ebcdic/EbcdicDecimalTagPackerTest.java",
        "beginString": "    private FieldBuilder createField() {",
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
        "beginString": "EbcdicDecimalTagPackerTest - MsgField structure dump: ",
        "includeBeginString": "false",
        "endString": "</f>",
        "indentation": ""
    }
} &&endPlaceholder
```

The following example shows packing and unpacking of the field value
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of EBCDIC tag packing and unpacking",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ebcdic/EbcdicDecimalTagPackerTest.java",
        "beginString": "        String value = \"1234\";",
        "endString": "assertEquals(value, unpackedValue);",
        "indentation": ""
    }
} &&endPlaceholder
```

The packed `FieldValue` with `TAG` looks like
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgValue dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "EbcdicDecimalTagPackerTest - MsgValue structure dump: ",
        "includeBeginString": "false",
        "endString": "</f>",
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
        "beginString": "Examples of integers packed with EbcdicDecimalTagPacker",
        "includeBeginString": "true",
        "endString": "Examples end.",
        "includeEndString": "false",
        "indentation": ""
    }
} &&endPlaceholder
```

More examples see [complex-example.md](../complex-example.md).