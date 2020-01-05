# `BitmapPacker` examples

The ISO 8583 standard uses [Bitmaps](https://en.wikipedia.org/wiki/ISO_8583#Bitmaps) as holders of field numbers located in a message.

The following implementations of the [BitmapPacker](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/bitmap/BitmapPacker.java)
interface transform bitmaps to different formats:
* com.credibledoc.iso8583packer.ifa.IfaBitmapPacker
* com.credibledoc.iso8583packer.ifb.IfbBitmapPacker


Some implementations are described on the following pages:
* [ifa-bitmap-packer.md](../ifa/ifa-bitmap-packer.md)
* [ifb-bitmap-packer.md](../ifb/ifb-bitmap-packer.md)
