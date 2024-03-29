package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.bcd.BcdService;
import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
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
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This builder helps to fill the existing {@link MsgField} definition with data. It contains
 * two state objects, the {@link #msgValue} and {@link #msgField}.
 * <p>
 * The {@link MsgField} definition can be created by {@link FieldBuilder}.
 *
 * @author Kyrylo Semenko
 */
public class ValueHolder {

    protected static final int INITIAL_SIZE_100_BYTES = 100;
    protected static final String PARTIAL_DUMP = "\nPartial dump:\n";
    protected static final String ROOT_MSG_FIELD = "\nRoot MsgField:\n";
    protected static final String THE_MSG_FIELD = "The MsgField '";
    protected static final String MSG_FIELD = "\nMsgField:\n";

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

    /**
     * The service helps to navigate through the {@link MsgField} and {@link MsgValue} object graphs.
     */
    protected Navigator navigator;

    /**
     * The service helps to visualize the {@link MsgField} and {@link MsgValue} object graphs.
     */
    protected Visualizer visualizer;

    /**
     * Please do not create instances of this builder. It uses for internal purposes only,
     * please use one of the newInstance() methods.
     */
    protected ValueHolder() {
        // empty
    }

    /**
     * Call the {@link #newInstance(MsgField, boolean)} method with the fromRoot=<b>false</b> value.
     *
     * @param definition see the {@link #newInstance(MsgField, boolean)} method description.
     * @return See the {@link #newInstance(MsgField, boolean)} method description.
     */
    public static ValueHolder newInstance(MsgField definition) {
        return newInstance(definition, false);
    }

    /**
     * Create a new instance of {@link MsgValue} from the {@link MsgField} definition for filling it with data.
     * <p>
     * Example of usage:
     * <pre>
     *     ValueHolder valueHolder = ValueHolder.newInstance(isoMsgField);
     * </pre>
     * <p>
     * How to fill the data to the {@link FieldBuilder}? See the following example:
     * <pre>
     *     String mtiValue = "0200";
     *     valueHolder.jumpToChild(MTI_NAME).setValue(mtiValue);
     * </pre>
     *
     * @param definition existing definition created with {@link FieldBuilder}.
     * @param fromRoot   if 'false', the current MsgField definition will be used as a current position in the 
     *                   MsgField graph.<p>
     *                   If 'true', the root definition in the MsgField graph will be used as a current position.
     * @return A new instance of the {@link ValueHolder} with {@link #msgValue} and {@link #msgField} in its context.
     */
    public static ValueHolder newInstance(MsgField definition, boolean fromRoot) {
        if (definition == null) {
            throw new PackerRuntimeException("MsgField definition cannot be 'null'.");
        }
        ValueHolder valueHolder = new ValueHolder();
        valueHolder.createDefaultServices();
        return valueHolder.setValueAndField(definition, fromRoot);
    }

    /**
     * Call the {@link #newInstance(FieldBuilder, boolean)} method with fromRoot=<b>false</b> value.
     *
     * @param fieldBuilder see the the {@link #newInstance(FieldBuilder, boolean)} method description.
     * @return See the {@link #newInstance(FieldBuilder, boolean)} method description.
     */
    public static ValueHolder newInstance(FieldBuilder fieldBuilder) {
        return newInstance(fieldBuilder.getCurrentField());
    }

    /**
     * Call the {@link #newInstance(MsgField, boolean)} method.
     *
     * @param fieldBuilder the current {@link FieldBuilder} with the {@link FieldBuilder#msgField} value.
     * @param fromRoot see the {@link #newInstance(MsgField, boolean)} description.
     * @return See the {@link #newInstance(MsgField, boolean)} description.
     */
    public static ValueHolder newInstance(FieldBuilder fieldBuilder, boolean fromRoot) {
        return newInstance(fieldBuilder.getCurrentField(), fromRoot);
    }

    /**
     * Create instances of services used in the builder. The method may be overridden if needed.
     */
    protected void createDefaultServices() {
        navigator = NavigatorService.getInstance();
        visualizer = DumpService.getInstance();
        visualizer.setNavigator(navigator);
        navigator.setVisualizer(visualizer);
    }

    /**
     * Create a new {@link MsgValue} instance.
     *
     * @param definition an existing definition.
     * @param fromRoot if 'true', the root {@link MsgField} will be used as a definition.
     * @return A new instance of the {@link ValueHolder} with {@link #msgValue} and the {@link #msgField} in its context.
     */
    protected ValueHolder setValueAndField(MsgField definition, boolean fromRoot) {
        if (fromRoot) {
            definition = navigator.findRoot(definition);
        }
        msgField = definition;
        msgValue = navigator.newFromNameAndTag(definition);
        msgValue.setRoot(msgValue); // root field references to itself
        if (msgValue.getParent() != null) {
            msgValue.getParent().getChildNamesMap().put(msgValue.getName(), msgValue);
        }
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
        if (msgValue == null) {
            throw new PackerRuntimeException("MsgValue cannot be 'null'.");
        }
        if (msgField == null) {
            throw new PackerRuntimeException("MsgField cannot be 'null'.");
        }
        ValueHolder valueHolder = new ValueHolder();
        valueHolder.createDefaultServices();
        return valueHolder.setValueAndField(msgValue, msgField);
    }

    /**
     * Set the arguments to the current instance of the {@link ValueHolder}.
     *
     * @param msgValue will be set to the {@link ValueHolder#msgValue}.
     * @param msgField will be set to the {@link ValueHolder#msgField}.
     * @return The current instance of {@link ValueHolder} with the {@link #msgValue} and {@link #msgField} in its context.
     */
    protected ValueHolder setValueAndField(MsgValue msgValue, MsgField msgField) {
        try {
            this.msgValue = msgValue;
            this.msgField = msgField;
            if (this.msgValue.getName() == null) {
                this.msgValue.setName(msgField.getName());
            }
            if (this.msgValue.getRoot() == null) {
                this.msgValue.setRoot(this.msgValue);
            }
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
        MsgValue newMsgValue = msgValue;
        try {
            Offset offsetObject = new Offset();
            offsetObject.setValue(offset);
            MsgPair msgPair = new MsgPair(msgField, newMsgValue);
            unpackFieldRecursively(bytes, offsetObject, msgPair);
            msgValue = newMsgValue;
            msgField = msgPair.getMsgField();
            if (newMsgValue.getParent() != null) {
                newMsgValue.getParent().getChildNamesMap().put(newMsgValue.getName(), newMsgValue);
            }
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
     */
    protected void unpackFieldRecursively(byte[] bytes, Offset offset, MsgPair msgPair) {
        navigator.validateSameNamesAndTags(msgPair);
        Integer rawDataLength;
        MsgFieldType msgFieldType = msgPair.getMsgField().getType();
        if (MsgFieldType.MSG == msgFieldType) {
            if (isValType(msgPair.getMsgField().getChildren())) {
                rawDataLength = sumChildrenLen(msgPair.getMsgField().getChildren());
            } else {
                rawDataLength = bytes.length - offset.getValue();
            }
        } else if (msgFieldType == MsgFieldType.BIT_SET) {
            rawDataLength = unpackBitSet(bytes, offset, msgPair);
        } else if (MsgFieldType.VAL == msgFieldType) {
            rawDataLength = unpackFixedLengthType(bytes, offset, msgPair);
        } else {
            rawDataLength = unpackOtherTypes(bytes, offset, msgPair);
        }

        MsgValue parent = msgPair.getMsgValue().getParent();
        if (parent != null) {
            parent.getChildNamesMap().put(msgPair.getMsgValue().getName(), msgPair.getMsgValue());
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

    protected int sumChildrenLen(List<MsgField> children) {
        int result = 0;
        for (MsgField child : children) {
            result = result + child.getLen();
        }
        return result;
    }

    protected boolean isValType(List<MsgField> children) {
        for (MsgField child : children) {
            if (child.getType() != MsgFieldType.VAL) {
                return false;
            }
        }
        return true;
    }

    protected Integer unpackOtherTypes(byte[] bytes, Offset offset, MsgPair msgPair) {
        Integer rawDataLength = null;
        TagPacker tagPacker = navigator.getTagPacker(msgPair.getMsgField());
        int tagPackedLength = tagPacker == null ? 0 : tagPacker.getPackedLength();
        boolean lengthFirst = MsgFieldType.getLengthFirstTypes().contains(msgPair.getMsgField().getType());
        Object tag = null;

        if (lengthFirst && MsgFieldType.isLengthType(msgPair.getMsgField())) {
            rawDataLength = unpackLength(bytes, offset, msgPair) - tagPackedLength;
        }

        if (MsgFieldType.getTaggedTypes().contains(msgPair.getMsgField().getType())) {
            tag = unpackTag(bytes, offset, msgPair);
        }

        boolean tagUnpackedButIsDifferent = tag != null && !Objects.equals(tag, msgPair.getMsgField().getTag());

        if (tagUnpackedButIsDifferent) {
            replaceWithSibling(msgPair, tag);
        }

        if (MsgFieldType.getTaggedTypes().contains(msgPair.getMsgField().getType())) {
            unpackTagBytes(bytes, offset, msgPair, tagPackedLength, tag);
        }

        if (!lengthFirst && MsgFieldType.isLengthType(msgPair.getMsgField())) {
            rawDataLength = unpackLength(bytes, offset, msgPair);
        }
        
        if (msgPair.getMsgField().getParent() != null && msgPair.getMsgField().getParent().getChildrenBodyLen() != null) {
            rawDataLength = msgPair.getMsgField().getParent().getChildrenBodyLen();
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
        MsgField currentMsgField = msgPair.getMsgField();
        rawDataLength = currentMsgField.getLen();
        if (rawDataLength == null) {
            MsgValue currentMsgValue = msgPair.getMsgValue();
            MsgValue currentMsgValueParent = currentMsgValue.getParent();
            if (currentMsgValueParent != null && currentMsgValueParent.getLengthBytes() != null) {
                LengthPacker parentLengthPacker = currentMsgField.getParent().getLengthPacker();
                if (parentLengthPacker == null) {
                    parentLengthPacker = currentMsgField.getParent().getParent().getChildrenLengthPacker();
                }
                int parentLength = parentLengthPacker.unpack(currentMsgValueParent.getLengthBytes(), 0);
                int childrenLength = calculateLengthUpTo(currentMsgValueParent.getChildren(), currentMsgValue);
                rawDataLength = parentLength - childrenLength;
            } else {
                rawDataLength = bytes.length - offset.getValue();
            }
        }
        int remaining = bytes.length - offset.getValue();
        if (rawDataLength > remaining) {
            throw new PackerRuntimeException("Cannot unpack bytes because the remaining data length '" + remaining +
                "' of the byte array is less than defined 'len' value '" + rawDataLength +
                "' of the MsgField with path '" + navigator.getPathRecursively(currentMsgField) + "'.");
        }
        unpackBodyBytes(bytes, offset, msgPair, rawDataLength);
        
        return rawDataLength;
    }

    private int calculateLengthUpTo(List<MsgValue> siblings, MsgValue currentSibling) {
        int length = 0;
        for (MsgValue sibling : siblings) {
            if (sibling == currentSibling) {
                return length;
            }
            length += sibling.getBodyBytes().length;
        }
        throw new PackerRuntimeException("It is expected, that the siblings' list contains the current sibling, " +
            "but the current sibling cannot be found in the list.\n" +
            "Siblings: '" + siblings + "'.\n" +
            "Current sibling: '" + currentSibling + "'.");
    }

    protected LengthPacker getLengthPackerFromParentOrSelfOrThrowException(MsgField msgField) {
        boolean parentContainsChildrenLengthPacker = msgField.getParent() != null &&
                msgField.getParent().getChildrenLengthPacker() != null;
        if (parentContainsChildrenLengthPacker) {
            return msgField.getParent().getChildrenLengthPacker();
        }
        
        if (msgField.getLengthPacker() == null) {
            throw new PackerRuntimeException("Property lengthPacker is not defined. Please define it by calling " +
                    "the .defineHeaderLengthPacker() method. " +
                    "Current MsgField: " + navigator.generatePath(msgField));
        }
        
        return msgField.getLengthPacker();
    }

    protected void unpackBodyBytes(byte[] bytes, Offset offset, MsgPair msgPair, Integer rawDataLength) {
        try {
            byte[] rawData = new byte[rawDataLength];
            System.arraycopy(bytes, offset.getValue(), rawData, 0, rawData.length);
            msgPair.getMsgValue().setBodyBytes(rawData);
        } catch (Exception e) {
            String path = navigator.getPathRecursively(msgPair.getMsgField());
            throw new PackerRuntimeException("Current MsgField: '" + path + "', source bytes length: '" + bytes.length +
                "', offset: '" + offset.getValue() + "', rawDataLength: '" + rawDataLength + "'", e);
        }
    }

    protected void unpackChildren(byte[] bytes, Offset offset, MsgPair msgPair, Integer rawDataLength) {
        int offsetWithChildren = offset.getValue() + rawDataLength;
        if (rawDataLength > 0) {
            msgPair.getMsgValue().setChildren(new ArrayList<>());
        }
        int childNum = 0;
        while (offset.getValue() < offsetWithChildren) {
            List<MsgField> children = msgPair.getMsgField().getChildren();
            MsgField nextMsgFieldChild = children.get(childNum);
            if (children.size() > childNum + 1) {
                childNum++; // the number of real children may be higher than defined in MsgField
                // for example for repeated values or undefined TLV and LTV values
            }
            MsgValue nextMsgValueChild = navigator.newFromNameAndTag(nextMsgFieldChild);
            
            msgPair.getMsgValue().getChildren().add(nextMsgValueChild);
            nextMsgValueChild.setParent(msgPair.getMsgValue());
            nextMsgValueChild.setRoot(nextMsgValueChild.getParent().getRoot());
            MsgPair msgPairChild = new MsgPair(nextMsgFieldChild, nextMsgValueChild);
            unpackFieldRecursively(bytes, offset, msgPairChild);
        }
        if (offset.getValue() != offsetWithChildren) {
            throw new PackerRuntimeException("Expected end of children is '" + offsetWithChildren +
                    "' but current offset is '" + offset.getValue() +
                    "'. These values should be equal.");
        }
    }

    protected void unpackTagBytes(byte[] bytes, Offset offset, MsgPair msgPair, Integer fieldTagLength,
                                              Object tag) {
        byte[] tagBytes = new byte[fieldTagLength];
        System.arraycopy(bytes, offset.getValue(), tagBytes, 0, tagBytes.length);
        offset.add(fieldTagLength);
        msgPair.getMsgValue().setTagBytes(tagBytes);
        msgPair.getMsgValue().setTag(tag);
    }

    protected Object unpackTag(byte[] bytes, Offset offset, MsgPair msgPair) {
        Object tag;
        TagPacker tagPacker = navigator.getTagPacker(msgPair.getMsgField());
        if (tagPacker == null) {
            tag = msgPair.getMsgField().getTag();
        } else {
            tag = tagPacker.unpack(bytes, offset.getValue());
        }
        return tag;
    }

    protected int unpackBitSet(byte[] bytes, Offset offset, MsgPair msgPair) {
        List<Integer> fieldNums = getFieldNumsFromBitSet(bytes, offset, msgPair);
        for (int nextFieldNum : fieldNums) {
            MsgField msgFieldChild = findChildByFieldNumUnsafe(msgPair.getMsgField(), nextFieldNum);
            // Bits 1 and 65 in a bitmap may be used as flags of a secondary and tertiary bitmaps, as well as flags of used msgFields
            if (msgFieldChild == null && (nextFieldNum == 1 || nextFieldNum == 65)) {
                continue;
            }
            if (msgFieldChild == null) {
                throw new PackerRuntimeException("Cannot find child with fieldNum '" + nextFieldNum +
                    "' in the msgField " + navigator.getPathRecursively(msgField));
            }
            MsgValue msgValueChild = navigator.newFromNameAndTag(msgFieldChild);
            List<MsgValue> children = msgPair.getMsgValue().getChildren();
            if (children == null) {
                children = new ArrayList<>();
                msgPair.getMsgValue().setChildren(children);
            }
            children.add(msgValueChild);
            msgValueChild.setParent(msgPair.getMsgValue());
            msgValueChild.setRoot(msgPair.getMsgValue().getRoot());
            msgPair.getMsgValue().getChildNamesMap().put(msgValueChild.getName(), msgValueChild);
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
        msgPair.getMsgValue().setLengthBytes(lengthBytes);
        int rawDataLength = lengthPacker.unpack(bytes, offset.getValue());
        offset.add(lenLength);
        return rawDataLength;
    }

    protected void setValueToLeaf(byte[] bytes, Offset offset, MsgPair msgPair, int bodyBytesLength) {
        BodyPacker bodyPacker = null;
        if (msgPair.getMsgField().getBodyPacker() != null) {
            bodyPacker = msgPair.getMsgField().getBodyPacker();
        } else if (msgPair.getMsgField().getParent() != null) {
            bodyPacker = msgPair.getMsgField().getParent().getChildrenBodyPacker();
        }
        if (bodyPacker == null) {
            String path = navigator.getPathRecursively(msgPair.getMsgField());
            throw new PackerRuntimeException("BodyPacker not found for MsgField with path '" + path + "'. " +
                "Please call the defineBodyPacker(...) method " +
                "of the FieldBuilder class, for example " +
                "MsgField subfield35 = FieldBuilder.builder(...).defineBodyPacker(...).");
        }
        Object bodyValue = bodyPacker.unpack(bytes, offset.getValue(), bodyBytesLength);
        msgPair.getMsgValue().setBodyValue(bodyValue);
    }

    protected void replaceWithSibling(MsgPair msgPair, Object tag) {
        MsgPair result = new MsgPair();
        MsgField msgFieldSibling = findSiblingByTag(tag, msgPair.getMsgField());
        boolean isUndefined = false;
        if (msgFieldSibling == null) {
            isUndefined = true;
            MsgField parent = msgPair.getMsgField().getParent();
            if (parent != null && parent.getChildrenTagPacker() != null) {
                MsgField undefinedMsgField = new MsgField();
                String originalName = msgPair.getMsgField().getName();
                Map<String, MsgValue> undefinedSiblings = msgPair.getMsgValue().getParent().getUndefinedChildrenMap();
                String cloneName = getCloneName(originalName, undefinedSiblings);

                undefinedMsgField.setName(cloneName);
                undefinedMsgField.setParent(parent);
                undefinedMsgField.setDepth(msgPair.getMsgField().getDepth());

                resolveBodyPacker(msgPair, parent, undefinedMsgField);

                resolveLengthPacker(msgPair, parent, undefinedMsgField);

                resolveTagPacker(msgPair, parent, undefinedMsgField);

                resolveLen(msgPair, parent, undefinedMsgField);

                MsgFieldType newType = msgPair.getMsgField().getType();
                undefinedMsgField.setType(newType);
                
                undefinedMsgField.setTag(tag);

                msgFieldSibling = undefinedMsgField;
            } else {
                throwSiblingNotFound(msgPair, tag);
            }
        }
        MsgValue oldMsgValue = msgPair.getMsgValue();
        MsgValue parentMsgValue = oldMsgValue.getParent();
        result.setMsgField(msgFieldSibling);
        assert msgFieldSibling != null;
        MsgValue newMsgValue = navigator.newFromNameAndTag(msgFieldSibling);
        if (parentMsgValue != null) {
            parentMsgValue.getChildren().remove(oldMsgValue);
            parentMsgValue.getChildren().add(newMsgValue);
            newMsgValue.setParent(parentMsgValue);
            newMsgValue.setRoot(parentMsgValue.getRoot());
            parentMsgValue.getChildNamesMap().put(newMsgValue.getName(), newMsgValue);
        }
        result.setMsgValue(newMsgValue);
        newMsgValue.setBitSet(oldMsgValue.getBitSet());
        newMsgValue.setTagBytes(oldMsgValue.getTagBytes());
        newMsgValue.setLengthBytes(oldMsgValue.getLengthBytes());

        if (isUndefined) {
            Map<String, MsgValue> undefinedChildrenMap = newMsgValue.getParent().getUndefinedChildrenMap();
            undefinedChildrenMap.put(newMsgValue.getName(), newMsgValue);
        }
        
        msgPair.setMsgField(result.getMsgField());
        msgPair.setMsgValue(result.getMsgValue());
    }

    protected void resolveLen(MsgPair msgPair, MsgField parent, MsgField undefinedMsgField) {
        Integer len;
        if (msgPair.getMsgField().getLen() != null) {
            len = msgPair.getMsgField().getLen();
        } else {
            len = parent.getChildrenBodyLen();
        }
        undefinedMsgField.setLen(len);
    }

    protected void resolveTagPacker(MsgPair msgPair, MsgField parent, MsgField undefinedMsgField) {
        TagPacker newTagPacker;
        if (msgPair.getMsgField().getTagPacker() != null) {
            newTagPacker = msgPair.getMsgField().getTagPacker();
        } else {
            newTagPacker = parent.getChildrenTagPacker();
        }
        undefinedMsgField.setTagPacker(newTagPacker);
    }

    protected void resolveLengthPacker(MsgPair msgPair, MsgField parent, MsgField undefinedMsgField) {
        LengthPacker newLengthPacker;
        if (msgPair.getMsgField().getLengthPacker() != null) {
            newLengthPacker = msgPair.getMsgField().getLengthPacker();
        } else {
            newLengthPacker = parent.getChildrenLengthPacker();
        }
        undefinedMsgField.setLengthPacker(newLengthPacker);
    }

    protected void resolveBodyPacker(MsgPair msgPair, MsgField parent, MsgField undefinedMsgField) {
        BodyPacker newBodyPacker;
        if (msgPair.getMsgField().getBodyPacker() != null) {
            newBodyPacker = msgPair.getMsgField().getBodyPacker();
        } else {
            newBodyPacker = parent.getChildrenBodyPacker();
        }
        undefinedMsgField.setBodyPacker(newBodyPacker);
    }

    protected String getCloneName(String originalName, Map<String, MsgValue> undefinedChildrenMap) {
        return originalName + "-clone-" + (undefinedChildrenMap.size() + 1);
    }

    protected List<Integer> getFieldNumsFromBitSet(byte[] bytes, Offset offset, MsgPair msgPair) {
        // unpack
        BitmapPacker bitmapPacker = msgPair.getMsgField().getBitMapPacker();
        if (bitmapPacker == null) {
            throw new PackerRuntimeException("Please call the defineHeaderBitmapPacker(...) " +
                    "method for this field " + navigator.getPathRecursively(msgPair.getMsgValue()));
                    
        }
        int consumed = bitmapPacker.unpack(msgPair.getMsgValue(), bytes, offset.getValue());
        byte[] bitMapBytes = new byte[consumed];
        System.arraycopy(bytes, offset.getValue(), bitMapBytes, 0, consumed);
        offset.add(consumed);
        
        msgPair.getMsgValue().setBodyBytes(bitMapBytes);

        return getFieldNumsAndValidateBitSet(msgPair);
    }

    protected MsgField findChildByFieldNumUnsafe(MsgField msgField, int nextFieldNum) {
        for (MsgField child : msgField.getChildren()) {
            if (nextFieldNum == child.getFieldNum()) {
                return child;
            }
        }
        return null;
    }

    protected List<Integer> getFieldNumsAndValidateBitSet(MsgPair msgPair) {
        BitSet unpackedBitSet = msgPair.getMsgValue().getBitSet();
        List<Integer> fieldNums = new ArrayList<>();
        int maxFieldNum = getMaxFieldNum(msgPair.getMsgField().getChildren(), msgPair.getMsgField());
        boolean secondaryBitmapMarked = false;
        boolean tertiaryBitmapMarked = false;
        for (int nextFieldNum = 1; nextFieldNum <= maxFieldNum; nextFieldNum++) {
            MsgField childMsgField = navigator.findByFieldNum(msgPair.getMsgField().getChildren(), nextFieldNum);
            boolean bitMarked = unpackedBitSet.get(nextFieldNum);
            // Bits 1 and 65 in a bitmap may be used as flags of a secondary and tertiary bitmaps, as well as flags of used msgFields
            if (!bitMarked || (childMsgField == null && (nextFieldNum == 65 || nextFieldNum == 1))) {
                continue;
            }
            if (childMsgField == null) {
                String path = navigator.getPathRecursively(msgPair.getMsgField());
                throw new PackerRuntimeException("Unpacked bitSet contains fieldNum '" + nextFieldNum + "', " +
                    "but the MsgField with path '" + path + "' has no child with such fieldNum. " +
                    "Please set the defineFieldNum(" + nextFieldNum + ") value " +
                    "to one of the field '" + path + "' children.");
            }

            secondaryBitmapMarked = markSecondaryBitmap(fieldNums, secondaryBitmapMarked, nextFieldNum);
            tertiaryBitmapMarked = markTertiaryBitmap(fieldNums, tertiaryBitmapMarked, nextFieldNum);
            
            fieldNums.add(nextFieldNum);
        }
        return fieldNums;
    }

    protected boolean markTertiaryBitmap(List<Integer> fieldNums, boolean tertiaryBitmapMarked, int nextFieldNum) {
        if (nextFieldNum > 129 && !tertiaryBitmapMarked) {
            int tertiaryFlagIndex = findTertiaryFlagIndex(fieldNums);
            fieldNums.add(tertiaryFlagIndex, 65);
            tertiaryBitmapMarked = true;
        }
        return tertiaryBitmapMarked;
    }

    protected boolean markSecondaryBitmap(List<Integer> fieldNums, boolean secondaryBitmapMarked, int nextFieldNum) {
        if (nextFieldNum > 65 && nextFieldNum < 130 && !secondaryBitmapMarked) {
            fieldNums.add(0, 1);
            secondaryBitmapMarked = true;
        }
        return secondaryBitmapMarked;
    }

    protected int findTertiaryFlagIndex(List<Integer> fieldNums) {
        int index = 0;
        for (int num : fieldNums) {
            if (num > 64) {
                return index;
            }
            index++;
        }
        return fieldNums.size();
    }

    protected void throwSiblingNotFound(MsgPair paramMsgPair, Object tag) {
        MsgField paramMsgField = paramMsgPair.getMsgField();
        MsgValue paramMsgValue = paramMsgPair.getMsgValue();
        String previousFieldCause = "";
        MsgField parentMsgField = paramMsgField.getParent();
        if (parentMsgField != null && parentMsgField.getLengthPacker() != null) {
            previousFieldCause = " \nNext cause is incorrect implementation of the " +
                parentMsgField.getLengthPacker().getClass().getSimpleName() +
                " class of the previous '" +
                navigator.getPathRecursively(parentMsgField) + "' field.";
        }
        String tagPackerClass = "null";
        TagPacker tagPacker = navigator.getTagPacker(paramMsgField);
        if (tagPacker != null) {
            tagPackerClass = tagPacker.getClass().getSimpleName();
        }
        throw new PackerRuntimeException("Cannot find a sibling with tag '" + tag +
            "' for the '" + navigator.getPathRecursively(paramMsgValue) +
            "' field. Its parent '" + navigator.getPathRecursively(parentMsgField) +
            "' has no child with such tag." +
            "\nThere are few possible causes of this error. " +
            "\nFirst cause is an incorrect implementation of " + tagPackerClass +
            ".unpack() method used for unpacking a tag from bytes. " +
            "\nSecond cause is an undefined (unknown) child with tag '" + tag +
            "' and parent '" + navigator.getPathRecursively(parentMsgField) +
            "' in the MsgField definition. Unknown TLV and LTV tags should have tag, length and body packers in the parent."
            + previousFieldCause + " " +
            "\nNext cause may be an incorrect order of tag and length subfields." +
            "\nNext cause is wrong place of actual context inside the rootMsgField, actual place is " +
            navigator.getPathRecursively(paramMsgField) + ".");
    }

    protected int getMaxFieldNum(List<MsgField> children, MsgField parent) {
        int result = 0;
        for (MsgField nextMsgField : children) {
            Integer fieldNum = nextMsgField.getFieldNum();
            if (fieldNum == null) {
                String path = navigator.getPathRecursively(nextMsgField);
                throw new PackerRuntimeException("The MsgField with path '" + path + "' " +
                    "has no 'fieldNum' property defined. " +
                    "Please call the defineFieldNum(...) value to the MsgField. " +
                    "The value is mandatory because its parent has the '" + MsgFieldType.class.getSimpleName() +
                    "." + parent.getType() + "' type.");
            }
            if (fieldNum > result) {
                result = fieldNum;
            }
        }
        return result;
    }

    protected MsgField findSiblingByTag(Object tag, MsgField msgField) {
        MsgField parent = msgField.getParent();
        for (MsgField child : parent.getChildren()) {
            if (Objects.equals(child.getTag(), tag)) {
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
        if (valueHolder.msgField == null) {
            throw new PackerRuntimeException("MsgField cannot be 'null'.");
        }
        valueHolder.msgValue = msgPair.getMsgValue();
        if (valueHolder.msgValue == null) {
            throw new PackerRuntimeException("MsgValue cannot be 'null'.");
        }
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
            MsgValue msgValueChild = msgValue.getChildNamesMap().get(childName);
            if (msgValueChild == null) {
                msgValueChild = createChildren(childName);
            }
            this.msgValue = msgValueChild;
            this.msgField = msgFieldChild;
            return this;
        } catch (Exception e) {
            MsgField rootMsgField = navigator.findRoot(msgField);
            if (e instanceof PackerRuntimeException) {
                throw new PackerRuntimeException("Message: " + e.getMessage(), e);
            } else {
                String dump = "\nCannot find a child." + ROOT_MSG_FIELD + visualizer.dumpMsgField(rootMsgField);
                throw new PackerRuntimeException("Exception message: " + e.getMessage() + dump, e);
            }
        }
    }

    protected MsgValue createChildren(String childName) {
        List<MsgField> msgFieldChildren = msgField.getChildren();
        if (msgValue.getChildren() == null) {
            msgValue.setChildren(new ArrayList<>());
        }
        List<MsgValue> msgValueChildren = msgValue.getChildren();
        Map<String, MsgValue> childNamesMap = msgValue.getChildNamesMap();

        MsgValue msgValueChild = null;

        for (int i = 0; i < msgFieldChildren.size(); i++) {
            MsgField nextMsgField = msgFieldChildren.get(i);
            MsgValue newMsgValue;
            if (msgValueChildren.size() < i + 1) {
                newMsgValue = createMsgValue(nextMsgField);
                msgValueChildren.add(newMsgValue);
            } else if (!nextMsgField.getName().equals(msgValueChildren.get(i).getName())) {
                newMsgValue = createMsgValue(nextMsgField);
                msgValueChildren.add(i, newMsgValue);
            } else {
                newMsgValue = msgValueChildren.get(i);
                if (!childNamesMap.containsKey(newMsgValue.getName())) {
                    childNamesMap.put(newMsgValue.getName(), newMsgValue);
                }
            }
            if (newMsgValue.getName().equals(childName)) {
                msgValueChild = newMsgValue;
            }
        }
        if (msgValueChild == null) {
            throw new PackerRuntimeException("Cannot create MsgValue with name " + childName);
        }
        return msgValueChild;
    }

    protected MsgValue createMsgValue(MsgField nextMsgField) {
        MsgValue newMsgValue = navigator.newFromNameAndTag(nextMsgField);
        newMsgValue.setParent(msgValue);
        newMsgValue.setRoot(msgValue.getRoot());
        msgValue.getChildNamesMap().put(newMsgValue.getName(), newMsgValue);
        return newMsgValue;
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
     * Length and tag are not mandatory.
     *
     * @param bodyValue can be 'null' for deletion the current msgField. In the case current positions of the
     *                  {@link #msgField} and {@link #msgValue} object graphs will be changed to theirs parents.
     * @return The current {@link ValueHolder} with the same {@link #msgValue} and {@link #msgField} in its context.
     */
    public ValueHolder setValue(Object bodyValue) {
        if (msgField.getChildren() != null && !msgField.getChildren().isEmpty()) {
            throw new PackerRuntimeException("Cannot set bodyValue to fields with children. Values can only be set to " +
                    "leaf fields. Field: " + navigator.getPathRecursively(msgField) + ", bodyValue: " + bodyValue);
        }
        if (bodyValue == null) {
            msgValue.setBodyBytes(null);
            msgValue.setLengthBytes(null);
            msgValue.setBodyValue(null);
            return this;
        }
        try {
            msgValue.setBodyValue(bodyValue);
            byte[] valueBytes = setBytes(bodyValue);
            setTagAndLenBytes(valueBytes, msgValue, msgField);
            return this;
        } catch (Exception e) {
            MsgValue rootMsgValue = navigator.findRoot(msgValue);
            MsgField rootMsgField = navigator.findRoot(msgField);
            MsgField appropriateMsgField = navigator.findByNameAndTagOrThrowException(rootMsgField, rootMsgValue);
            throw new PackerRuntimeException("Exception message: " + e.getMessage() + "\nCannot set bodyValue" +
                    " to field '" + navigator.getPathRecursively(msgField) + "'" +
                    "\nRoot MsgValue:\n" + visualizer.dumpMsgValue(appropriateMsgField, rootMsgValue, true) +
                    "\nThe MsgField:\n" + visualizer.dumpMsgField(msgField) +
                ROOT_MSG_FIELD + visualizer.dumpMsgField(rootMsgField), e);
        }
    }

    /**
     * Call the {@link #setValue(Object)} method. Current positions in the {@link #msgField} and {@link #msgValue}
     * object graphs remain unchanged.
     *
     * @param bodyValue    see the {@link #setValue(Object)} method description
     * @param absolutePath the {@link MsgValue#getName()}s from the root field
     * @return See the {@link #setValue(Object)} method description
     */
    public ValueHolder setValue(Object bodyValue, String... absolutePath) {
        MsgField currentMsgField = msgField;
        MsgValue currentMsgValue = msgValue;

        jumpAbsolute(absolutePath);
        setValue(bodyValue);
        
        this.msgField = currentMsgField;
        this.msgValue = currentMsgValue;
        return this;
    }

    /**
     * Call the {@link #setValue(Object, String...)} method.
     *
     * @param bodyValue    see the {@link #setValue(Object, String...)} method description
     * @param absolutePath the field names from the root field
     * @return See the {@link #setValue(Object, String...)} method description
     */
    public ValueHolder setValue(Object bodyValue, List<String> absolutePath) {
        return setValue(bodyValue, absolutePath.toArray(new String[0]));
    }

    protected byte[] setBytes(Object bodyValue) {
        BodyPacker bodyPacker = msgField.getBodyPacker();
        if (bodyPacker == null && msgField.getParent() != null) {
            bodyPacker = msgField.getParent().getChildrenBodyPacker();
        }

        if (bodyPacker == null) {
            throw new PackerRuntimeException("BodyPacker not found. Please call setBodyPacker(...) " +
                    "method\n" +
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
     * Set valueBytes to the msgValue
     * @param valueBytes source data
     * @param msgValue target object
     * @param msgField the field definition
     */
    protected void setTagAndLenBytes(byte[] valueBytes, MsgValue msgValue, MsgField msgField) {
        MsgField msgFieldParent = msgField.getParent();
        LengthPacker lengthPacker;

        Object tag = this.msgValue.getTag();
        if (msgField.getType() == MsgFieldType.LEN_TAG_VAL) {
            lengthPacker = msgField.getLengthPacker();
            if (lengthPacker == null) {
                assert msgFieldParent != null;
                lengthPacker = msgFieldParent.getChildrenLengthPacker();
            }
            packTagBytes(msgField, msgValue, tag);
            int tagAndValueLength = valueBytes.length + msgValue.getTagBytes().length;
            byte[] lengthBytes = lengthPacker.pack(tagAndValueLength);
            msgValue.setLengthBytes(lengthBytes);
        } else {
            lengthPacker = msgField.getLengthPacker();
            if (lengthPacker == null && msgField.getParent() != null) {
                lengthPacker = msgField.getParent().getChildrenLengthPacker();
            }
            if (MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
                packTagBytes(msgField, msgValue, tag);
            }

            if (msgField.getLen() == null && lengthPacker != null) {
                byte[] lengthBytes = lengthPacker.pack(valueBytes.length);
                msgValue.setLengthBytes(lengthBytes);
            }
        }
    }

    protected void packTagBytes(MsgField msgField, MsgValue msgValue, Object tag) {
        TagPacker tagPacker = navigator.getTagPacker(msgField);
        if (tagPacker == null) {
            msgValue.setTagBytes(new byte[0]);
            return;
        }
        byte[] tagBytes = tagPacker.pack(tag);
        msgValue.setTagBytes(tagBytes);
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
            msgValue = parentMsgValue.getChildNamesMap().get(siblingName);
            if (msgValue == null) {
                throw new PackerRuntimeException("Cannot find child MsgValue with name '" + siblingName +
                    "' for parent " + parentMsgValue);
            }
            msgField = msgFieldSibling;
            return this;
        } catch (Exception e) {
            if (msgValue != null && msgField != null) {
                MsgValue rootMsgValue = navigator.findRoot(msgValue);
                MsgField rootMsgField = navigator.findRoot(msgField);
                throw new PackerRuntimeException("Exception: " + e.getMessage() + "\nCannot jumpToSibling '" + siblingName +
                    "' of the message definition." +
                    "\nMsgValue:\n" + visualizer.dumpMsgValue(rootMsgField, rootMsgValue, true) +
                    MSG_FIELD + visualizer.dumpMsgField(rootMsgField) + "\n", e);
            }
            throw e;
        }
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
        msgValue.getChildNamesMap().clear();
        for (MsgValue child : subfields) {
            child.setParent(msgValue);
            msgValue.getChildNamesMap().put(child.getName(), child);
            child.setRoot(msgValue.getRoot());
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
        msgValue = msgValue.getRoot();
        msgField = msgField.getRoot();
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
            if (msgField != null && msgValue != null) {
                MsgValue rootMsgValue = navigator.findRoot(msgValue);
                MsgField rootMsgField = navigator.findRoot(msgField);
                if (Objects.equals(rootMsgValue.getName(), rootMsgField.getName()) &&
                    Objects.equals(rootMsgValue.getTag(), rootMsgField.getTag())) {
                    
                    throw new PackerRuntimeException("Exception: " + e.getMessage() + "\n" +
                        "Cannot pack field '" + navigator.getPathRecursively(msgValue) + "'" +
                        PARTIAL_DUMP + visualizer.dumpMsgValue(rootMsgField, rootMsgValue, true) +
                        MSG_FIELD + visualizer.dumpMsgField(msgField) + "\n", e);
                }
            }
            throw new PackerRuntimeException(e);
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
                packBodyBytesAndLengthAndTag(msgValue, msgField, messageBytes, bytes);
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
        BitmapPacker bitmapPacker = msgField.getBitMapPacker();
        if (bitmapPacker == null) {
            throw new PackerRuntimeException("The value of '" + BitmapPacker.class.getSimpleName() +
                "' type is mandatory for '" + MsgFieldType.class.getSimpleName() + 
                "' '" + MsgFieldType.BIT_SET + "' type. " +
                "Please call the defineHeaderBitmapPacker(...) method.");
        }
        BitSet bitSet = new BitSet();
        int maxFieldNum = 0;
        for (MsgField nextMsgField : msgField.getChildren()) {
            Integer fieldNum = nextMsgField.getFieldNum();
            MsgValue nextMsgValue = navigator.findByFieldNum(msgValue.getChildren(), fieldNum);
            if (nextMsgValue != null &&
                (nextMsgValue.getBodyValue() != null ||
                    (nextMsgValue.getChildren() != null && !nextMsgValue.getChildren().isEmpty())
                )
            ) {
                maxFieldNum = Math.max(maxFieldNum, fieldNum);
                bitSet.set(fieldNum);
            }
        }
        if (maxFieldNum > 64) {
            bitSet.set(1);
        }
        if (maxFieldNum > 128) {
            bitSet.set(65);
        }
        msgValue.setBitSet(bitSet);
        byte[] bytes = bitmapPacker.pack(bitSet);
        msgValue.setBodyBytes(bytes);
        messageBytes.write(bytes);
    }

    protected void packBodyBytesAndLengthAndTag(MsgValue msgValue, MsgField msgField, ByteArrayOutputStream messageBytes, byte[] bytes) throws IOException {
        msgValue.setBodyBytes(bytes);
        LengthPacker lengthPacker;
        boolean lengthPrecedesTag = msgField.getType() == MsgFieldType.LEN_TAG_VAL;
        if (MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
            setTagBytes(msgValue, msgField);
        }
        if (MsgFieldType.getLengthTypes().contains(msgField.getType())) {
            lengthPacker = getLengthPackerFromParentOrSelfOrThrowException(msgField);
            packLength(msgValue, lengthPacker, bytes, lengthPrecedesTag);
        }

        if (lengthPrecedesTag) {
            writeLengthBytesIfAllowed(msgValue, msgField, messageBytes);
            writeTagBytesIfAllowed(msgValue, msgField, messageBytes);
        } else {
            writeTagBytesIfAllowed(msgValue, msgField, messageBytes);
            writeLengthBytesIfAllowed(msgValue, msgField, messageBytes);
        }
    }

    protected void writeTagBytesIfAllowed(MsgValue msgValue, MsgField msgField, ByteArrayOutputStream messageBytes) throws IOException {
        if (MsgFieldType.getTaggedTypes().contains(msgField.getType())) {
            messageBytes.write(msgValue.getTagBytes());
        }
    }

    protected void writeLengthBytesIfAllowed(MsgValue msgValue, MsgField msgField,
                                                  ByteArrayOutputStream messageBytes) throws IOException {
        if (MsgFieldType.getLengthTypes().contains(msgField.getType())) {
            messageBytes.write(msgValue.getLengthBytes());
        }
    }

    protected ByteArrayOutputStream packHeaderAndValue(MsgValue msgValue, MsgField msgField) throws IOException {
        if (msgValue.getBodyBytes() == null) {
            throw new PackerRuntimeException("Expected non-null MsgValue.bodyBytes but found 'null'. MsgValue path: '" +
                navigator.getPathRecursively(msgValue) + "'. Please set the value or delete the MsgField. " +
                    "The cause of the exception probably in the setValue() method.");
        }
        int length = msgValue.getBodyBytes().length;
        ByteArrayOutputStream result;

        length += msgValue.getTagBytes() == null ? 0 : msgValue.getTagBytes().length;
        length += msgValue.getLengthBytes() == null ? 0 : msgValue.getLengthBytes().length;
        result = new ByteArrayOutputStream(length);

        boolean lengthPrecedesTag = msgField.getType() == MsgFieldType.LEN_TAG_VAL;

        if (lengthPrecedesTag) {
            writeLengthBytesIfExist(result, msgValue);
            writeTagBytes(result, msgValue);
        } else {
            writeTagBytes(result, msgValue);
            writeLengthBytesIfExist(result, msgValue);
        }

        if (msgValue.getBodyBytes() != null) {
            result.write(msgValue.getBodyBytes());
        }
        return result;
    }

    protected void writeTagBytes(ByteArrayOutputStream result, MsgValue msgValue) throws IOException {
        if (msgValue.getTagBytes() != null) {
            result.write(msgValue.getTagBytes());
        }
    }

    protected void writeLengthBytesIfExist(ByteArrayOutputStream result, MsgValue msgValue) throws IOException {
        if (msgValue.getLengthBytes() != null) {
            result.write(msgValue.getLengthBytes());
        }
    }

    protected void packLength(MsgValue msgValue, LengthPacker lengthPacker, byte[] bytes, boolean lengthPrecedesTag) {
        int bytesLength;
        if (lengthPrecedesTag) {
            bytesLength = bytes.length + msgValue.getTagBytes().length;
        } else {
            bytesLength = bytes.length;
        }
        byte[] lengthBytes = lengthPacker.pack(bytesLength);
        msgValue.setLengthBytes(lengthBytes);
    }

    protected void setTagBytes(MsgValue msgValue, MsgField msgField) {
        Object tag = msgValue.getTag();
        TagPacker tagPacker = navigator.getTagPacker(msgField);
        assert tagPacker != null;
        byte[] tagBytes = tagPacker.pack(tag);
        msgValue.setTagBytes(tagBytes);
    }

    /**
     * Create a copy from the current {@link #msgValue} and set it to this {@link #msgValue} context.
     * This sibling will have the same {@link MsgField#getName()}, {@link MsgField#getTag()}
     * and {@link MsgField#getFieldNum()} as its sibling.
     * <p>
     * It is useful for creation of multiple repeated fields with the same name and/or fieldNum.
     *
     * @return The current actual {@link ValueHolder}.
     */
    public ValueHolder cloneSibling() {
        MsgValue clone = navigator.newFromNameAndTag(msgField);
        clone.setParent(msgValue.getParent());
        clone.setRoot(msgValue.getParent().getRoot());
        msgValue.getParent().getChildren().add(clone);
        clone.getParent().getChildNamesMap().put(clone.getName(), clone);
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
        unpackMsgField(bytes, 0);
        return msgValue;
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
     * @return the current {@link #navigator}.
     */
    public Navigator getNavigator() {
        return this.navigator;
    }

    /**
     * @param visualizer see the {@link #visualizer} field description.
     */
    public void setVisualizer(Visualizer visualizer) {
        this.visualizer = visualizer;
    }

    /**
     * @return The {@link #visualizer} value.
     */
    public Visualizer getVisualizer() {
        return visualizer;
    }

    /**
     * Change the actual {@link #msgValue} object graph place (location) to be the same as the {@link #msgField},
     * see the {@link Navigator#synchronizeMessageValue(MsgField, MsgValue)} method description.
     *
     * @return The current {@link ValueHolder} instance with adjusted {@link #msgValue}.
     */
    public ValueHolder adjust() {
        msgValue = this.navigator.synchronizeMessageValue(msgField, msgValue);
        return this;
    }

    /**
     * Change the actual object graph places (locations) of the {@link #msgField} and {@link #msgValue}.
     *
     * @param fieldNames the {@link MsgField#getName()}s of fields from the root of the {@link #msgField}.
     * @return The current {@link ValueHolder} instance with a new place (location) of the {@link #msgValue}.
     */
    public ValueHolder jumpAbsolute(String ... fieldNames) {
        jumpToRoot();
        if (!fieldNames[0].equals(msgField.getName())) {
            String msgFieldDump = visualizer.dumpMsgField(msgField);
            throw new PackerRuntimeException("MsgField with name '" + fieldNames[0] + "' is not a root field. " +
                "Please define the absolute path from the root field. " +
                "Actual root field name: '" + msgField.getName() + "'." +
                MSG_FIELD + msgFieldDump);
        }
        for (int i = 1; i < fieldNames.length; i++) {
            String name = fieldNames[i];
            if (name == null) {
                throw new PackerRuntimeException("MsgField name cannot be 'null'. " +
                    "FieldNames: " + Arrays.toString(fieldNames));
            }
            try {
                jumpToChild(name);
            } catch (Exception e) {
                throw new PackerRuntimeException("Cannot find MsgField with path '" +
                    Arrays.toString(fieldNames) + "'", e);
            }
        }
        return this;
    }

    /**
     * Call the {@link #jumpAbsolute(String...)} method.
     *
     * @param fieldNames the {@link MsgField#getName()}s of fields from the root of the {@link #msgField}.
     * @return The current {@link ValueHolder} instance with a new place (location) of the {@link #msgValue}.
     */
    public ValueHolder jumpAbsolute(List<String> fieldNames) {
        return jumpAbsolute(fieldNames.toArray(new String[0]));
    }

    /**
     * Call the {@link #getValue(Class)} method with the type of {@link #msgValue} body value.
     * @param <T> to be used for casting the returned value
     * @return the {@link MsgValue#getBodyValue()} casted to <i>T</i> or 'null'.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        Object bodyValue = this.msgValue.getBodyValue();
        if (bodyValue == null) {
            return null;
        }
        return (T) getValue(bodyValue.getClass());
    }

    /**
     * Call the {@link #getValue(Class)} method with the type of {@link #msgValue} body value. Current positions in the
     * {@link #msgField} and {@link #msgValue} object graphs remain unchanged.
     *
     * @param <T>          to be used for casting the returned value
     * @param absolutePath the {@link MsgValue#getName()}s of fields from the root of the {@link #msgValue}.
     * @return the {@link MsgValue#getBodyValue()} casted to <i>T</i> or 'null'.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String... absolutePath) {
        MsgField currentMsgField = this.msgField;
        MsgValue currentMsgValue = this.msgValue;

        jumpAbsolute(absolutePath);
        
        Object bodyValue = this.msgValue.getBodyValue();
        
        if (bodyValue == null) {
            return null;
        }
        T value = (T) getValue(bodyValue.getClass());
        
        this.msgField = currentMsgField;
        this.msgValue = currentMsgValue;
        
        return value;
    }

    /**
     * Call the {@link #getValue(String...)} method.
     * @param <T> to be used for casting the returned value
     * @param absolutePath the {@link MsgValue#getName()}s of fields from the root of the {@link #msgValue}
     * @return the {@link MsgValue#getBodyValue()} casted to <i>T</i> or 'null'.
     */
    public <T> T getValue(List<String> absolutePath) {
        return getValue(absolutePath.toArray(new String[0]));
    }

    /**
     * Check whether the current {@link #msgValue} contains a {@link MsgValue#getBodyValue()}. Current
     * {@link #msgField} and {@link #msgValue} will not be changed.
     * @param absolutePath the {@link MsgValue#getName()}s of fields from the root of the {@link #msgValue}.
     * @return 'true' if the field on the path has some value.
     */
    public boolean hasValue(String... absolutePath) {
        if (absolutePath.length < 2) {
            throw new PackerRuntimeException("Please define absolutePath as a path, for example 'ROOT', 'field1' ...");
        }
        MsgValue currentMsgValue = msgValue;
        while (currentMsgValue.getParent() != null) {
            currentMsgValue = currentMsgValue.getParent();
        }
        for (int i = 1; i < absolutePath.length; i++) {
            List<MsgValue> children = currentMsgValue.getChildren();
            if (currentMsgValue.getBodyValue() == null && children == null) {
                return false;
            }
            currentMsgValue = navigator.findByName(children, absolutePath[i]);
            if (currentMsgValue == null ||
                (currentMsgValue.getBodyValue() == null && currentMsgValue.getChildren() == null)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Call the {@link #hasValue(String...)} method.
     * @param absolutePath see the {@link #hasValue(String...)} method description.
     * @return See the {@link #hasValue(String...)} method description.
     */
    public boolean hasValue(List<String> absolutePath) {
        return hasValue(absolutePath.toArray(new String[0]));
    }

    /**
     * Set the {@link #msgField} and {@link #msgValue} to the current instance of the {@link ValueHolder}.
     * @param msgField will be assigned to {@link #msgField}
     * @param msgValue will be assigned to {@link #msgValue}
     */
    public void setCurrent(MsgField msgField, MsgValue msgValue) {
        this.msgField = msgField;
        this.msgValue = msgValue;
    }
}
