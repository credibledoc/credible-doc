package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.bcd.BcdService;
import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.header.HeaderField;
import com.credibledoc.iso8583packer.header.HeaderValue;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgPair;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.navigator.Navigator;
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
public class ValueHolder {

    static final int INITIAL_SIZE_100_BYTES = 100;
    static final String PARTIAL_DUMP = "\nPartial dump:\n";
    static final String ROOT_MSG_FIELD = "\nRoot MsgField:\n";
    static final String THE_MSG_FIELD = "The MsgField '";

    /**
     * The builder state. Contains an object created from the {@link #msgField} template. This field will be a part
     * of object graph for packing and unpacking this graph from / to a byte array. You can show this graph by calling
     * the {@link com.credibledoc.iso8583packer.dump.DumpService#dumpMsgValue(MsgField, MsgValue, boolean)} method.
     */
    protected MsgValue msgValue;

    /**
     * The builder state. Contains an object created by the {@link FieldBuilder} builder. It contains a graph of
     * {@link MsgField}s connected to each other. This graph defines rules of packing and unpacking of bytes from / to
     * a {@link #msgValue} object graph. You can show this graph by calling
     * the {@link com.credibledoc.iso8583packer.dump.DumpService#dumpMsgField(MsgField)} method.
     */
    protected MsgField msgField;

    protected Navigator navigator;
    
    protected Visualizer visualizer;

    /**
     * Please do not create instances of this builder. It uses for internal purposes only,
     * see the {@link #newInstance(MsgField)} method.
     */
    protected ValueHolder() {
        // empty
    }

    /**
     * Create a new instance of {@link MsgValue} from the {@link MsgField} definition for filling it with data.
     * <p>
     * Example of usage:
     * <pre>
     *     ValueHolder valueHolder = ValueHolder.newInstance(isoMsgField);
     * </pre>
     * 
     * How to fill the data to the {@link FieldBuilder}? See the following example:
     * <pre>
     *     String mtiValue = "0200";
     *     valueHolder.jumpToChild(MTI_NAME).setValue(mtiValue);
     * </pre>
     *
     * @param definition existing definition created with {@link FieldBuilder}.
     * @return A new instance of this {@link ValueHolder} with {@link #msgValue} and {@link #msgField} in its context.
     */
    public static ValueHolder newInstance(MsgField definition) {
        ValueHolder valueHolder = new ValueHolder();
        valueHolder.createDefaultServices();
        return valueHolder.newValueHolder(definition);
    }

    /**
     * Create instances of services used in the builder. The method may be overridden if needed.
     */
    protected void createDefaultServices() {
        navigator = new NavigatorService();
        visualizer = DumpService.getInstance();
        visualizer.setNavigator(navigator);
        navigator.setVisualizer(visualizer);
    }

    /**
     * Set the instances of {@link MsgValue} and {@link MsgField} from the definition tho the {@link ValueHolder} instance.
     *
     * @param definition existing definition created with {@link FieldBuilder}.
     * @return A new instance of this {@link ValueHolder} with {@link #msgValue} and {@link #msgField} in its context.
     */
    protected ValueHolder newValueHolder(MsgField definition) {
        this.msgField = definition;
        this.msgValue = navigator.newFromNameAndTagNum(definition);
        return this;
    }

    /**
     * Create a new instance of {@link ValueHolder}.
     *
     * @param msgValue      will be set to the {@link ValueHolder#msgValue}.
     * @param msgField will be set to the {@link ValueHolder#msgField}.
     * @return The new created {@link ValueHolder} with the {@link #msgValue} and {@link #msgField} in its context.
     */
    public static ValueHolder newInstance(MsgValue msgValue, MsgField msgField) {
        ValueHolder valueHolder = new ValueHolder();
        valueHolder.createDefaultServices();
        return valueHolder.newValueHolder(msgValue, msgField);
    }

