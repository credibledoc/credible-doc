# `PanMasker` examples

The implementation of the [masker.md](../masking/masker.md) interface replaces the PAN with wildcards except the first few digits.

The following example shows how to define a `Masker`.
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of PanMasker definition",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/pan/PanMaskerTest.java",
        "beginString": "        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.VAL)",
        "endString": "MsgValue msgValue = valueHolder.getCurrentMsgValue();",
        "indentation": ""
    }
} &&endPlaceholder
```

The following example shows how to use the `Masker`.
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of PanMasker usage",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/pan/PanMaskerTest.java",
        "beginString": "        DumpService dumpService = DumpService.getInstance();",
        "endString": "dumpService.dumpMsgValue(msgField, msgValue, true);",
        "indentation": ""
    }
} &&endPlaceholder
```

The following example shows the field structure, field value without masking and the field value with masking
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgField dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "PanMaskerTest - MsgField structure dump: ",
        "includeBeginString": "false",
        "endString": "The end of the example",
        "includeEndString": "false",
        "indentation": "        "
    }
} &&endPlaceholder
```

More examples see [complex-example.md](../complex-example.md).