# Complex example of the `MsgField` structure definition

The following example contains the definition of an ISO 8583 message

```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of complex ISO 8583 message",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/FieldBuilderTest.java",
        "beginString": "        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)",
        "endString": "        assertEquals(pan, unpackedPanString);",
        "indentation": ""
    }
} &&endPlaceholder
```

The message definition can be described as XML by calling the `DumpService.dumpMsgField(isoMsgField)` method
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of complex ISO 8583 message structure as XML",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "Root msgField dump: \n",
        "includeBeginString": "false",
        "endString": "End of msgField dump.",
        "includeEndString": "false",
        "indentation": ""
    }
} &&endPlaceholder
```

The message values can be described as XML by calling the `DumpService.dumpMsgValue(isoMsgField, msgValue, false)` method
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of complex ISO 8583 message value as XML",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "Root msgValue dump: \n",
        "includeBeginString": "false",
        "endString": "End of msgValue dump.",
        "includeEndString": "false",
        "indentation": ""
    }
} &&endPlaceholder
```

The complete example is located on GitHub [FieldBuilderTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/FieldBuilderTest.java).