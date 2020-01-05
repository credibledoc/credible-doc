# Masker

On the production environment it is necessary to mask private sensitive data before printing to a log file.

The [Masker](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/masking/Masker.java) interface contains few methods for masking data.

The following implementations of the `Masker` interface can be used:
&&beginPlaceholder {
    "className": "com.credibledoc.substitution.content.generator.code.InterfaceImplementationsContentGenerator",
    "description": "All known implementations of the Masker interface",
    "parameters": {
        "interfaceName": "com.credibledoc.iso8583packer.masking.Masker",
        "includePackages": "com.credibledoc.*"
    }
} &&endPlaceholder

Some implementations are described on the following pages:
* [pan-masker.md](../pan/pan-masker.md)
