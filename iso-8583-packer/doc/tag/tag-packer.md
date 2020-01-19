# `TAG` Packer

Some [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
with [MsgValue](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgValue.java)
pairs contain `TAG` or `fieldNum` subfields.

The `TAG` or `fieldNum` subfields are used for identification of message fields.
The following Wiki pages describe the `TAG` subfield as a `type`, see
[Type-length-value](https://en.wikipedia.org/wiki/Type-length-value) page or as a `key`,
see the [KLV](https://en.wikipedia.org/wiki/KLV) page.

The  `fieldNum` property is defined in subfields (children) of the `MsgFieldType.BIT_SET` fields only.
The `TAG` property is defined in fields of `TAG_LEN_VAL`, `LEN_TAG_VAL`, `TAG_VAL` types and can be packed in different formats.

The following implementations of the `TagPacker` interface transform tags to different formats:
* com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalTagPacker
* com.credibledoc.iso8583packer.hex.HexTagPacker
* com.credibledoc.iso8583packer.literal.LiteralTagPacker


Some implementations are described on the following pages:
* [ebcdic-decimal-tag-packer.md](../ebcdic/ebcdic-decimal-tag-packer.md)
* [hex-tag-packer.md](../hex/hex-tag-packer.md)
* [literal-tag-packer.md](../literal/literal-tag-packer.md)
