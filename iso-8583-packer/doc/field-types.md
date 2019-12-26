# Field types

The [FieldBuilder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldBuilder.java)
is able to create different field types, see the [MsgFieldType](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgFieldType.java)
enumeration.

These [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
types always have a `body` and may have a `header`. The example [here](../README.md) is a `MsgField` without a `header`, it has the `body` without the `tag` and `length` subfields.

