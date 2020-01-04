# Field types

The [FieldBuilder](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/FieldBuilder.java)
is able to create different field types, see the [MsgFieldType](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgFieldType.java)
enumeration.

These [MsgField](https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/src/main/java/com/credibledoc/iso8583packer/message/MsgField.java)
types always have a `body` (`VAL`) and may have a `header` (`TAG`, `LEN`). The example [here](../README.md) is a `MsgField` without a `header`,
it has the `body` (`VAL`) without the `header` (`TAG` and `LEN`) subfields.

## Length and Value types

Some MsgFields have defined the `LEN` subfield, for example
```
LEN VAL
 02 123
```
or
```
TAG LEN VAL
 60  02 543
```
or
```
LEN TAG VAL
 03  60 543
```
* Examples of `TAG` types [tag-packer.md](../doc/tag/tag-packer.md)
* Examples of `LEN` types [length-packer.md](../doc/length/length-packer.md)
* Examples of `VAL` types [body-packer.md](../doc/body/body-packer.md)
