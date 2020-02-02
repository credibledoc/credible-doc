# Example of a repeated field in an ISO message

The following example shows how to define and use a field of the 'TAG_VAL' type that is repeated in a message.

Field definition
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of repeated TAG_VAL field definition",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/repeated/RepeatedFieldsTest.java",
        "beginString": "        String root = ",
        "endString": "        fieldBuilder.validateStructure();",
        "indentation": ""
    }
} &&endPlaceholder
```

The defined field structure
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgField dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "RepeatedFieldsTest - MsgField structure dump: ",
        "includeBeginString": "false",
        "endString": "End of MsgField dump",
        "includeEndString": "false",
        "indentation": ""
    }
} &&endPlaceholder
```

And the following code shows the usage of the definition, where the `cloneSibling()` method is used
for creation of a second field with the same tag `Item` as the first field
```Java
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "Example of repeated TAG_VAL field usage",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/repeated/RepeatedFieldsTest.java",
        "beginString": "        MsgField msgField = fieldBuilder.getCurrentField();",
        "endString": "        assertEquals(8, secondProductAmount);",
        "indentation": ""
    }
} &&endPlaceholder
```

The packed `FieldValue` is shown in the following example
```XML
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.SourceContentGenerator",
    "description": "MsgValue dump",
    "parameters": {
        "sourceRelativePath": "iso-8583-packer/log/iso-8583-packer.log",
        "beginString": "RepeatedFieldsTest - MsgValue with repeated fields: ",
        "includeBeginString": "false",
        "endString": "End of example",
        "includeEndString": "false",
        "indentation": ""
    }
} &&endPlaceholder
```

The test source is located in GitHub [RepeatedFieldsTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/repeated/RepeatedFieldsTest.java)

More examples see [complex-example.md](../complex-example.md).