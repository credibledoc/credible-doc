# Example of a repeated field in an ISO message

The following example shows how to define and use a field of the 'TAG_VAL' type that is repeated in a message.

Field definition
```Java
        String root = "Root";
        String product = "Product";
        String productCode = "Code";
        String productAmount = "Amount";
        FieldBuilder msg = FieldBuilder.builder(MsgFieldType.MSG).defineName(root);
        FieldBuilder fieldBuilder = FieldBuilder.from(msg.getCurrentField())
            .createChild(MsgFieldType.TAG_VAL)
            .defineHeaderTag("Item")
            .defineName(product)
            .defineLen(8)
            .defineHeaderTagPacker(AsciiStringTagPacker.getInstance(4))

            .createChild(MsgFieldType.VAL)
            .defineName(productCode)
            .defineLen(6)
            .defineBodyPacker(AsciiBodyPacker.getInstance())

            .cloneToSibling()
            .defineName(productAmount)
            .defineBodyPacker(BcdIntBodyPacker.getInstance(2))
            .defineLen(2)
            .jumpToRoot();
        fieldBuilder.validateStructure();
```

The defined field structure
```XML

<f type="MSG" name="Root">
    <f type="TAG_VAL" tag="Item" name="Product" len="8" tagPacker="AsciiStringTagPacker">
        <f type="VAL" name="Code" bodyPacker="AsciiBodyPacker" len="6"/>
        <f type="VAL" name="Amount" bodyPacker="BcdIntBodyPacker" len="2"/>
    </f>
</f>
```

And the following code shows the usage of the definition, where the `cloneSibling()` method is used
for creation of a second field with the same tag `Item` as the first field
```Java
        MsgField msgField = fieldBuilder.getCurrentField();
        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder);
        
        // set values
        valueHolder.jumpAbsolute(root, product, productCode).setValue("code01")
            .jumpToSibling(productAmount).setValue(1);
        
        valueHolder.jumpToParent().cloneSibling().jumpToChild(productCode).setValue("code02")
            .jumpToSibling(productAmount).setValue(8);

        // pack to bytes
        byte[] bytes = valueHolder.jumpToRoot().pack();

        // unpack from bytes
        MsgValue unpackedMsgValue = ValueHolder.newInstance(msgField).unpack(bytes);
        List<MsgValue> products = unpackedMsgValue.getChildren();
        assertEquals(2, products.size());
        
        ValueHolder firstProduct = ValueHolder.newInstance(products.get(0), msgField.getChildren().get(0));
        String firstProductCode = firstProduct.jumpToChild(productCode).getValue(String.class);
        assertEquals("code01", firstProductCode);
        int firstProductAmount = firstProduct.jumpToSibling(productAmount).getValue(Integer.class);
        assertEquals(1, firstProductAmount);

        ValueHolder secondProduct = ValueHolder.newInstance(products.get(1), msgField.getChildren().get(0));
        String secondProductCode = secondProduct.jumpToChild(productCode).getValue(String.class);
        assertEquals("code02", secondProductCode);
        int secondProductAmount = secondProduct.jumpToSibling(productAmount).getValue(Integer.class);
        assertEquals(8, secondProductAmount);
```

The packed `FieldValue` is shown in the following example
```XML

<f name="Root">
    <f name="Product" tag="Item" tagHex="4974656D">
        <f name="Code" val="code01" valHex="636F64653031"/>
        <f name="Amount" val="1" valHex="0001"/>
    </f>
    <f name="Product" tag="Item" tagHex="4974656D">
        <f name="Code" val="code02" valHex="636F64653032"/>
        <f name="Amount" val="8" valHex="0008"/>
    </f>
</f>

Bytes: 4974656D636F6465303100014974656D636F646530320008
```

The test source is located in GitHub [RepeatedFieldsTest.java](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/test/java/com/credibledoc/iso8583packer/repeated/RepeatedFieldsTest.java)

More examples see [complex-example.md](../complex-example.md).