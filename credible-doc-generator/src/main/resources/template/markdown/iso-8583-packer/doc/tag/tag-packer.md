# `TAG` Packer

Some [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
with [MsgValue](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgValue.java)
pairs contain `TAG` subfields.

The `TAG` subfields are used for identification message fields. The following Wiki pages describe the `TAG` subfield as the `type`, see
[Type-length-value](https://en.wikipedia.org/wiki/Type-length-value) or as the `key`,
see the [KLV](https://en.wikipedia.org/wiki/KLV) pages. The best (in my opinion) implementation of ISO 8583, the `jPOS` library, uses the `fieldNumber` term,
see the [ISOMsg](http://jpos.org/doc/javadoc/org/jpos/iso/ISOMsg.html) page.

The `TAG` can be packed in different formats.

The following implementations of the `TagPacker` interface transform values to different formats:
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.InterfaceImplementationsContentGenerator",
    "description": "All known implementations of the TagPacker interface",
    "parameters": {
        "interfaceName": "com.credibledoc.iso8583packer.tag.TagPacker",
        "includePackages": "com.credibledoc.*"
    }
} &&endPlaceholder

Some implementations are described on the following pages:
* [ebcdic-decimal-tag-packer.md](../ebcdic/ebcdic-decimal-tag-packer.md)
