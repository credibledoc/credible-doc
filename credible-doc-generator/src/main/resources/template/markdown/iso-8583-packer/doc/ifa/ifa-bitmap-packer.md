# `IfaBitmapPackerExtendedTest` examples


The following example shows how to define a field with `IFA` `bitmap` format
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of IFA bitmap definition",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ifa/IfaBitmapPackerExtendedTest.java",
        "beginString": "        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)",
        "endString": "fieldBuilder.validateStructure();",
        "indentation": ""
    }
} &&endPlaceholder
```

The defined structure can be shown as XML by calling the `DumpService.getInstance().dumpMsgValue(isoMsgField, msgValue, true)` method
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgField dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "IfaBitmapPackerExtendedTest - Root msgField dump: ",
        "includeBeginString": "false",
        "endString": "End of msgField dump.",
        "includeEndString": "false",
        "indentation": "    "
    }
} &&endPlaceholder
```

The following example shows how to set a value to the `bitmap` child
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of IFA bitmap usage",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ifa/IfaBitmapPackerExtendedTest.java",
        "beginString": "        ValueHolder valueHolder = ValueHolder.newInstance(isoMsgField);",
        "endString": "assertEquals(expectedHex, packedHex);",
        "indentation": ""
    }
} &&endPlaceholder
```

The packed `MsgValue` then looks like the next example
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of packed MsgValue",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "IfaBitmapPackerExtendedTest - Root msgValue dump:",
        "includeBeginString": "false",
        "endString": "End of msgValue dump.",
        "includeEndString": "false",
        "indentation": "        "
    }
} &&endPlaceholder
```

The source of the test is located in GitHub [IfaBitmapPackerExtendedTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/ifa/IfaBitmapPackerExtendedTest.java)

More examples see [complex-example.md](../complex-example.md).