# Length Packer

Some `MsgField`s have defined the `LEN` subfield, see the [field-types.md](../field-types.md) page.
These `LEN` subfields may have many different formats. The following implementations of the `LengthPacker` interface transform `LEN` subfield into different formats:
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.InterfaceImplementationsContentGenerator",
    "description": "All known implementations of the LengthPacker interface",
    "parameters": {
        "interfaceName": "com.credibledoc.iso8583packer.length.LengthPacker",
        "includePackages": "com.credibledoc.*"
    }
} &&endPlaceholder

Description of some implementations:
* [ascii-length-packer.md](../asciihex/ascii-length-packer.md)
* [bcd-length-packer.md](../bcd/bcd-length-packer.md)
* [ebcdic-decimal-length-packer.md](../ebcdic/ebcdic-decimal-length-packer.md)
* [binary-length-packer.md](../binary/binary-length-packer.md)
* [hex-length-packer.md](../hex/hex-length-packer.md)
