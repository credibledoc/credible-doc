# Body Packer

Every leaf field in a message contains a value. Leaf is a field without children.
The value can be packed in different formats.

The following implementations of the `BodyPacker` interface transform `VAL` subfield to different formats:
* com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker
* com.credibledoc.iso8583packer.bcd.BcdBodyPacker
* com.credibledoc.iso8583packer.ebcdic.EbcdicBodyPacker
* com.credibledoc.iso8583packer.hex.HexBodyPacker
* com.credibledoc.iso8583packer.literal.LiteralBodyPacker


Description of some implementations:
* [bcd-body-packer.md](../bcd/bcd-body-packer.md)
