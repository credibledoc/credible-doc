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
as described in https://stackoverflow.com/questions/3368830/how-to-name-factory-like-methods

