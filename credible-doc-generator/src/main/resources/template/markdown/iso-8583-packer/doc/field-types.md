# Field types

The [FieldBuilder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldBuilder.java)
is able to create different field types, see the [MsgFieldType](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgFieldType.java)
enumeration.

These [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
types always have a `body` and may have a `header`. The example [here](../README.md) is a `MsgField` without a `header`, it has the `body` without the `tag` and `length` subfields.

## Length and Value types
The example of `LEN_VAL` type [bcd-length-packer.md](bcd/bcd-length-packer.md)