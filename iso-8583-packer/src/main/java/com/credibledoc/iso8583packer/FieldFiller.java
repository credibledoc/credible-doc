package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.bcd.BcdService;
import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.header.HeaderField;
import com.credibledoc.iso8583packer.header.HeaderValue;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgPair;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.navigator.NavigatorService;
import com.credibledoc.iso8583packer.offset.Offset;
import com.credibledoc.iso8583packer.tag.TagPacker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * This builder helps to fill the existing {@link MsgField} definition with data. It contains
 * two state objects, the {@link #msgValue} and {@link #msgField}.
 * <p>
 * The {@link MsgField} definition can be created by {@link FieldBuilder}.
 *
 * @author Kyrylo Semenko
 */
public class FieldFiller {

    private static final int INITIAL_SIZE_100_BYTES = 100;
    private static final String PARTIAL_DUMP = "\nPartial dump:\n";
    private static final String ROOT_MSG_FIELD = "\nRoot MsgField:\n";
    private static final String THE_MSG_FIELD = "The MsgField '";

    /**
     * The builder state. Contains an object created from the {@link #msgField} template. This field will be a part
     * of object graph for packing and unpacking this graph from / to a byte array. You can show this graph by calling
     * the {@link com.credibledoc.iso8583packer.dump.DumpService#dumpMsgValue(MsgField, MsgValue, boolean)} method.
     */
    private MsgValue msgValue;

    /**
     * The builder state. Contains an object created by the {@link FieldBuilder} builder. It contains a graph of
     * {@link MsgField}s connected to each other. This graph defines rules of packing and unpacking of bytes from / to
     * a {@link #msgValue} object graph. You can show this graph by calling
     * the {@link com.credibledoc.iso8583packer.dump.DumpService#dumpMsgField(MsgField)} method.
     */
    private MsgField msgField;

    /**
     * Please do not create instances of this builder. It uses for internal purposes only,
     * see the {@link #from(MsgField)} method.
     */
    private FieldFiller() {
        // empty
    }

    /**
     * Create a new instance of {@link MsgValue} from the {@link MsgField} definition for filling it with data.
     * <p>
     * Example of usage:
     * <pre>
     *     FieldFiller fieldFiller = // TODO Kyrylo Semenko
     * </pre>
     * 
     * How to fill the data to the {@link FieldBuilder}? For example see the example:
     * <pre>
     *     // TODO Kyrylo Semenko - documentation
     * </pre>
     *
     * @param definition a template created by {@link FieldBuilder}.
     * @return A new instance of this {@link FieldFiller} with {@link #msgValue} and {@link #msgField} in its context.
     */
    public static FieldFiller from(MsgField definition) {
        FieldFiller fieldFiller = new FieldFiller();
        fieldFiller.msgField = definition;
        fieldFiller.msgValue = NavigatorService.newFromNameAndTagNum(definition);
        return fieldFiller;
    }

    /**
     * Create a new instance of {@link FieldFiller}.
     *
     * @param msgValue      will be set to the {@link FieldFiller#msgValue}.
     * @param msgField will be set to the {@link FieldFiller#msgField}.
     * @return The new created {@link FieldFiller} with the {@link #msgValue} and {@link #msgField} in its context.
     */
    public static FieldFiller get(MsgValue msgValue, MsgField msgField) {
        try {
            MsgPair msgPair = new MsgPair(msgField, msgValue);
            NavigatorService.validateSameNamesAndTagNum(msgPair);
            FieldFiller fieldFiller = new FieldFiller();
            fieldFiller.msgValue = msgValue;
            fieldFiller.msgField = msgField;
            return fieldFiller;
        } catch (Exception e) {
            MsgField rootMsgField = NavigatorService.findRoot(msgField);
            throw new PackerRuntimeException("Exception in method get(msgValue, msgField): " + e.getMessage() + "\n" +
                    "\nThe root MsgField:\n" + DumpService.dumpMsgField(rootMsgField) +
                    "\nMsgValue:\n" + DumpService.dumpMsgValue(msgField, msgValue, true) + "\n", e);
        }
    }

    /**
     * Unpack the bytes started from offset to a new {@link MsgField} using its definition from the third argument.
     * @param bytes the source bytes.
     * @param offset the index where the field starts in the bytes.
     * @param msgField the definition of the {@link MsgField} structure.
     * @return The unpacked {@link MsgValue}.
     */
    public static MsgValue unpack(byte[] bytes, int offset, MsgField msgField) {
        MsgValue msgValue = NavigatorService.newFromNameAndTagNum(msgField);
        try {
            Offset offsetObject = new Offset();
            offsetObject.setValue(offset);
            MsgPair msgPair = new MsgPair(msgField, msgValue);
            unpackFieldRecursively(bytes, offsetObject, msgPair);
            return msgValue;
        } catch (Exception e) {
            String dump = PARTIAL_DUMP + DumpService.dumpMsgValue(msgField, msgValue, true) +
                ROOT_MSG_FIELD + DumpService.dumpMsgField(NavigatorService.findRoot(msgField));
            throw new PackerRuntimeException("Cannot unpack field: " + NavigatorService.generatePath(msgValue) + dump, e);
        }
    }

    /**
     * @param bytes the source bytes.
     * @param offset the index where the field starts in the bytes.
     * @param msgPair the definition of the {@link MsgField} structure and the field values.
     * @throws Exception in case of packing problems
     */
    private static void unpackFieldRecursively(byte[] bytes, Offset offset, MsgPair msgPair) throws Exception {
        NavigatorService.validateSameNamesAndTagNum(msgPair);
        Integer rawDataLength = null;
        if (msgPair.getMsgField().getType() == MsgFieldType.BIT_SET) {
            rawDataLength = unpackBitSet(bytes, offset, msgPair);
        } else if (MsgFieldType.VAL == msgPair.getMsgField().getType()) {
            rawDataLength = unpackValType(bytes, offset, msgPair);
        } else {
            Integer tagLength = getChildTagLengthFromParent(msgPair.getMsgField());
            boolean lengthFirst = MsgFieldType.getLengthFirstTypes().contains(msgPair.getMsgField().getType());
            Integer tagNum = null;

            if (lengthFirst) {
                rawDataLength = unpackLength(bytes, offset, msgPair) - tagLength;
            }

            if (MsgFieldType.getTaggedTypes().contains(msgPair.getMsgField().getType())) {
                tagNum = unpackTagNum(bytes, offset, msgPair, tagLength);
            }

            boolean tagNumUnpackedButIsDifferent = tagNum != null && (
                    msgPair.getMsgField().getTagNum() == null || !tagNum.equals(msgPair.getMsgField().getTagNum()));

            if (tagNumUnpackedButIsDifferent) {
                msgPair = replaceWithSibling(msgPair, tagNum);
            }

            if (MsgFieldType.getTaggedTypes().contains(msgPair.getMsgField().getType())) {
                unpackTagBytes(bytes, offset, msgPair, tagLength, tagNum);
            }

            if (!lengthFirst) {
                rawDataLength = unpackLength(bytes, offset, msgPair);
            }

            // unpack field body
            unpackBodyBytes(bytes, offset, msgPair, rawDataLength);
        }

        if (msgPair.getMsgField().getChildren() == null) {
            // set value, but for leaves (outer children) only.
            setValueToLeaf(bytes, offset, msgPair, rawDataLength);
        } else {
            unpackChildren(bytes, offset, msgPair, rawDataLength);
            return;
        }
        offset.add(rawDataLength);
    }

    private static Integer unpackValType(byte[] bytes, Offset offset, MsgPair msgPair) {
        Integer rawDataLength;
        MsgPair nextEmptyMsgPair = getNextEmptyMsgPairForValType(msgPair);
        rawDataLength = nextEmptyMsgPair.getMsgField().getLen();
        unpackBodyBytes(bytes, offset, nextEmptyMsgPair, rawDataLength);

        if (msgPair.getMsgField().getParent() == null) {
            return rawDataLength;
        }
        // set relationship
        msgPair.getMsgValue().getParent().getChildren().add(nextEmptyMsgPair.getMsgValue());
        nextEmptyMsgPair.getMsgValue().setParent(msgPair.getMsgValue().getParent());

        // set current pair
        msgPair.setMsgField(nextEmptyMsgPair.getMsgField());
        msgPair.setMsgValue(nextEmptyMsgPair.getMsgValue());
        return rawDataLength;
    }

    private static LengthPacker getLengthPackerFromParentOrSelfOrThrowException(MsgField msgField) {
        boolean parentContainsChildrenLengthPacker = msgField.getParent() != null &&
                msgField.getParent().getChildrenLengthPacker() != null;
        if (parentContainsChildrenLengthPacker) {
            return msgField.getParent().getChildrenLengthPacker();
        }
        
        if (msgField.getHeaderField().getLengthPacker() == null) {
            throw new PackerRuntimeException("Property lengthPacker is not defined. Please define it by calling  " +
                    "the .defineHeaderLengthPacker() method. " +
                    "Current MsgField: " + NavigatorService.generatePath(msgField));
        }
        
        return msgField.getHeaderField().getLengthPacker();
    }

    private static void unpackBodyBytes(byte[] bytes, Offset offset, MsgPair msgPair, Integer rawDataLength) {
        try {
            byte[] rawData = new byte[rawDataLength];
            System.arraycopy(bytes, offset.getValue(), rawData, 0, rawData.length);
            msgPair.getMsgValue().setBodyBytes(rawData);
        } catch (Exception e) {
            String path = NavigatorService.getPathRecursively(msgPair.getMsgField());
            throw new PackerRuntimeException("Current MsgField: '" + path + "', offset: '" + offset.getValue() +
                "', rawDataLength: '" + rawDataLength + "'", e);
        }
    }

    private static MsgPair getNextEmptyMsgPairForValType(MsgPair msgPair) {
        MsgField msgField = msgPair.getMsgField();
        MsgField parentMsgField = msgField.getParent();
        if (parentMsgField == null) {
            return msgPair;
        }
        MsgValue msgValue = msgPair.getMsgValue();
        // Remove last MsgValue because this item is empty and not accepted for MsgFieldType==VAL.
        msgValue.getParent().getChildren().remove(msgValue.getParent().getChildren().size() - 1);
        for (MsgField siblingMsgField : parentMsgField.getChildren()) {
            MsgValue siblingMsgValue = NavigatorService.findByName(msgValue.getParent().getChildren(),
                    siblingMsgField.getName());
            if (siblingMsgValue == null) {
                if (siblingMsgField.getType() != MsgFieldType.VAL) {
                    throw new PackerRuntimeException("Expected VAL type but found " + siblingMsgField.getType() +
                            ". SiblingMsgField '" + NavigatorService.getPathRecursively(siblingMsgField) + "'.");
                }
                siblingMsgValue = NavigatorService.newFromNameAndTagNum(siblingMsgField);
                return new MsgPair(siblingMsgField, siblingMsgValue);
            }
            if (siblingMsgValue.getBodyBytes() == null) {
                return new MsgPair(siblingMsgField, siblingMsgValue);
            }
        }
        
        throw new PackerRuntimeException("Cannot find sibling with empty bodyBytes for MsgField '" +
                NavigatorService.getPathRecursively(msgField) +
                "'. It means cannot unpack the remaining bytes to MsgValue.");
    }

    private static void unpackChildren(byte[] bytes, Offset offset, MsgPair msgPair, Integer rawDataLength) throws Exception {
        int offsetWithChildren = offset.getValue() + rawDataLength;
        if (rawDataLength > 0) {
            msgPair.getMsgValue().setChildren(new ArrayList<>());
        }
        while (offset.getValue() < offsetWithChildren) {
            MsgField msgFieldFirstChild = msgPair.getMsgField().getChildren().get(0);
            MsgValue msgValueChild = NavigatorService.newFromNameAndTagNum(msgFieldFirstChild);
            msgPair.getMsgValue().getChildren().add(msgValueChild);
            msgValueChild.setParent(msgPair.getMsgValue());
            MsgPair msgPairChild = new MsgPair(msgFieldFirstChild, msgValueChild);
            unpackFieldRecursively(bytes, offset, msgPairChild);
        }
        if (offset.getValue() != offsetWithChildren) {
            throw new PackerRuntimeException("Expected end of children is '" + offsetWithChildren +
                    "' but current offset is '" + offset.getValue() +
                    "'. These values should be equal.");
        }
    }

    private static void unpackTagBytes(byte[] bytes, Offset offset, MsgPair msgPair, Integer fieldTagLength,
                                              Integer tagNum) {
        byte[] tagBytes = new byte[fieldTagLength];
        System.arraycopy(bytes, offset.getValue(), tagBytes, 0, tagBytes.length);
        offset.add(fieldTagLength);
        HeaderValue headerValue = msgPair.getMsgValue().getHeaderValue();
        headerValue.setTagBytes(tagBytes);
        msgPair.getMsgValue().setTagNum(tagNum);
    }

    private static Integer unpackTagNum(byte[] bytes, Offset offset, MsgPair msgPair, Integer fieldTagLength) {
        Integer tagNum;
        TagPacker tagPacker = NavigatorService.getTagPackerFromParent(msgPair.getMsgField());
        if (tagPacker == null) {
            tagNum = msgPair.getMsgField().getTagNum();
        } else {
            tagNum = tagPacker.unpack(bytes, offset.getValue(), fieldTagLength);
        }
        return tagNum;
    }

    private static int unpackBitSet(byte[] bytes, Offset offset, MsgPair msgPair) throws Exception {
        List<Integer> tagNums = getTagNumsFromBitSet(bytes, offset, msgPair);
        for (int nextTagNum : tagNums) {
            MsgField msgFieldChild = findChildByTagNum(msgPair.getMsgField(), nextTagNum);
            MsgValue msgValueChild = NavigatorService.newFromNameAndTagNum(msgFieldChild);
            List<MsgValue> children = msgPair.getMsgValue().getChildren();
            if (children == null) {
                children = new ArrayList<>();
                msgPair.getMsgValue().setChildren(children);
            }
            children.add(msgValueChild);
            msgValueChild.setParent(msgPair.getMsgValue());
            MsgPair msgPairChild = new MsgPair(msgFieldChild, msgValueChild);
            unpackFieldRecursively(bytes, offset, msgPairChild);
        }
        return 0;
    }

    private static int unpackLength(byte[] bytes, Offset offset, MsgPair msgPair) {
        LengthPacker lengthPacker = getLengthPackerFromParentOrSelfOrThrowException(msgPair.getMsgField());
        int lenLength = lengthPacker.calculateLenLength(bytes, offset.getValue());
        byte[] lengthBytes = new byte[lenLength];
        System.arraycopy(bytes, offset.getValue(), lengthBytes, 0, lengthBytes.length);
        msgPair.getMsgValue().getHeaderValue().setLengthBytes(lengthBytes);
        int rawDataLength = lengthPacker.unpack(bytes, offset.getValue(), lenLength);
        offset.add(lenLength);
        return rawDataLength;
    }

    private static void setValueToLeaf(byte[] bytes, Offset offset, MsgPair msgPair, int bodyBytesLength) {
        BodyPacker bodyPacker = msgPair.getMsgField().getBodyPacker();
        if (bodyPacker == null) {
            throw new PackerRuntimeException("BodyPacker not found. Please call defineBodyPacker(...) method " +
                    "of the FieldBuilder, for example " +
                    "MsgField subfield35 = FieldBuilder.builder().defineBodyPacker(HexBodyPacker.INSTANCE).\n" +
                    "MsgValue: " + NavigatorService.getPathRecursively(msgPair.getMsgValue()));
        }
        Object bodyValue = bodyPacker.unpack(bytes, offset.getValue(), bodyBytesLength);
        msgPair.getMsgValue().setBodyValue(bodyValue);
    }

    private static MsgPair replaceWithSibling(MsgPair msgPair, Integer tagNum) {
        MsgPair result = new MsgPair();
        MsgField msgFieldSibling = findSiblingByTagNum(tagNum, msgPair.getMsgField());
        if (msgFieldSibling == null) {
            throwSiblingNotFound(msgPair, tagNum);
        }
        MsgValue oldMsgValue = msgPair.getMsgValue();
        MsgValue parentMsgValue = oldMsgValue.getParent();
        result.setMsgField(msgFieldSibling);
        assert msgFieldSibling != null;
        MsgValue newMsgValue = NavigatorService.newFromNameAndTagNum(msgFieldSibling);
        if (parentMsgValue != null) {
            parentMsgValue.getChildren().remove(oldMsgValue);
            parentMsgValue.getChildren().add(newMsgValue);
            newMsgValue.setParent(parentMsgValue);
            result.setMsgValue(newMsgValue);
        }
        if (oldMsgValue.getHeaderValue() != null) {
            newMsgValue.setHeaderValue(oldMsgValue.getHeaderValue());
        }
        return result;
    }

    private static List<Integer> getTagNumsFromBitSet(byte[] bytes, Offset offset, MsgPair msgPair) {
        // this is IsoMsg, so tagNums are in the header BitSet
        HeaderField headerField = msgPair.getMsgField().getHeaderField();
        BitSet bitSetDefinition = headerField.getBitSet();

        // unpack
        BitmapPacker bitmapPacker = headerField.getBitMapPacker();
        if (bitmapPacker == null) {
            throw new PackerRuntimeException("Please call the defineHeaderBitMapPacker(...) " +
                    "method for this field " + NavigatorService.getPathRecursively(msgPair.getMsgValue()));
                    
        }
        int consumed = bitmapPacker.unpack(headerField, bytes, offset.getValue());
        offset.add(consumed);

        return getTagNumsAndValidateBitSet(msgPair, bitSetDefinition);
    }

    private static MsgField findChildByTagNum(MsgField msgField, int nextTagNum) {
        for (MsgField child : msgField.getChildren()) {
            if (nextTagNum == child.getTagNum()) {
                return child;
            }
        }
        throw new PackerRuntimeException("Cannot find child with tagNum '" + nextTagNum +
                "' in the msgField " + NavigatorService.getPathRecursively(msgField));
    }

    private static List<Integer> getTagNumsAndValidateBitSet(MsgPair msgPair, BitSet bitSetDefinition) {
        BitSet unpackedBitSet = msgPair.getMsgField().getHeaderField().getBitSet();
        List<Integer> tagNums = new ArrayList<>();
        int maxTagNum = getMaxTagNum(msgPair.getMsgField().getChildren());
        for (int nextTagNum = 0; nextTagNum <= maxTagNum; nextTagNum++) {
            if (unpackedBitSet.get(nextTagNum) && !bitSetDefinition.get(nextTagNum)) {
                throw new PackerRuntimeException("Unpacked bitSet contains tagNum '" + nextTagNum +
                        "', but bitSetDefinition does not contains it. " +
                        "Please define this tagNum in the field '" + NavigatorService.getPathRecursively(msgPair.getMsgValue()) +
                        "' header bitSet");
            }
            if (unpackedBitSet.get(nextTagNum)) {
                tagNums.add(nextTagNum);
            }
        }
        return tagNums;
    }

    private static void throwSiblingNotFound(MsgPair msgPair, Integer tagNum) {
        MsgField msgField = msgPair.getMsgField();
        MsgValue msgValue = msgPair.getMsgValue();
        String previousFieldCause = "";
        MsgField parentMsgField = msgField.getParent();
        if (parentMsgField != null && parentMsgField.getHeaderField() != null) {
            previousFieldCause = " \nNext cause is incorrect implementation of the " + parentMsgField.getHeaderField().getLengthPacker().getClass().getSimpleName() +
                    ".calculateLenLength(bytes, offset) method of the previous '" + NavigatorService.getPathRecursively(parentMsgField) +
                    "' field.";
        }
        String tagPackerClass = "null";
        if (msgField.getParent() != null && msgField.getParent().getChildrenTagPacker() != null) {
            tagPackerClass = msgField.getParent().getChildrenTagPacker().getClass().getSimpleName();
        }
        throw new PackerRuntimeException("Cannot find a sibling with tagNum '" + tagNum +
                "' for the '" + NavigatorService.getPathRecursively(msgValue) +
                "' Field. Its parent '" + NavigatorService.getPathRecursively(parentMsgField) +
                "' has no child with such tagNum. \nThere are few possible causes of this error. " +
                "\nFirst cause is incorrect implementation of " + tagPackerClass +
                ".unpack() method used for unpacking tagNum from bytes. " +
                "\nSecond cause is undefined child with tagNum '" + tagNum +
                "' and with parent '" + NavigatorService.getPathRecursively(parentMsgField) +
                "' in the MsgField definition." + previousFieldCause + " \nNext cause can be the order of tagNum and " +
                "length subfields where some MsgFields have the first tagNum and the second length but other " +
                "MsgFields wise versa, for example F0F0F3 F9F3 F0, where F0F0F3 is the length 003, F9F3 is " +
                "the tagNum 93 and F0 is the body." +
                " \nNext cause is wrong place of actual context inside the rootMsgField, actual place is " +
                NavigatorService.getPathRecursively(msgField));
    }

    private static int getMaxTagNum(List<MsgField> children) {
        int result = 0;
        for (MsgField msgField : children) {
            if (msgField.getTagNum() > result) {
                result = msgField.getTagNum();
            }
        }
        return result;
    }

    private static MsgField findSiblingByTagNum(int tagNum, MsgField msgField) {
        MsgField parent = msgField.getParent();
        for (MsgField child : parent.getChildren()) {
            if (child.getTagNum() == tagNum) {
                return child;
            }
        }
        return null;
    }

    /**
     * Create a new instance of {@link FieldFiller} and set msgPair values to its
     * {@link #msgField} and {@link #msgValue} context objects.
     * @param msgPair contains {@link MsgField} and {@link MsgValue}.
     * @return The created instance.
     */
    public static FieldFiller from(MsgPair msgPair) {
        FieldFiller fieldFiller = new FieldFiller();
        fieldFiller.msgField = msgPair.getMsgField();
        fieldFiller.msgValue = msgPair.getMsgValue();
        return fieldFiller;
    }

    /**
     * Change the current {@link #msgField} and {@link #msgValue} to the new location in the {@link #msgField} graph.
     * <p>
     * The new location will be the child with given name from {@link #msgField}
     * and the <b>first</b> child of the {@link #msgValue} with the name.
     * <p>
     * If the current {@link #msgValue} doesn't have the child, it will be created. Else the found child will be set
     * tho the {@link #msgValue} field.
     *
     * @param childName the {@link #msgField}'s child.
     * @return An existing {@link #msgField}'s child.
     */
    public FieldFiller jumpToChild(String childName) {
        try {
            MsgField msgFieldChild = NavigatorService.getChildOrThrowException(childName, msgField);
            MsgValue msgValueChild = NavigatorService.findByName(msgValue.getChildren(), childName);
            if (msgValueChild == null) {
                msgValueChild = NavigatorService.newFromNameAndTagNum(msgFieldChild);
                msgValueChild.setParent(msgValue);
                if (msgValue.getChildren() == null) {
                    msgValue.setChildren(new ArrayList<>());
                }
                msgValue.getChildren().add(msgValueChild);
            }
            this.msgValue = msgValueChild;
            this.msgField = msgFieldChild;
            return this;
        } catch (Exception e) {
            MsgField rootMsgField = NavigatorService.findRoot(msgField);
            throw new PackerRuntimeException("Exception message: " + e.getMessage() + "\nCannot find a child." +
                ROOT_MSG_FIELD + DumpService.dumpMsgField(rootMsgField), e);
        }
    }

    /**
     * Set the {@link MsgValue#setBodyValue(Object)} from the argument to the {@link #msgValue}.
     * 
     * Example of usage:
     * <pre>
     *     FieldFiller.from(msgPair)
     *                 .jumpToChild("child_name")
     *                 .setValue("some_value");
     * </pre>
     * 
     * <p>
     * Set the {@link MsgValue#setBodyBytes(byte[])} from this bodyValue to the field, see the {@link #setBytes(Object)} method.
     * <p>
     * If this field contains {@link MsgField#getHeaderField()} then properties of this header, for example
     * {@link HeaderValue#setTagBytes(byte[])} or {@link HeaderValue#setLengthBytes(byte[])} are set.
     * Length and tag are not mandatory.
     * See the {@link #setHeader(byte[], HeaderValue, HeaderField)}  method.
     *
     * @param bodyValue can be 'null' for unset.
     * @return The current {@link FieldFiller} with the same {@link #msgValue} and {@link #msgField} in its context.
     */
    public FieldFiller setValue(Object bodyValue) {
        if (msgField.getChildren() != null && !msgField.getChildren().isEmpty()) {
            throw new PackerRuntimeException("Cannot set bodyValue to fields with children. Values can only contain " +
                    "leaf fields. Field: " + NavigatorService.getPathRecursively(msgField) + ", bodyValue: " + bodyValue);
        }
        if (bodyValue == null) {
            msgValue.setBodyBytes(null);
            if (msgValue.getHeaderValue() != null) {
                HeaderValue headerValue = msgValue.getHeaderValue();
                headerValue.setLengthBytes(null);
            }
            msgValue.setBodyValue(null);
            return this;
        }
        try {
            msgValue.setBodyValue(bodyValue);
            byte[] valueBytes = setBytes(bodyValue);

            HeaderField headerField = msgField.getHeaderField();
            HeaderValue headerValue = msgValue.getHeaderValue();
            if (headerValue != null) {
                setHeader(valueBytes, headerValue, headerField);
            }

            return this;
        } catch (Exception e) {
            MsgValue rootMsgValue = NavigatorService.findRoot(msgValue);
            MsgField rootMsgField = NavigatorService.findRoot(msgField);
            throw new PackerRuntimeException("Exception message: " + e.getMessage() + "\nCannot set bodyValue '" + bodyValue +
                    "' to field '" + NavigatorService.getPathRecursively(msgField) + "'" +
                    "\nRoot MsgValue:\n" + DumpService.dumpMsgValue(rootMsgField, rootMsgValue, true) +
                    "\nThe MsgField:\n" + DumpService.dumpMsgField(msgField) +
                ROOT_MSG_FIELD + DumpService.dumpMsgField(rootMsgField), e);
        }
    }

    private byte[] setBytes(Object bodyValue) {
        BodyPacker bodyPacker = msgField.getBodyPacker();

        if (bodyPacker == null) {
            throw new PackerRuntimeException("BodyPacker not found. Please call setBodyPacker(...) " +
                    "method, for example " +
                    "MsgField subfield35 = FieldBuilder.builder().BodyPacker(HexBodyPacker.INSTANCE)...\n" +
                    "MsgField: " + NavigatorService.getPathRecursively(msgField));
        }
        
        byte[] bodyBytes;
        int bodyLength = bodyPacker.getPackedLength(bodyValue);

        Integer exactlyLength = msgField.getExactlyLength();
        if (exactlyLength != null && bodyLength != exactlyLength) {
            throw new PackerRuntimeException(THE_MSG_FIELD + NavigatorService.getPathRecursively(msgField) +
                    "' contains the 'exactlyLength' definition with value '" + exactlyLength +
                    "', but the bodyValue length '" + bodyLength + "' is not the same.");
        }

        Integer maxLen = msgField.getMaxLen();
        if (maxLen != null && maxLen < bodyLength) {
            throw new PackerRuntimeException(THE_MSG_FIELD + NavigatorService.getPathRecursively(msgField) +
                    "' contains the 'maxLen' definition with value '" + maxLen +
                    "', but its bodyValue length '" + bodyLength + "' is greater.");
        }
        
        Integer len = msgField.getLen();
        if (len != null && bodyLength != len) {
            throw new PackerRuntimeException(THE_MSG_FIELD + NavigatorService.getPathRecursively(msgField) +
                "' contains the 'len' definition with value '" + len +
                "', but its bodyValue length '" + bodyLength + "' is different.");
        }
        
        bodyBytes = new byte[bodyLength];
        bodyPacker.pack(bodyValue, bodyBytes, 0);
        msgValue.setBodyBytes(bodyBytes);
        return bodyBytes;
    }

    private void setHeader(byte[] valueBytes, HeaderValue headerValue, HeaderField headerField) {
        MsgField msgFieldParent = msgField.getParent();
        if (msgFieldParent == null && MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
            throw new PackerRuntimeException("This MsgField has no parent. Please use FieldBuilder.get(field) and " +
                    "call the setParent() method before setValue() method " +
                    "because parent.getChildTagLength() is used for header creation");
        }
        Integer tagLength = getChildTagLengthFromParent(msgField);
        if (tagLength == null && MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
            throw new PackerRuntimeException("Property childTagLength is mandatory for parent of this field '" +
                    NavigatorService.getPathRecursively(msgField) + "'. " +
                    "Please use FieldBuilder.get(field) and set the setChildTagLen(?) value to this field parent. " +
                    "This property should not be set in leaf fields only.");
        }
        int lenLength = 0;

        LengthPacker lengthPacker;

        Integer fieldNum = msgValue.getTagNum();
        if (msgField.getType() == MsgFieldType.LEN_TAG_VAL) {
            // field parent contains lengthPacker, hence length precedes tagNum
            assert msgFieldParent != null;
            lengthPacker = msgFieldParent.getChildrenLengthPacker();
            packTagBytesToHeader(headerValue, tagLength, fieldNum);
            int tagAndValueLength = valueBytes.length + headerValue.getTagBytes().length;
            lenLength = lengthPacker.calculateLenLength(tagAndValueLength);
            byte[] lengthBytes = lengthPacker.pack(tagAndValueLength, lenLength);
            headerValue.setLengthBytes(lengthBytes);
        } else {
            // field itself contains lengthPacker
            lengthPacker = headerField.getLengthPacker();
            if (lengthPacker != null) {
                lenLength = lengthPacker.calculateLenLength(valueBytes.length);
            }
            if (MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
                packTagBytesToHeader(headerValue, tagLength, fieldNum);
            }

            if (lengthPacker != null) {
                byte[] lengthBytes = lengthPacker.pack(valueBytes.length, lenLength);
                headerValue.setLengthBytes(lengthBytes);
            }
        }
    }

    private void packTagBytesToHeader(HeaderValue headerValue, Integer tagLength, Integer fieldNum) {
        if (tagLength == 0) {
            headerValue.setTagBytes(new byte[0]);
            return;
        }
        TagPacker tagPacker = NavigatorService.getTagPackerFromParent(msgField);
        byte[] tagBytes = tagPacker.pack(fieldNum, tagLength);
        headerValue.setTagBytes(tagBytes);
    }

    private static Integer getChildTagLengthFromParent(MsgField msgField) {
        if (MsgFieldType.isNotTaggedType(msgField)) {
            return 0;
        }
        if (msgField.getParent() != null) {
            return msgField.getParent().getChildTagLength();
        }
        throw new PackerRuntimeException("This field '" + NavigatorService.getPathRecursively(msgField) +
                "' has no parent. The parent is mandatory for obtaining of the ChildTagLength property. " +
                "Please create a new Field and set it as a parent. Parent is not mandatory for fields which " +
                "contains the bitSet property.");
    }

    /**
     * Change the current {@link #msgValue} and {@link #msgField} to the new location. If the field sibling is not found,
     * create a new one.
     *
     * @param siblingName the sibling name of the current {@link #msgValue} and {@link #msgField}.
     * @return The current builder instance with the new {@link #msgValue} and {@link #msgField}.
     */
    public FieldFiller jumpToSibling(String siblingName) {
        try {
            MsgField msgFieldSibling = NavigatorService.getSiblingOrThrowException(siblingName, msgField);
            MsgValue parentMsgValue = msgValue.getParent();
            MsgValue siblingMsgValue = NavigatorService.findByName(parentMsgValue.getChildren(), siblingName);
            if (siblingMsgValue == null) {
                siblingMsgValue = NavigatorService.newFromNameAndTagNum(msgFieldSibling);
                siblingMsgValue.setParent(msgValue.getParent());
                msgValue.getParent().getChildren().add(siblingMsgValue);
                sortFieldChildren(msgValue.getParent(), msgField.getParent());
            }
            msgValue = siblingMsgValue;
            msgField = msgFieldSibling;
            return this;
        } catch (Exception e) {
            MsgValue rootMsgValue = NavigatorService.findRoot(msgValue);
            MsgField rootMsgField = NavigatorService.findRoot(msgField);
            throw new PackerRuntimeException("Exception: " + e.getMessage() + "\nCannot jumpToSibling '" + siblingName +
                    "' of the message definition." +
                    "\nMsgValue:\n" + DumpService.dumpMsgValue(rootMsgField, rootMsgValue, true) +
                    "\nMsgField:\n" + DumpService.dumpMsgField(rootMsgField) + "\n", e);
        }
    }

    /**
     * @param msgValue to be sorted
     * @param msgField how to sort
     */
    private void sortFieldChildren(MsgValue msgValue, MsgField msgField) {
        List<MsgValue> msgValueChildren = msgValue.getChildren();
        List<MsgField> msgFieldChildren = msgField.getChildren();
        List<MsgValue> result = new ArrayList<>(msgValueChildren.size());
        for (MsgField def : msgFieldChildren) {
            String name = def.getName();
            MsgValue msgFieldChild = remove(name, msgValueChildren);
            while (msgFieldChild != null) {
                result.add(msgFieldChild);
                msgFieldChild = remove(name, msgValueChildren);
            }
            if (msgValueChildren.isEmpty()) {
                msgValue.setChildren(result);
                return;
            }
        }
        throw new PackerRuntimeException("Cannot find fields in MsgValue. Fields: " + msgValueChildren + "");
    }

    private MsgValue remove(String name, List<MsgValue> msgValueChildren) {
        for (MsgValue child : msgValueChildren) {
            if (name.equals(child.getName())) {
                msgValueChildren.remove(child);
                return child;
            }
        }
        return null;
    }

    /**
     * @return The {@link #msgValue} value;
     */
    public MsgValue getCurrentMsgValue() {
        return msgValue;
    }

    /**
     * @return The {@link #msgField} value;
     */
    public MsgField getCurrentMsgField() {
        return msgField;
    }

    /**
     * Add subfields to the current {@link #msgValue}. Old children will be deleted.
     *
     * @param subfields new children
     * @return The current instance with {@link #msgValue} and {@link #msgField} in its context.
     */
    public FieldFiller setChildren(List<MsgValue> subfields) {
        for (MsgValue child : subfields) {
            child.setParent(msgValue);
        }
        msgValue.setChildren(subfields);
        return this;
    }

    /**
     * Navigate to the root {@link #msgValue} and {@link #msgField}.
     * @return The current instance of {@link FieldFiller} with root of {@link #msgValue} and corresponding node of
     * {@link #msgField} in its context.
     */
    public FieldFiller jumpToRoot() {
        msgValue = NavigatorService.findRoot(msgValue);
        msgField = NavigatorService.findByNameAndTagNumOrThrowException(msgField, msgValue);
        return this;
    }

    /**
     * Pack this {@link #msgValue} to bytes for sending to Host.
     * @return Bytes created from the {@link #msgValue}.
     */
    public byte[] pack() {
        try {
            ByteArrayOutputStream result = packRecursively(msgValue, msgField);
            return result.toByteArray();
        } catch (Exception e) {
            MsgValue rootMsgValue = NavigatorService.findRoot(msgValue);
            MsgField rootMsgField = NavigatorService.findRoot(msgField);
            throw new PackerRuntimeException("Exception: " + e.getMessage() + "\n" +
                    "Cannot pack field '" + NavigatorService.getPathRecursively(msgValue) + "'" +
                PARTIAL_DUMP + DumpService.dumpMsgValue(rootMsgField, rootMsgValue, true) +
                    "\nMsgField:\n" + DumpService.dumpMsgField(msgField) + "\n", e);
        }
    }

    private static ByteArrayOutputStream packRecursively(MsgValue msgValue, MsgField msgField) throws IOException {
        int maxLen = msgField.getMaxLen() != null ? msgField.getMaxLen() : INITIAL_SIZE_100_BYTES;
        ByteArrayOutputStream messageBytes = new ByteArrayOutputStream(maxLen);
        if (msgValue.getChildren() != null) {
            ByteArrayOutputStream childrenBytes = new ByteArrayOutputStream(maxLen);
            if (MsgFieldType.BIT_SET == msgField.getType()) {
                packBitmap(msgValue, msgField, messageBytes);
            }
            for (MsgValue nextMsgValue : msgValue.getChildren()) {
                MsgField msgFieldChild = NavigatorService.findByName(msgField.getChildren(), nextMsgValue.getName());
                ByteArrayOutputStream childArray = packRecursively(nextMsgValue, msgFieldChild);
                childArray.writeTo(childrenBytes);
            }
            byte[] bytes = childrenBytes.toByteArray();
            if (bytes.length != 0) {
                packBodyBytesAndLengthAndTagNum(msgValue, msgField, messageBytes, bytes);
            }
            childrenBytes.writeTo(messageBytes);
        } else {
            if (msgValue.getBodyBytes() != null) {
                // Do not pack empty fields
                ByteArrayOutputStream headerAndValue = packHeaderAndValue(msgValue, msgField);
                headerAndValue.writeTo(messageBytes);
            }
        }
        return messageBytes;
    }

    private static void packBitmap(MsgValue msgValue, MsgField msgField, ByteArrayOutputStream messageBytes) throws IOException {
        BitmapPacker bitmapPacker = msgField.getHeaderField().getBitMapPacker();
        if (bitmapPacker == null) {
            throw new PackerRuntimeException("The value of '" + BitmapPacker.class.getSimpleName() +
                "' type is mandatory for '" + MsgFieldType.class.getSimpleName() + 
                "' '" + MsgFieldType.BIT_SET + "' type. " +
                "Please call the defineBitmapPacker(...) method.");
        }
        BitSet bitSet = new BitSet();
        for (MsgField nextMsgField : msgField.getChildren()) {
            bitSet.set(nextMsgField.getTagNum());
        }
        msgField.getHeaderField().setBitSet(bitSet);
        msgValue.getHeaderValue().setBitSet(bitSet);
        messageBytes.write(bitmapPacker.pack(bitSet));
    }

    private static void packBodyBytesAndLengthAndTagNum(MsgValue msgValue, MsgField msgField, ByteArrayOutputStream messageBytes, byte[] bytes) throws IOException {
        msgValue.setBodyBytes(bytes);
        LengthPacker lengthPacker;
        boolean lengthPrecedesTagNum = msgField.getType() == MsgFieldType.LEN_TAG_VAL;
        if (MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
            setTagBytes(msgValue, msgField);
        }
        if (MsgFieldType.getLengthTypes().contains(msgField.getType())) {
            lengthPacker = getLengthPackerFromParentOrSelfOrThrowException(msgField);
            packLength(msgValue, lengthPacker, bytes, lengthPrecedesTagNum);
        }

        if (lengthPrecedesTagNum) {
            writeLengthBytesIfAllowed(msgValue, msgField, messageBytes);
            writeTagBytesIfAllowed(msgValue, msgField, messageBytes);
        } else {
            writeTagBytesIfAllowed(msgValue, msgField, messageBytes);
            writeLengthBytesIfAllowed(msgValue, msgField, messageBytes);
        }
    }

    private static void writeTagBytesIfAllowed(MsgValue msgValue, MsgField msgField, ByteArrayOutputStream messageBytes) throws IOException {
        if (MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
            messageBytes.write(msgValue.getHeaderValue().getTagBytes());
        }
    }

    private static void writeLengthBytesIfAllowed(MsgValue msgValue, MsgField msgField,
                                                  ByteArrayOutputStream messageBytes) throws IOException {
        if (MsgFieldType.getLengthTypes().contains(msgField.getType())) {
            messageBytes.write(msgValue.getHeaderValue().getLengthBytes());
        }
    }

    private static ByteArrayOutputStream packHeaderAndValue(MsgValue msgValue, MsgField msgField) throws IOException {
        if (msgValue.getBodyBytes() == null) {
            throw new PackerRuntimeException("Expected non-null MsgValue.bodyBytes but found 'null'. MsgValue path: '" +
                    NavigatorService.getPathRecursively(msgValue) + "'. Please set the value or delete the MsgField. " +
                    "The cause of the exception probably in the setValue() method.");
        }
        int length = msgValue.getBodyBytes().length;
        HeaderValue headerField = msgValue.getHeaderValue();
        ByteArrayOutputStream result = null;
        if (headerField != null) {
            length += headerField.getTagBytes() == null ? 0 : headerField.getTagBytes().length;
            length += headerField.getLengthBytes() == null ? 0 : headerField.getLengthBytes().length;
            result = new ByteArrayOutputStream(length);

            boolean lengthPrecedesTagNum = msgField.getType() == MsgFieldType.LEN_TAG_VAL;

            if (lengthPrecedesTagNum) {
                writeLengthBytesIfExist(result, headerField);
                writeTagBytes(result, headerField);
            } else {
                writeTagBytes(result, headerField);
                writeLengthBytesIfExist(result, headerField);
            }
        }
        if (result == null) {
            result = new ByteArrayOutputStream(length);
        }
        if (msgValue.getBodyBytes() != null) {
            result.write(msgValue.getBodyBytes());
        }
        return result;
    }

    private static void writeTagBytes(ByteArrayOutputStream result, HeaderValue headerField) throws IOException {
        if (headerField.getTagBytes() != null) {
            result.write(headerField.getTagBytes());
        }
    }

    private static void writeLengthBytesIfExist(ByteArrayOutputStream result, HeaderValue headerField) throws IOException {
        if (headerField.getLengthBytes() != null) {
            result.write(headerField.getLengthBytes());
        }
    }

    private static void packLength(MsgValue msgValue, LengthPacker lengthPacker, byte[] bytes, boolean lengthPrecedesTagNum) {
        int lenLength;
        int bytesLength;
        if (lengthPrecedesTagNum) {
            bytesLength = bytes.length + msgValue.getHeaderValue().getTagBytes().length;
        } else {
            bytesLength = bytes.length;
        }
        lenLength = lengthPacker.calculateLenLength(bytesLength);
        byte[] lengthBytes = lengthPacker.pack(bytesLength, lenLength);
        msgValue.getHeaderValue().setLengthBytes(lengthBytes);
    }

    private static void setTagBytes(MsgValue msgValue, MsgField msgField) {
        int tagNum = msgValue.getTagNum();
        Integer childTagLength = getChildTagLengthFromParent(msgField);
        if (childTagLength == null) {
            throw new PackerRuntimeException("The field parent (" + NavigatorService.getPathRecursively(msgValue.getParent()) +
                    ") has no property childTagLength defined." +
                    " The property is used for packing field tagNum bytes. Field: " +
                    NavigatorService.getPathRecursively(msgField));
        }
        TagPacker tagPacker = NavigatorService.getTagPackerFromParent(msgField);
        byte[] tagBytes = tagPacker.pack(tagNum, childTagLength);
        msgValue.getHeaderValue().setTagBytes(tagBytes);
    }

    /**
     * Create a copy from the current {@link #msgValue} and set it to this {@link #msgValue} context.
     * This sibling will have the same {@link MsgField#getName()} and {@link MsgField#getTagNum()} as its sibling.
     *
     * @return The current actual {@link FieldFiller}.
     */
    public FieldFiller cloneSibling() {
        MsgValue clone = NavigatorService.newFromNameAndTagNum(msgField);
        clone.setParent(msgValue.getParent());
        msgValue.getParent().getChildren().add(clone);
        msgValue = clone;
        return this;
    }

    /**
     * Create the new instance and set {@link #msgValue} and {@link #msgField} from the current instance.
     *
     * @return Created instance of {@link FieldFiller}.
     */
    public FieldFiller copyFiller() {
        FieldFiller clone = new FieldFiller();
        clone.msgValue = msgValue;
        clone.msgField = msgField;
        return clone;
    }

    /**
     * Call the {@link #unpack(byte[], int, MsgField)} method with offset 0 and with current {@link #msgField} from the
     * {@link FieldFiller} context.
     *
     * @param bytes will be set as first argument
     * @return The {@link MsgValue} unpacked from the bytes.
     */
    public MsgValue unpack(byte[] bytes) {
        return unpack(bytes, 0, msgField);
    }

    /**
     * @return Existing {@link #msgField} and {@link #msgValue} created previously by the {@link #from(MsgField)} method.
     */
    public MsgPair getCurrentPair() {
        return new MsgPair(this.msgField, this.msgValue);
    }

    public void validateData() {
        try {
            validateRecursively(msgField, msgValue);
            List<MsgField> msgFieldChildren = msgField.getChildren();
            if (msgFieldChildren != null) {
                for (MsgField childMsgField : msgFieldChildren) {
                    MsgValue childMsgValue = NavigatorService.findByName(msgValue.getChildren(),
                        childMsgField.getName());
                    if (childMsgValue != null) {
                        validateRecursively(childMsgField, childMsgValue);
                    }
                }
            }
        } catch (Exception e) {
            String dump = PARTIAL_DUMP + DumpService.dumpMsgValue(msgField, msgValue, true) +
                ROOT_MSG_FIELD + DumpService.dumpMsgField(NavigatorService.findRoot(msgField));
            throw new PackerRuntimeException("Validation failed: " + dump, e);
        }
    }

    private static void validateRecursively(MsgField msgField, MsgValue msgValue) {
        if (msgField.getBodyPacker() instanceof BcdBodyPacker) {
            BcdService.validateIsStringBcdNumber(msgValue);
        }
    }

}
