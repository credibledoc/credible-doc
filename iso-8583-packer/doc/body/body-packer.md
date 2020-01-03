# Body Packer

Every leaf field in a message contains a value. Leaf is a field without children.
The value can be packed in different formats.

The following implementations of the `BodyPacker` interface transform values to different formats:
* com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker
* com.credibledoc.iso8583packer.bcd.BcdBodyPacker
* com.credibledoc.iso8583packer.ebcdic.EbcdicBodyPacker
* com.credibledoc.iso8583packer.hex.HexBodyPacker
* com.credibledoc.iso8583packer.literal.LiteralBodyPacker


Some implementations are described on the following pages:
* [ascii-body-packer.md](../asciihex/ascii-body-packer.md)
* [bcd-body-packer.md](../bcd/bcd-body-packer.md)
* [ebcdic-body-packer.md](../ebcdic/ebcdic-body-packer.md)
* [hex-body-packer.md](../hex/hex-body-packer.md)
* [literal-body-packer.md](../literal/literal-body-packer.md)