    /**
     * Set the new instance of {@link ValueHolder}.
     *
     * @param msgValue      will be set to the {@link ValueHolder#msgValue}.
     * @param msgField will be set to the {@link ValueHolder#msgField}.
     * @return The new created {@link ValueHolder} with the {@link #msgValue} and {@link #msgField} in its context.
     */
    protected ValueHolder newValueHolder(MsgValue msgValue, MsgField msgField) {
        try {
            MsgPair msgPair = new MsgPair(msgField, msgValue);
            navigator.validateSameNamesAndTagNum(msgPair);
            this.msgValue = msgValue;
            this.msgField = msgField;
            return this;
        } catch (Exception e) {
            MsgField rootMsgField = navigator.findRoot(msgField);
            throw new PackerRuntimeException("Exception in method get(msgValue, msgField): " + e.getMessage() + "\n" +
                    "\nThe root MsgField:\n" + visualizer.dumpMsgField(rootMsgField) +
                    "\nMsgValue:\n" + visualizer.dumpMsgValue(msgField, msgValue, true) + "\n", e);
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
        ValueHolder valueHolder = newInstance(msgField);
        return valueHolder.unpackMsgField(bytes, offset);
    }

    /**
     * Unpack the bytes started from offset to the {@link #msgField} in the context.
     * @param bytes the source bytes.
     * @param offset the index where the field starts in the bytes.
     * @return The unpacked {@link MsgValue}.
     */
    protected MsgValue unpackMsgField(byte[] bytes, int offset) {
        MsgValue newMsgValue = navigator.newFromNameAndTagNum(msgField);
        try {
            Offset offsetObject = new Offset();
            offsetObject.setValue(offset);
            MsgPair msgPair = new MsgPair(msgField, newMsgValue);
            unpackFieldRecursively(bytes, offsetObject, msgPair);
            return newMsgValue;
        } catch (Exception e) {
            String dump = PARTIAL_DUMP + visualizer.dumpMsgValue(msgField, newMsgValue, true) +
                ROOT_MSG_FIELD + visualizer.dumpMsgField(navigator.findRoot(msgField));
            throw new PackerRuntimeException("Cannot unpack field: " + navigator.generatePath(newMsgValue) + dump, e);
        }
    }

    /**
     * @param bytes the source bytes.
     * @param offset the index where the field starts in the bytes.
     * @param msgPair the definition of the {@link MsgField} structure and the field values.
     * @throws Exception in case of packing problems
     */
    protected void unpackFieldRecursively(byte[] bytes, Offset offset, MsgPair msgPair) throws Exception {
        navigator.validateSameNamesAndTagNum(msgPair);
        Integer rawDataLength;
        MsgFieldType msgFieldType = msgPair.getMsgField().getType();
        if (MsgFieldType.MSG == msgFieldType) {
            rawDataLength = bytes.length - offset.getValue();
        } else if (msgFieldType == MsgFieldType.BIT_SET) {
            rawDataLength = unpackBitSet(bytes, offset, msgPair);
        } else if (MsgFieldType.VAL == msgFieldType) {
            rawDataLength = unpackFixedLengthType(bytes, offset, msgPair);
        } else {
            rawDataLength = unpackOtherTypes(bytes, offset, msgPair);
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

    protected Integer unpackOtherTypes(byte[] bytes, Offset offset, MsgPair msgPair) {
        Integer rawDataLength = null;
        Integer tagLength = getChildTagLengthFromParent(msgPair.getMsgField());
        boolean lengthFirst = MsgFieldType.getLengthFirstTypes().contains(msgPair.getMsgField().getType());
        Integer tagNum = null;

        if (lengthFirst && MsgFieldType.isLengthType(msgPair.getMsgField())) {
            rawDataLength = unpackLength(bytes, offset, msgPair) - tagLength;
        }

        if (MsgFieldType.getTaggedTypes().contains(msgPair.getMsgField().getType())) {
            tagNum = unpackTagNum(bytes, offset, msgPair);
        }

        boolean tagNumUnpackedButIsDifferent = tagNum != null && (
                msgPair.getMsgField().getTagNum() == null || !tagNum.equals(msgPair.getMsgField().getTagNum()));

        if (tagNumUnpackedButIsDifferent) {
            replaceWithSibling(msgPair, tagNum);
        }

        if (MsgFieldType.getTaggedTypes().contains(msgPair.getMsgField().getType())) {
            unpackTagBytes(bytes, offset, msgPair, tagLength, tagNum);
        }

        if (!lengthFirst && MsgFieldType.isLengthType(msgPair.getMsgField())) {
            rawDataLength = unpackLength(bytes, offset, msgPair);
        }
        
        if (msgPair.getMsgField().getLen() != null) {
            rawDataLength = msgPair.getMsgField().getLen();
        }

        if (rawDataLength == null) {
            throw new PackerRuntimeException("Cannot find rawDataLength of the msgField with path '" + navigator.getPathRecursively(msgPair.getMsgField()) + "'");
        }

        // unpack field body
        unpackBodyBytes(bytes, offset, msgPair, rawDataLength);
        return rawDataLength;
    }

    protected Integer unpackFixedLengthType(byte[] bytes, Offset offset, MsgPair msgPair) {
        Integer rawDataLength;
        MsgPair nextEmptyMsgPair = getNextEmptyMsgPairForFixedLengthType(msgPair);
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

    protected LengthPacker getLengthPackerFromParentOrSelfOrThrowException(MsgField msgField) {
        boolean parentContainsChildrenLengthPacker = msgField.getParent() != null &&
                msgField.getParent().getChildrenLengthPacker() != null;
        if (parentContainsChildrenLengthPacker) {
            return msgField.getParent().getChildrenLengthPacker();
        }
        
        if (msgField.getHeaderField().getLengthPacker() == null) {
            throw new PackerRuntimeException("Property lengthPacker is not defined. Please define it by calling " +
                    "the .defineHeaderLengthPacker() method. " +
                    "Current MsgField: " + navigator.generatePath(msgField));
        }
        
        return msgField.getHeaderField().getLengthPacker();
    }

    protected void unpackBodyBytes(byte[] bytes, Offset offset, MsgPair msgPair, Integer rawDataLength) {
        try {
            byte[] rawData = new byte[rawDataLength];
            System.arraycopy(bytes, offset.getValue(), rawData, 0, rawData.length);
            msgPair.getMsgValue().setBodyBytes(rawData);
        } catch (Exception e) {
            String path = navigator.getPathRecursively(msgPair.getMsgField());
            throw new PackerRuntimeException("Current MsgField: '" + path + "', offset: '" + offset.getValue() +
                "', rawDataLength: '" + rawDataLength + "'", e);
        }
    }

    protected MsgPair getNextEmptyMsgPairForFixedLengthType(MsgPair paramMsgPair) {
        MsgField paramMsgField = paramMsgPair.getMsgField();
        MsgField paramParentMsgField = paramMsgField.getParent();
        if (paramParentMsgField == null) {
            return paramMsgPair;
        }
        MsgValue paramMsgValue = paramMsgPair.getMsgValue();
        // Remove last MsgValue because this item is empty and not accepted for MsgFieldType==VAL.
        paramMsgValue.getParent().getChildren().remove(paramMsgValue.getParent().getChildren().size() - 1);
        for (MsgField siblingMsgField : paramParentMsgField.getChildren()) {
            MsgValue siblingMsgValue = navigator.findByName(paramMsgValue.getParent().getChildren(),
                    siblingMsgField.getName());
            if (siblingMsgValue == null) {
                if (!MsgFieldType.isFixedLengthType(siblingMsgField)) {
                    throw new PackerRuntimeException("Expected one of the '" + MsgFieldType.getFixedLengthTypes() +
                        "' types but found '" + siblingMsgField.getType() +
                            "'. SiblingMsgField '" + navigator.getPathRecursively(siblingMsgField) + "'.");
                }
                siblingMsgValue = navigator.newFromNameAndTagNum(siblingMsgField);
                return new MsgPair(siblingMsgField, siblingMsgValue);
            }
            if (siblingMsgValue.getBodyBytes() == null) {
                return new MsgPair(siblingMsgField, siblingMsgValue);
            }
        }
        
        throw new PackerRuntimeException("Cannot find sibling with empty bodyBytes for MsgField '" +
            navigator.getPathRecursively(paramMsgField) +
                "'. It means cannot unpack the remaining bytes to MsgValue.");
    }

    protected void unpackChildren(byte[] bytes, Offset offset, MsgPair msgPair, Integer rawDataLength) throws Exception {
        int offsetWithChildren = offset.getValue() + rawDataLength;
        if (rawDataLength > 0) {
            msgPair.getMsgValue().setChildren(new ArrayList<>());
        }
        int childNum = 0;
        while (offset.getValue() < offsetWithChildren) {
            List<MsgField> children = msgPair.getMsgField().getChildren();
            MsgField msgFieldFirstChild = children.get(childNum);
            if (children.size() > childNum + 1) {
                childNum++;
            }
            MsgValue msgValueChild = navigator.newFromNameAndTagNum(msgFieldFirstChild);
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

    protected void unpackTagBytes(byte[] bytes, Offset offset, MsgPair msgPair, Integer fieldTagLength,
                                              Integer tagNum) {
        byte[] tagBytes = new byte[fieldTagLength];
        System.arraycopy(bytes, offset.getValue(), tagBytes, 0, tagBytes.length);
        offset.add(fieldTagLength);
        HeaderValue headerValue = msgPair.getMsgValue().getHeaderValue();
        headerValue.setTagBytes(tagBytes);
        msgPair.getMsgValue().setTagNum(tagNum);
    }

    protected Integer unpackTagNum(byte[] bytes, Offset offset, MsgPair msgPair) {
        Integer tagNum;
        TagPacker tagPacker = navigator.getTagPackerFromParent(msgPair.getMsgField());
        if (tagPacker == null) {
            tagNum = msgPair.getMsgField().getTagNum();
        } else {
            tagNum = tagPacker.unpack(bytes, offset.getValue());
        }
        return tagNum;
    }

    protected int unpackBitSet(byte[] bytes, Offset offset, MsgPair msgPair) throws Exception {
        List<Integer> tagNums = getTagNumsFromBitSet(bytes, offset, msgPair);
        for (int nextTagNum : tagNums) {
            MsgField msgFieldChild = findChildByTagNum(msgPair.getMsgField(), nextTagNum);
            MsgValue msgValueChild = navigator.newFromNameAndTagNum(msgFieldChild);
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

    protected int unpackLength(byte[] bytes, Offset offset, MsgPair msgPair) {
        LengthPacker lengthPacker = getLengthPackerFromParentOrSelfOrThrowException(msgPair.getMsgField());
        int lenLength = lengthPacker.calculateLenLength(bytes, offset.getValue());
        byte[] lengthBytes = new byte[lenLength];
        System.arraycopy(bytes, offset.getValue(), lengthBytes, 0, lengthBytes.length);
        msgPair.getMsgValue().getHeaderValue().setLengthBytes(lengthBytes);
        int rawDataLength = lengthPacker.unpack(bytes, offset.getValue());
        offset.add(lenLength);
        return rawDataLength;
    }

    protected void setValueToLeaf(byte[] bytes, Offset offset, MsgPair msgPair, int bodyBytesLength) {
        BodyPacker bodyPacker = msgPair.getMsgField().getBodyPacker();
        if (bodyPacker == null) {
            throw new PackerRuntimeException("BodyPacker not found. Please call defineBodyPacker(...) method " +
                    "of the FieldBuilder, for example " +
                    "MsgField subfield35 = FieldBuilder.builder().defineBodyPacker(...).\n" +
                    "MsgValue: " + navigator.getPathRecursively(msgPair.getMsgValue()));
        }
        Object bodyValue = bodyPacker.unpack(bytes, offset.getValue(), bodyBytesLength);
        msgPair.getMsgValue().setBodyValue(bodyValue);
    }

    protected void replaceWithSibling(MsgPair msgPair, Integer tagNum) {
        MsgPair result = new MsgPair();
        MsgField msgFieldSibling = findSiblingByTagNum(tagNum, msgPair.getMsgField());
        if (msgFieldSibling == null) {
            throwSiblingNotFound(msgPair, tagNum);
        }
        MsgValue oldMsgValue = msgPair.getMsgValue();
        MsgValue parentMsgValue = oldMsgValue.getParent();
        result.setMsgField(msgFieldSibling);
        assert msgFieldSibling != null;
        MsgValue newMsgValue = navigator.newFromNameAndTagNum(msgFieldSibling);
        if (parentMsgValue != null) {
            parentMsgValue.getChildren().remove(oldMsgValue);
            parentMsgValue.getChildren().add(newMsgValue);
            newMsgValue.setParent(parentMsgValue);
            result.setMsgValue(newMsgValue);
        }
        if (oldMsgValue.getHeaderValue() != null) {
            newMsgValue.setHeaderValue(oldMsgValue.getHeaderValue());
        }
        msgPair.setMsgField(result.getMsgField());
        msgPair.setMsgValue(result.getMsgValue());
    }

    protected List<Integer> getTagNumsFromBitSet(byte[] bytes, Offset offset, MsgPair msgPair) {
        // this is IsoMsg, so tagNums are in the header BitSet
        HeaderField headerField = msgPair.getMsgField().getHeaderField();
        HeaderValue headerValue = msgPair.getMsgValue().getHeaderValue();

        // unpack
        BitmapPacker bitmapPacker = headerField.getBitMapPacker();
        if (bitmapPacker == null) {
            throw new PackerRuntimeException("Please call the defineHeaderBitmapPacker(...) " +
                    "method for this field " + navigator.getPathRecursively(msgPair.getMsgValue()));
                    
        }
        Integer len = msgPair.getMsgField().getLen();
        int consumed = bitmapPacker.unpack(headerValue, bytes, offset.getValue(), len);
        offset.add(consumed);

        return getTagNumsAndValidateBitSet(msgPair);
    }

    protected MsgField findChildByTagNum(MsgField msgField, int nextTagNum) {
        for (MsgField child : msgField.getChildren()) {
            if (nextTagNum == child.getTagNum()) {
                return child;
            }
        }
        throw new PackerRuntimeException("Cannot find child with tagNum '" + nextTagNum +
                "' in the msgField " + navigator.getPathRecursively(msgField));
    }

    protected List<Integer> getTagNumsAndValidateBitSet(MsgPair msgPair) {
        BitSet unpackedBitSet = msgPair.getMsgValue().getHeaderValue().getBitSet();
        List<Integer> tagNums = new ArrayList<>();
        int maxTagNum = getMaxTagNum(msgPair.getMsgField().getChildren());
        for (int nextTagNum = 0; nextTagNum <= maxTagNum; nextTagNum++) {
            MsgField childMsgField = navigator.findByTagNum(msgPair.getMsgField().getChildren(), nextTagNum);
            if (unpackedBitSet.get(nextTagNum) && childMsgField == null) {
                String path = navigator.getPathRecursively(msgPair.getMsgValue());
                throw new PackerRuntimeException("Unpacked bitSet contains tagNum '" + nextTagNum +
                        "', but bitSetDefinition doesn't contain it. " +
                        "Please define the tagNum in the field '" + path + "' header bitSet");
            }
            if (unpackedBitSet.get(nextTagNum)) {
                tagNums.add(nextTagNum);
            }
        }
        return tagNums;
    }

    protected void throwSiblingNotFound(MsgPair paramMsgPair, Integer tagNum) {
        MsgField paramMsgField = paramMsgPair.getMsgField();
        MsgValue paramMsgValue = paramMsgPair.getMsgValue();
        String previousFieldCause = "";
        MsgField parentMsgField = paramMsgField.getParent();
        if (parentMsgField != null && parentMsgField.getHeaderField() != null) {
            previousFieldCause = " \nNext cause is incorrect implementation of the " + parentMsgField.getHeaderField().getLengthPacker().getClass().getSimpleName() +
                    ".calculateLenLength(bytes, offset) method of the previous '" + navigator.getPathRecursively(parentMsgField) +
                    "' field.";
        }
        String tagPackerClass = "null";
        if (paramMsgField.getParent() != null && paramMsgField.getParent().getChildrenTagPacker() != null) {
            tagPackerClass = paramMsgField.getParent().getChildrenTagPacker().getClass().getSimpleName();
        }
        throw new PackerRuntimeException("Cannot find a sibling with tagNum '" + tagNum +
                "' for the '" + navigator.getPathRecursively(paramMsgValue) +
                "' Field. Its parent '" + navigator.getPathRecursively(parentMsgField) +
                "' has no child with such tagNum. \nThere are few possible causes of this error. " +
                "\nFirst cause is incorrect implementation of " + tagPackerClass +
                ".unpack() method used for unpacking tagNum from bytes. " +
                "\nSecond cause is undefined child with tagNum '" + tagNum +
                "' and with parent '" + navigator.getPathRecursively(parentMsgField) +
                "' in the MsgField definition." + previousFieldCause + " \nNext cause can be the order of tagNum and " +
                "length subfields where some MsgFields have the first tagNum and the second length but other " +
                "MsgFields wise versa, for example F0F0F3 F9F3 F0, where F0F0F3 is the length 003, F9F3 is " +
                "the tagNum 93 and F0 is the body." +
                " \nNext cause is wrong place of actual context inside the rootMsgField, actual place is " +
            navigator.getPathRecursively(paramMsgField));
    }

    protected int getMaxTagNum(List<MsgField> children) {
        int result = 0;
        for (MsgField nextMsgField : children) {
            if (nextMsgField.getTagNum() > result) {
                result = nextMsgField.getTagNum();
            }
        }
        return result;
    }

    protected MsgField findSiblingByTagNum(int tagNum, MsgField msgField) {
        MsgField parent = msgField.getParent();
        for (MsgField child : parent.getChildren()) {
            if (child.getTagNum() == tagNum) {
                return child;
            }
        }
        return null;
    }

    /**
     * Create a new instance of {@link ValueHolder} and set msgPair values to its
     * {@link #msgField} and {@link #msgValue} context objects.
     * @param msgPair contains {@link MsgField} and {@link MsgValue}.
     * @return The created instance.
     */
    public static ValueHolder newInstance(MsgPair msgPair) {
        ValueHolder valueHolder = new ValueHolder();
        valueHolder.createDefaultServices();
        valueHolder.msgField = msgPair.getMsgField();
        valueHolder.msgValue = msgPair.getMsgValue();
        return valueHolder;
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
     * @return The current instance of {@link ValueHolder}.
     */
    public ValueHolder jumpToChild(String childName) {
        try {
            MsgField msgFieldChild = navigator.getChildOrThrowException(childName, msgField);
            MsgValue msgValueChild = navigator.findByName(msgValue.getChildren(), childName);
            if (msgValueChild == null) {
                msgValueChild = navigator.newFromNameAndTagNum(msgFieldChild);
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
            MsgField rootMsgField = navigator.findRoot(msgField);
            throw new PackerRuntimeException("Exception message: " + e.getMessage() + "\nCannot find a child." +
                ROOT_MSG_FIELD + visualizer.dumpMsgField(rootMsgField), e);
        }
    }

    /**
     * Change actual {@link #msgField} and {@link #msgValue} to their parents.
     * @return The current instance of {@link ValueHolder}.
     */
    public ValueHolder jumpToParent() {
        if (msgField.getParent() == null) {
            throw new PackerRuntimeException("MsgField '" + navigator.getPathRecursively(msgField) + "' has no parent.");
        }
        if (msgValue.getParent() == null) {
            throw new PackerRuntimeException("MsgValue '" + navigator.getPathRecursively(msgValue) + "' has no parent.");
        }
        msgField = msgField.getParent();
        msgValue = msgValue.getParent();
        return this;
    }

    /**
     * Set the {@link MsgValue#setBodyValue(Object)} from the argument to the {@link #msgValue}.
     * <p>
     * Example of usage:
     * <pre>
     *     ValueHolder.newInstance(msgPair)
     *             .jumpToChild("child_name")
     *             .setValue("some_value");
     * </pre>
     *
     * <p>
     * Set the {@link MsgValue#setBodyBytes(byte[])} from the bodyValue to the field, see the
     * {@link #setBytes(Object)} method description.
     * <p>
     * If the field contains {@link MsgField#getHeaderField()} then properties of the header, for example
     * {@link HeaderValue#setTagBytes(byte[])} or {@link HeaderValue#setLengthBytes(byte[])} are set.
     * Length and tag are not mandatory.
     * See the {@link #setHeader(byte[], HeaderValue, HeaderField)}  method.
     *
     * @param bodyValue can be 'null' for unset.
     * @return The current {@link ValueHolder} with the same {@link #msgValue} and {@link #msgField} in its context.
     */
    public ValueHolder setValue(Object bodyValue) {
        if (msgField.getChildren() != null && !msgField.getChildren().isEmpty()) {
            throw new PackerRuntimeException("Cannot set bodyValue to fields with children. Values can only contain " +
                    "leaf fields. Field: " + navigator.getPathRecursively(msgField) + ", bodyValue: " + bodyValue);
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
            MsgValue rootMsgValue = navigator.findRoot(msgValue);
            MsgField rootMsgField = navigator.findRoot(msgField);
            MsgField appropriateMsgField = navigator.findByNameAndTagNumOrThrowException(rootMsgField, msgValue);
            throw new PackerRuntimeException("Exception message: " + e.getMessage() + "\nCannot set bodyValue '" + bodyValue +
                    "' to field '" + navigator.getPathRecursively(msgField) + "'" +
                    "\nRoot MsgValue:\n" + visualizer.dumpMsgValue(appropriateMsgField, rootMsgValue, true) +
                    "\nThe MsgField:\n" + visualizer.dumpMsgField(msgField) +
                ROOT_MSG_FIELD + visualizer.dumpMsgField(rootMsgField), e);
        }
    }

    protected byte[] setBytes(Object bodyValue) {
        BodyPacker bodyPacker = msgField.getBodyPacker();

        if (bodyPacker == null) {
            throw new PackerRuntimeException("BodyPacker not found. Please call setBodyPacker(...) " +
                    "method, for example " +
                    "MsgField subfield35 = FieldBuilder.builder().BodyPacker(HexBodyPacker.getInstance())...\n" +
                    "MsgField: " + navigator.getPathRecursively(msgField));
        }
        
        byte[] bodyBytes;
        int bodyLength = bodyPacker.getPackedLength(bodyValue);

        Integer exactlyLength = msgField.getExactlyLength();
        if (exactlyLength != null && bodyLength != exactlyLength) {
            throw new PackerRuntimeException(THE_MSG_FIELD + navigator.getPathRecursively(msgField) +
                    "' contains the 'exactlyLength' definition with value '" + exactlyLength +
                    "', but the bodyValue length '" + bodyLength + "' is not the same.");
        }

        Integer maxLen = msgField.getMaxLen();
        if (maxLen != null && maxLen < bodyLength) {
            throw new PackerRuntimeException(THE_MSG_FIELD + navigator.getPathRecursively(msgField) +
                    "' contains the 'maxLen' definition with value '" + maxLen +
                    "', but its bodyValue length '" + bodyLength + "' is greater.");
        }
        
        Integer len = msgField.getLen();
        if (len != null && bodyLength != len) {
            throw new PackerRuntimeException(THE_MSG_FIELD + navigator.getPathRecursively(msgField) +
                "' contains the 'len' definition with value '" + len +
                "', but its bodyValue length '" + bodyLength + "' is different.");
        }
        
        bodyBytes = new byte[bodyLength];
        bodyPacker.pack(bodyValue, bodyBytes, 0);
        msgValue.setBodyBytes(bodyBytes);
        return bodyBytes;
    }

    /**
     * Set valueBytes to the headerValue
     * @param valueBytes source data
     * @param headerValue target object
     * @param headerField the header definition
     */
    protected void setHeader(byte[] valueBytes, HeaderValue headerValue, HeaderField headerField) {
        MsgField msgFieldParent = msgField.getParent();
        if (msgFieldParent == null && MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
            throw new PackerRuntimeException("This MsgField has no parent. Please use FieldBuilder.from(msgField) and " +
                    "call the setParent(..) method before setValue(..) method " +
                    "because parent.getChildTagPacker(..) is used for header creation");
        }
        LengthPacker lengthPacker;

        Integer fieldNum = msgValue.getTagNum();
        if (msgField.getType() == MsgFieldType.LEN_TAG_VAL) {
            // field parent contains lengthPacker, hence length precedes tagNum
            assert msgFieldParent != null;
            lengthPacker = msgFieldParent.getChildrenLengthPacker();
            packTagBytesToHeader(headerValue, fieldNum);
            int tagAndValueLength = valueBytes.length + headerValue.getTagBytes().length;
            byte[] lengthBytes = lengthPacker.pack(tagAndValueLength);
            headerValue.setLengthBytes(lengthBytes);
        } else {
            // field itself contains lengthPacker
            lengthPacker = headerField.getLengthPacker();
            if (MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
                packTagBytesToHeader(headerValue, fieldNum);
            }

            if (lengthPacker != null) {
                byte[] lengthBytes = lengthPacker.pack(valueBytes.length);
                headerValue.setLengthBytes(lengthBytes);
            }
        }
    }

    protected void packTagBytesToHeader(HeaderValue headerValue, Integer fieldNum) {
        TagPacker tagPacker = navigator.getTagPackerFromParent(msgField);
        if (tagPacker == null) {
            headerValue.setTagBytes(new byte[0]);
            return;
        }
        byte[] tagBytes = tagPacker.pack(fieldNum);
        headerValue.setTagBytes(tagBytes);
    }

    protected Integer getChildTagLengthFromParent(MsgField msgField) {
        if (MsgFieldType.isNotTaggedType(msgField)) {
            return 0;
        }
        if (msgField.getParent() != null) {
            return msgField.getParent().getChildrenTagPacker().getPackedLength();
        }
        throw new PackerRuntimeException("This field '" + navigator.getPathRecursively(msgField) +
                "' has no parent. The parent is mandatory for obtaining of the ChildrenTagPacker property. " +
                "Please create a new Field and set it as a parent.");
    }

    /**
     * Change the current {@link #msgValue} and {@link #msgField} to the new location. If the field sibling is not found,
     * create a new one.
     *
     * @param siblingName the sibling name of the current {@link #msgValue} and {@link #msgField}.
     * @return The current builder instance with the new {@link #msgValue} and {@link #msgField}.
     */
    public ValueHolder jumpToSibling(String siblingName) {
        try {
            MsgField msgFieldSibling = navigator.getSiblingOrThrowException(siblingName, msgField);
            MsgValue parentMsgValue = msgValue.getParent();
            MsgValue siblingMsgValue = navigator.findByName(parentMsgValue.getChildren(), siblingName);
            if (siblingMsgValue == null) {
                siblingMsgValue = navigator.newFromNameAndTagNum(msgFieldSibling);
                siblingMsgValue.setParent(msgValue.getParent());
                msgValue.getParent().getChildren().add(siblingMsgValue);
                sortFieldChildren(msgValue.getParent(), msgField.getParent());
            }
            msgValue = siblingMsgValue;
            msgField = msgFieldSibling;
            return this;
        } catch (Exception e) {
            MsgValue rootMsgValue = navigator.findRoot(msgValue);
            MsgField rootMsgField = navigator.findRoot(msgField);
            throw new PackerRuntimeException("Exception: " + e.getMessage() + "\nCannot jumpToSibling '" + siblingName +
                    "' of the message definition." +
                    "\nMsgValue:\n" + visualizer.dumpMsgValue(rootMsgField, rootMsgValue, true) +
                    "\nMsgField:\n" + visualizer.dumpMsgField(rootMsgField) + "\n", e);
        }
    }

    /**
     * @param msgValue to be sorted
     * @param msgField how to sort
     */
    protected void sortFieldChildren(MsgValue msgValue, MsgField msgField) {
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

    protected MsgValue remove(String name, List<MsgValue> msgValueChildren) {
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
    public ValueHolder setChildren(List<MsgValue> subfields) {
        for (MsgValue child : subfields) {
            child.setParent(msgValue);
        }
        msgValue.setChildren(subfields);
        return this;
    }

    /**
     * Navigate to the root {@link #msgValue} and {@link #msgField}.
     * @return The current instance of {@link ValueHolder} with root of {@link #msgValue} and corresponding node of
     * {@link #msgField} in its context.
     */
    public ValueHolder jumpToRoot() {
        msgValue = navigator.findRoot(msgValue);
        msgField = navigator.findByNameAndTagNumOrThrowException(msgField, msgValue);
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
            MsgValue rootMsgValue = navigator.findRoot(msgValue);
            MsgField rootMsgField = navigator.findRoot(msgField);
            throw new PackerRuntimeException("Exception: " + e.getMessage() + "\n" +
                    "Cannot pack field '" + navigator.getPathRecursively(msgValue) + "'" +
                PARTIAL_DUMP + visualizer.dumpMsgValue(rootMsgField, rootMsgValue, true) +
                    "\nMsgField:\n" + visualizer.dumpMsgField(msgField) + "\n", e);
        }
    }

    protected ByteArrayOutputStream packRecursively(MsgValue msgValue, MsgField msgField) throws IOException {
        int maxLen = msgField.getMaxLen() != null ? msgField.getMaxLen() : INITIAL_SIZE_100_BYTES;
        ByteArrayOutputStream messageBytes = new ByteArrayOutputStream(maxLen);
        if (msgValue.getChildren() != null) {
            ByteArrayOutputStream childrenBytes = new ByteArrayOutputStream(maxLen);
            if (MsgFieldType.BIT_SET == msgField.getType()) {
                packBitmap(msgValue, msgField, messageBytes);
            }
            for (MsgValue nextMsgValue : msgValue.getChildren()) {
                MsgField msgFieldChild = navigator.findByName(msgField.getChildren(), nextMsgValue.getName());
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

    protected void packBitmap(MsgValue msgValue, MsgField msgField, ByteArrayOutputStream messageBytes) throws IOException {
        BitmapPacker bitmapPacker = msgField.getHeaderField().getBitMapPacker();
        if (bitmapPacker == null) {
            throw new PackerRuntimeException("The value of '" + BitmapPacker.class.getSimpleName() +
                "' type is mandatory for '" + MsgFieldType.class.getSimpleName() + 
                "' '" + MsgFieldType.BIT_SET + "' type. " +
                "Please call the defineBitmapPacker(...) method.");
        }
        BitSet bitSet = new BitSet();
        for (MsgField nextMsgField : msgField.getChildren()) {
            if (navigator.findByTagNum(msgValue.getChildren(), nextMsgField.getTagNum()) != null) {
                bitSet.set(nextMsgField.getTagNum());
            }
        }
        msgValue.getHeaderValue().setBitSet(bitSet);
        Integer len = msgField.getLen();
        if (len == null) {
            throw new PackerRuntimeException("Mandatory MsgField.len property not found. It is mandatory for '" + MsgFieldType.BIT_SET + "' type. " +
                "Please call the defineLen(...) method.");
        }
        byte[] bytes = bitmapPacker.pack(bitSet, len);
        msgValue.setBodyBytes(bytes);
        messageBytes.write(bytes);
    }

    protected void packBodyBytesAndLengthAndTagNum(MsgValue msgValue, MsgField msgField, ByteArrayOutputStream messageBytes, byte[] bytes) throws IOException {
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

    protected void writeTagBytesIfAllowed(MsgValue msgValue, MsgField msgField, ByteArrayOutputStream messageBytes) throws IOException {
        if (MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
            messageBytes.write(msgValue.getHeaderValue().getTagBytes());
        }
    }

    protected void writeLengthBytesIfAllowed(MsgValue msgValue, MsgField msgField,
                                                  ByteArrayOutputStream messageBytes) throws IOException {
        if (MsgFieldType.getLengthTypes().contains(msgField.getType())) {
            messageBytes.write(msgValue.getHeaderValue().getLengthBytes());
        }
    }

    protected ByteArrayOutputStream packHeaderAndValue(MsgValue msgValue, MsgField msgField) throws IOException {
        if (msgValue.getBodyBytes() == null) {
            throw new PackerRuntimeException("Expected non-null MsgValue.bodyBytes but found 'null'. MsgValue path: '" +
                navigator.getPathRecursively(msgValue) + "'. Please set the value or delete the MsgField. " +
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

    protected void writeTagBytes(ByteArrayOutputStream result, HeaderValue headerField) throws IOException {
        if (headerField.getTagBytes() != null) {
            result.write(headerField.getTagBytes());
        }
    }

    protected void writeLengthBytesIfExist(ByteArrayOutputStream result, HeaderValue headerField) throws IOException {
        if (headerField.getLengthBytes() != null) {
            result.write(headerField.getLengthBytes());
        }
    }

    protected void packLength(MsgValue msgValue, LengthPacker lengthPacker, byte[] bytes, boolean lengthPrecedesTagNum) {
        int bytesLength;
        if (lengthPrecedesTagNum) {
            bytesLength = bytes.length + msgValue.getHeaderValue().getTagBytes().length;
        } else {
            bytesLength = bytes.length;
        }
        byte[] lengthBytes = lengthPacker.pack(bytesLength);
        msgValue.getHeaderValue().setLengthBytes(lengthBytes);
    }

    protected void setTagBytes(MsgValue msgValue, MsgField msgField) {
        int tagNum = msgValue.getTagNum();
        TagPacker tagPacker = navigator.getTagPackerFromParent(msgField);
        assert tagPacker != null;
        byte[] tagBytes = tagPacker.pack(tagNum);
        msgValue.getHeaderValue().setTagBytes(tagBytes);
    }

    /**
     * Create a copy from the current {@link #msgValue} and set it to this {@link #msgValue} context.
     * This sibling will have the same {@link MsgField#getName()} and {@link MsgField#getTagNum()} as its sibling.
     *
     * @return The current actual {@link ValueHolder}.
     */
    public ValueHolder cloneSibling() {
        MsgValue clone = navigator.newFromNameAndTagNum(msgField);
        clone.setParent(msgValue.getParent());
        msgValue.getParent().getChildren().add(clone);
        msgValue = clone;
        return this;
    }

    /**
     * Create the new instance and set {@link #msgValue} and {@link #msgField} from the current instance.
     *
     * @return Created instance of {@link ValueHolder}.
     */
    public ValueHolder copyValueHolder() {
        ValueHolder clone = new ValueHolder();
        clone.createDefaultServices();
        clone.msgValue = msgValue;
        clone.msgField = msgField;
        return clone;
    }

    /**
     * Call the {@link #unpack(byte[], int, MsgField)} method with offset 0 and with current {@link #msgField} from the
     * {@link ValueHolder} context.
     *
     * @param bytes will be set as first argument
     * @return The {@link MsgValue} unpacked from the bytes.
     */
    public MsgValue unpack(byte[] bytes) {
        return unpack(bytes, 0, msgField);
    }

    /**
     * @return Existing {@link #msgField} and {@link #msgValue} created previously by the {@link #newInstance(MsgField)} method.
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
                    MsgValue childMsgValue = navigator.findByName(msgValue.getChildren(),
                        childMsgField.getName());
                    if (childMsgValue != null) {
                        validateRecursively(childMsgField, childMsgValue);
                    }
                }
            }
        } catch (Exception e) {
            String dump = PARTIAL_DUMP + visualizer.dumpMsgValue(msgField, msgValue, true) +
                ROOT_MSG_FIELD + visualizer.dumpMsgField(navigator.findRoot(msgField));
            throw new PackerRuntimeException("Validation failed: " + dump, e);
        }
    }

    protected void validateRecursively(MsgField msgField, MsgValue msgValue) {
        if (msgField.getBodyPacker() instanceof BcdBodyPacker) {
            BcdService.validateIsStringBcdNumber(msgValue);
        }
    }

    /**
     * @param type expected type of returned value
     * @param <T> expected type of returned value
     * @return The {@link MsgValue#getBodyValue()} casted to T type
     */
    public <T> T getValue(Class<T> type) {
        return type.cast(msgValue.getBodyValue());
    }

    /**
     * @param navigator see the {@link #navigator} field description.
     */
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    /**
     * @param visualizer see the {@link #visualizer} field description.
     */
    public void setVisualizer(Visualizer visualizer) {
        this.visualizer = visualizer;
    }
}
