# Release Notes of `iso-8583-packer` version 1.0.12

Static instances of `BcdBodyPacker` LEFT_PADDED_0, RIGHT_PADDED_F and LEFT_PADDED_F
where changed to factory methods `leftPadding0()`, `rightPaddingF()` and `leftPaddingF()`
for avoiding of keeping unused instances in memory.

BitmapPacker interface changed. Please use the MsgField.len value instead of
predefined BitmapPacker instances, for example instead of
<s>.defineHeaderBitMapPacker(IfbBitmapPacker.L16)]</s>
please use
```
.defineName(BITMAP_NAME)
.defineHeaderBitmapPacker(IfbBitmapPacker.getInstance())
```   

FieldBuilder.defineHeaderBitMapPacker changed to .defineHeaderBitmapPacker

FieldFiller.<b>from</b>(MsgPair) renamed to FieldFiller.<b>newInstance</b>(MsgPair)
and FieldFiller.<b>get</b>(MsgValue, MsgField) renamed to FieldFiller.<b>newInstance</b>(MsgValue, MsgField)
as described in https://stackoverflow.com/questions/3368830/how-to-name-factory-like-methods.

MsgFieldType LEN_VAL_BIT_SET deleted, please use the LEN_VAL type instead.

Default static instances of `BcdLengthPacker.L`, `LL` and so on replaced with `BcdLengthPacker.getInstance(int numBytes)` factory method.

Default static instances of `EbcdicDecimalLengthPacker.L`, `LL` and so on replaced with `EbcdicDecimalLengthPacker.getInstance(int numBytes)` factory method.

Interface `LengthPacker` changed, `Integer` is used instead of `int` because in some cases
the `lenLength` value is defined in the class constructor and it has a fixed - length value.
