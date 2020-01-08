# Length Packer

Some `MsgField`s have defined the `LEN` subfield, see the [field-types.md](../field-types.md) page.
These `LEN` subfields may have many different formats. The following implementations of the `LengthPacker` interface transform `LEN` subfield into different formats:
* com.credibledoc.iso8583packer.bcd.BcdLengthPacker
* com.credibledoc.iso8583packer.binary.BinaryLengthPacker
* com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker
* com.credibledoc.iso8583packer.hex.HexLengthPacker


Description of some implementations:
* [bcd-length-packer.md](../bcd/bcd-length-packer.md)
* [ebcdic-decimal-length-packer.md](../ebcdic/ebcdic-decimal-length-packer.md)
* [binary-length-packer.md](../binary/binary-length-packer.md)
* [hex-length-packer.md](../hex/hex-length-packer.md)
