# Masker

In a production environment it is necessary to mask private sensitive data before printing to a log file.

The [Masker](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/masking/Masker.java) interface contains few methods for masking data.

The following implementations of the `Masker` interface can be used:
* com.credibledoc.iso8583packer.masking.AnyMasker
* com.credibledoc.iso8583packer.pan.PanMasker


Some implementations are described on the following pages:
* [pan-masker.md](../pan/pan-masker.md)
