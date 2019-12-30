# BcdLengthPacker examples

Some MsgFields have defined the `LEN` subfield, for example
```
LEN VAL
 02 123
```
or
```
TAG LEN VAL
 60  02 543
```
or
```
LEN TAG VAL
 03  60 543
```

The following example shows how to define a field length in [BCD](https://en.wikipedia.org/wiki/Binary-coded_decimal) format
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of BCD length definition",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdLengthPackerTest.java",
        "beginString": "            FieldBuilder.builder(MsgFieldType.LEN_VAL)",
        "endString": ".defineBodyPacker(BcdBodyPacker.leftPadding0());",
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
        "beginString": "BcdLengthPackerTest - MsgField structure dump: ",
        "includeBeginString": "false",
        "endString": "\"/>",
        "indentation": ""
    }
} &&endPlaceholder
```

The following example shows packing and unpacking of the field value
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of BCD length packing and unpacking",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/bcd/BcdLengthPackerTest.java",
        "beginString": "        String value = \"123\";",
        "endString": "assertEquals(value, msgValue.getBodyValue(String.class));",
        "indentation": "    "
    }
} &&endPlaceholder
```

The packed `FieldValue` with `lenHex` looks like
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgValue dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "BcdLengthPackerTest - MsgValue structure dump: ",
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
    "description": "MsgValue dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "Examples of integers packed with",
        "includeBeginString": "true",
        "endString": "Examples end.",
        "includeEndString": "false",
        "indentation": ""
    }
} &&endPlaceholder
```

More examples see [complex-example.md](../complex-example.md).