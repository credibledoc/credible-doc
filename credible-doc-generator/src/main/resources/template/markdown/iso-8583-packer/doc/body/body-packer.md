# Body Packer

Every leaf field in a message contains a value. Leaf is a field without children.
The value can be packed in different formats.

The following implementations of the `BodyPacker` interface transform values to different formats:
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.InterfaceImplementationsContentGenerator",
    "description": "All known implementations of the BodyPacker interface",
    "parameters": {
        "interfaceName": "com.credibledoc.iso8583packer.body.BodyPacker",
        "includePackages": "com.credibledoc.*"
    }
} &&endPlaceholder

Some implementations described on the following pages:
* [ascii-body-packer.md](../asciihex/ascii-body-packer.md)
* [bcd-body-packer.md](../bcd/bcd-body-packer.md)
* [ebcdic-body-packer.md](../ebcdic/ebcdic-body-packer.md)
* [hex-body-packer.md](../hex/hex-body-packer.md)
* [literal-body-packer.md](../literal/literal-body-packer.md)
