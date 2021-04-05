# `BitmapPacker` examples

The ISO 8583 standard uses [Bitmaps](https://en.wikipedia.org/wiki/ISO_8583#Bitmaps) as holders of field numbers located in a message.

The following implementations of the [BitmapPacker](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/bitmap/BitmapPacker.java)
interface transform bitmaps to different formats:
* com.credibledoc.iso8583packer.ifa.IfaBitmapPacker
* com.credibledoc.iso8583packer.ifb.IfbBitmapPacker


The packer supports a primary (8 bytes), secondary (16 bytes) and tertiary (24 bytes) bitmap parts.
* An 8-byte bitmap can have 2 to 64 field numbers, bit 1 indicates the secondary bitmap
* A 16-byte bitmap can have 66 to 128 field numbers, bit 65 indicates the tertiary bitmap
* A 24-byte bitmap can have 130 to 192 field numbers, bit 129 is not used

Some implementations are described on the following pages:
* [ifa-bitmap-packer.md](../ifa/ifa-bitmap-packer.md)
* [ifb-bitmap-packer.md](../ifb/ifb-bitmap-packer.md)
