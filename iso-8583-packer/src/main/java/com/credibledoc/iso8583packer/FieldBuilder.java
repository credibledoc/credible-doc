package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.header.HeaderField;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.masking.Masker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.navigator.NavigatorService;
import com.credibledoc.iso8583packer.stringer.Stringer;
import com.credibledoc.iso8583packer.tag.TagPacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Builder of ISOMsg structure. The instances of this builder contain the {@link #msgField} state.
 * <p>
 * The instance of this builder has a single {@link #msgField} in its context. This field contains a state information.
 * <p>
 * This builder provides methods for {@link MsgField} creation, see the {@link #builder(MsgFieldType)} method or for handling existing
 * {@link MsgField}, see the {@link #from(MsgField)} method.
 * <p>
 * This builder helps to set {@link MsgField}'s properties, see for example the {@link #defineName(String)} or
 * {@link #defineParent(MsgField)} methods.
 * <p>
 * // TODO Kyrylo Semenko - upravit finalne po refaktoringu.
 * Example of new node creation
 * <pre>
 *             // TODO Kyrylo Semenko - upravit finalne po refaktoringu.
 * </pre>
 *
 * @author Kyrylo Semenko
 */
public class FieldBuilder {
    private static final Logger logger = LoggerFactory.getLogger(FieldBuilder.class);

    /**
     * The context and state of this builder instance.
     */
    private MsgField msgField;

    /**
     * Create a new {@link FieldBuilder} with a new empty {@link #msgField}.
     * 
     * Example of the root field creation. This root field contains the single Bmp48 subfield.
     * // TODO Kyrylo Semenko - provest revizi prikladu
     * <pre>
     *     // TODO Kyrylo Semenko - upravit finalne po refaktoringu.
     * </pre>
     * 
     * Example of the first level field creation.
     * <pre>
     *     // TODO Kyrylo Semenko - upravit finalne po refaktoringu.
     * </pre>
     * @param msgFieldType mandatory type
     * @return The new instance of {@link FieldBuilder}.
     */
    public static FieldBuilder builder(MsgFieldType msgFieldType) {
        FieldBuilder fieldBuilder = new FieldBuilder();
        fieldBuilder.msgField = new MsgField();
        fieldBuilder.msgField.setType(msgFieldType);
        return fieldBuilder;
    }

    /**
     * Copy the field from the argument and create a new {@link FieldBuilder} instance with the new field in its 
     * context.
     * <p>
     * Set all properties from the argument 'example' and its {@link HeaderField} to the created field except
     * {@link MsgField#setName(String)}, {@link MsgField#setTagNum(Integer)}, {@link MsgField#setParent(MsgField)}
     * and {@link MsgField#setChildren(List)}.
     * <p>
     *
     * @param example where some properties are copied from.
     * @return this instance of {@link FieldBuilder}.
     */
    public static FieldBuilder clone(MsgField example) {
        FieldBuilder fieldBuilder = builder(example.getType());
        MsgField msgField = fieldBuilder.getCurrentField();
        msgField.setChildrenLengthPacker(example.getChildrenLengthPacker());
        msgField.setBodyPacker(example.getBodyPacker());
        msgField.setChildrenTagPacker(example.getChildrenTagPacker());
        msgField.setChildrenLengthPacker(example.getChildrenLengthPacker());
        msgField.setChildTagLength(example.getChildTagLength());
        msgField.setExactlyLength(example.getExactlyLength());
        msgField.setLen(example.getLen());
        msgField.setMaxLen(example.getMaxLen());
        msgField.setMasker(example.getMasker());
        msgField.setStringer(example.getStringer());
        
        HeaderField headerField = msgField.getHeaderField();
        HeaderField exampleHeaderField = example.getHeaderField();
        headerField.setBitMapPacker(exampleHeaderField.getBitMapPacker());
        headerField.setBitSet(exampleHeaderField.getBitSet());
        headerField.setLengthPacker(exampleHeaderField.getLengthPacker());
        
        return fieldBuilder;
    }

    /**
     * Call the {@link #validateStructure(MsgField)} method with the current {@link #msgField} as argument.
     */
    public void validateStructure() {
        validateStructure(msgField);
    }

    /**
     * Check restrictions of the Field structure and all its children recursively.
     *
     * @param current the {@link MsgField} to be checked.
     */
    // TODO Kyrylo Semenko - presunout do static servisy
    public static void validateStructure(MsgField current) {
        try {
            validateStructureRecursively(current);
        } catch (Exception e) {
            MsgField root = NavigatorService.findRoot(current);
            throw new PackerRuntimeException("Validation failed, message:\n" + e.getMessage() + "\n" +
                    "Root MsgField:\n" + DumpService.dumpMsgField(root), e);
        }
    }

    /**
     * Check restrictions of the Field structure and all its children recursively.
     *
     * @param msgField the {@link MsgField} to be checked.
     */
    // TODO Kyrylo Semenko - presunout do static servisy
    private static void validateStructureRecursively(MsgField msgField) {
        if (msgField == null) {
            throw new PackerRuntimeException("Field is null");
        }
        
        String path = NavigatorService.getPathRecursively(msgField);
        if (msgField.getTagNum() == null && msgField.getName() == null && msgField.getType() != MsgFieldType.VAL) {
            throw new PackerRuntimeException("At least one of 'tagNum' or 'name' should be set to the field " +
                    "but the both properties are 'null'. Field path: '" + path + "'");
        }
        
        if (msgField.getParent() != null && msgField.getBodyPacker() == null) {
            throw new PackerRuntimeException("Please call the method: defineBodyPacker() " +
                    "because the 'BodyPacker' value is mandatory. " +
                    "Field path: '" + path + "'. The 'BodyPacker' value is not mandatory for root field only.");
        }
        
        if (msgField.getType() == null) {
            throw new PackerRuntimeException("Please set the " + MsgFieldType.class.getSimpleName() +
                    " value to the field with path: '" + path + "'");
        }
        
        if (msgField.getType() == MsgFieldType.TAG_LEN_VAL || msgField.getType() == MsgFieldType.LEN_TAG_VAL) {
            validateTagAndTagPackerExists(msgField, path);
            validateHeaderOrParentLengthPackerExists(msgField, path);
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.TAG_VAL) {
            validateTagAndTagPackerExists(msgField, path);
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.LEN_VAL) {
            validateHeaderOrParentLengthPackerExists(msgField, path);
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.VAL) {
            validateLenExists(msgField, path);
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.BIT_SET) {
            validateBitSetAndBitMapPackerExists(msgField, path);
            validateChildrenExists(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.LEN_VAL_BIT_SET) {
            validateParentExists(msgField, path);
            validateParentIsBitSet(msgField, path);
            validateHeaderOrParentLengthPackerExists(msgField, path);
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }

        List<MsgField> msgFields = msgField.getChildren();
        if (msgFields != null) {
            for (MsgField nextMsgField : msgFields) {
                validateStructureRecursively(nextMsgField);
            }
        }
    }

    private static void validateHasNoBitSetAndBitMapPacker(MsgField msgField, String path) {
        if (msgField.getHeaderField() != null && msgField.getHeaderField().getBitSet() != null) {
            throw new PackerRuntimeException("BitSet is not allowed for MsgField '" + path +
                    "' with MsgType '" + msgField.getType() + "' because that doesn't make sense.");
        }
        if (msgField.getHeaderField() != null && msgField.getHeaderField().getBitMapPacker() != null) {
            throw new PackerRuntimeException("BitMapPacker is not allowed for MsgField '" + path +
                    "' with MsgType '" + msgField.getType() + "' because that doesn't make sense.");
        }
    }

    private static void validateParentIsBitSet(MsgField msgField, String path) {
        if (msgField.getParent() == null || msgField.getParent().getType() != MsgFieldType.BIT_SET) {
            throw new PackerRuntimeException("The parent field must have the '" + MsgFieldType.BIT_SET +
                    "' type because the MsgField has the '" + msgField.getType() +
                    "' type. MsgField: '" + path + "'. Please call the defineHeaderBitSet() method on the parent field.");
        }
    }

    private static void validateParentExists(MsgField msgField, String path) {
        if (msgField.getParent() == null) {
            throw new PackerRuntimeException("Parent is mandatory for MsgField of MsgFieldType '" + msgField.getType() +
                    "'. MsgField: '" + path + "'. Please call the defineParent() method.");
        }
    }

    private static void validateTagAndTagPackerExists(MsgField msgField, String path) {
        boolean parentPackerExists = msgField.getParent() != null && msgField.getParent().getChildrenTagPacker() != null;
        boolean parentTagLengthExists = msgField.getParent() != null && msgField.getParent().getChildTagLength() != null;
        boolean tagNumExists = msgField.getTagNum() != null;

        if (!parentPackerExists) {
            String parentPath = NavigatorService.getPathRecursively(msgField.getParent());
            throw new PackerRuntimeException("Please define the '" + TagPacker.class.getSimpleName() +
                    "' value to the '" + parentPath + "' field, " +
                    "because that is mandatory for MsgFieldType '" + msgField.getType() + "'. Please call the .defineChildrenTagPacker(...) method.");
        }

        if (!parentTagLengthExists) {
            throw new PackerRuntimeException("Please define the childTagLength value to the parent of the field '" + path +
                    "' because it is mandatory for MsgFieldType '" + msgField.getType() + "'.");
        }

        if (!tagNumExists) {
            throw new PackerRuntimeException("Please define the tagNum value to the field '" + path +
                    "', it is mandatory for MsgFieldType '" + msgField.getType() + "'.");
        }
    }

    private static void validateBitSetAndBitMapPackerExists(MsgField msgField, String path) {
        boolean hasNoHeader = msgField.getHeaderField() == null;
        if (hasNoHeader || msgField.getHeaderField().getBitMapPacker() == null) {
            throw new PackerRuntimeException("The bitMapPacker value is mandatory for '" + msgField.getType() +
                    "' field type. Please call the defineHeaderBitMapPacker(...) method. Field path: " + path + ".");
        }
    }

    private static void validateHeaderOrParentLengthPackerExists(MsgField msgField, String path) {
        boolean parentHasLengthPacker = msgField.getParent() != null && msgField.getParent().getChildrenLengthPacker() != null;
        boolean fieldHasLengthPacker = msgField.getHeaderField() != null && msgField.getHeaderField().getLengthPacker() != null;
        if (!parentHasLengthPacker && !fieldHasLengthPacker) {
            throw new PackerRuntimeException("The '" + LengthPacker.class.getSimpleName() +
                    "' value is mandatory for the field '" + path +
                    "' or its parent because it has the '" + msgField.getType() +
                    "' type. Please call the defineHeaderLengthPacker() method on the field " +
                    "or defineChildrenLengthPacker() method on the field parent.");
        }
    }

    private static void validateLenExists(MsgField msgField, String path) {
        if (msgField.getLen() == null) {
            throw new PackerRuntimeException("The field with path '" + path +
                    "' is a '" + msgField.getType() +
                    "' so please define its length by calling the defineLen() method.");
        }
    }

    private static void validateChildrenExists(MsgField msgField, String path) {
        if (msgField.getChildren() == null || msgField.getChildren().isEmpty()) {
            throw new PackerRuntimeException("The field '" + path +
                    "' has no children but has the '" + msgField.getType() +
                    "' type. Please define at least one child with the .createChild(...) method.");
        }
    }

    /**
     * Get the field from the argument and instantiate a new {@link FieldBuilder} with this field in its context.
     *
     * @param msgField will be {@link FieldBuilder#msgField} value.
     * @return Created {@link FieldBuilder}.
     */
    public static FieldBuilder from(MsgField msgField) {
        FieldBuilder fieldBuilder = new FieldBuilder();
        fieldBuilder.msgField = msgField;
        return fieldBuilder;
    }

    /**
     * Set the {@link MsgField#setTagNum(Integer)} value.
         * @param fieldNum for example String FC= can be write as hex 46433D and int 4604733. It depends on {@link TagPacker} defined
     *                 in dhe {@link #msgField} how this tag will be presented.
     *                 
     * @return The current instance of {@link FieldBuilder} with {@link #msgField} in its context.
     */
    public FieldBuilder defineTagNum(int fieldNum) {
        this.msgField.setTagNum(fieldNum);
        return this;
    }

    /**
     * Set the {@link MsgField#setName(String)} value. It uses for dumping and navigation only.
     * @param name the {@link MsgField#setName(String)} value.
     * @return The current instance of {@link FieldBuilder} with {@link #msgField} in its context.
     */
    public FieldBuilder defineName(String name) {
        this.msgField.setName(name);
        return this;
    }

    public FieldBuilder defineMaxLen(int maxLength) {
        this.msgField.setMaxLen(maxLength);
        return this;
    }

    /**
     * Set the {@link MsgField#setBodyPacker(BodyPacker)} value to the current {@link #msgField}.
     * @param bodyPacker to be set or unset if 'null'.
     * @return The current instance of {@link FieldBuilder} with {@link #msgField} in its context.
     */
    public FieldBuilder defineBodyPacker(BodyPacker bodyPacker) {
        this.msgField.setBodyPacker(bodyPacker);
        return this;
    }

    /**
     * @return The {@link #msgField} from the context of this {@link FieldBuilder}.
     */
    public MsgField getCurrentField() {
        return this.msgField;
    }

    /**
     * Set the {@link MsgField#setLen(Integer)} value. The value can be set to fields with fixed length only.
     * <p>
     * These fields may have a {@link HeaderField} if it is required in documentation, but it is not used for unpacking.
     *
     * @param fieldLen can be 'null' for unset.
     * @return The current actual {@link FieldBuilder}
     */
    public FieldBuilder defineLen(Integer fieldLen) {
        this.msgField.setLen(fieldLen);
        return this;
    }

    public FieldBuilder defineParent(MsgField parentMsgField) {
        if (parentMsgField == msgField.getParent()) {
            logger.info("Parent field '" + NavigatorService.getPathRecursively(parentMsgField) +
                    "' already set to the field '" + NavigatorService.getPathRecursively(msgField) +
                    "'. Skip the defineParent() method.");
            return this;
        }
        if (parentMsgField.getChildTagLength() == null) {
            throw new PackerRuntimeException("Field '" + NavigatorService.getPathRecursively(parentMsgField) +
                    "' has no the ChildTagLength property defined. Please set the property.");
        }
        if (parentMsgField.getChildrenTagPacker() == null) {
            throw new PackerRuntimeException("Field '" + NavigatorService.getPathRecursively(parentMsgField) +
                    "' has no the ChildrenTagPacker property defined. Please set the property.");
        }
        if (parentMsgField.getChildrenLengthPacker() != null &&
                msgField.getHeaderField() != null && msgField.getHeaderField().getLengthPacker() != null) {
            throw new PackerRuntimeException(createMessageSameLengthPacker(parentMsgField, msgField));
        }
        this.msgField.setParent(parentMsgField);
        List<MsgField> msgFields = parentMsgField.getChildren();
        if (msgFields == null) {
            msgFields = new ArrayList<>();
            parentMsgField.setChildren(msgFields);
        }
        msgFields.add(msgField);
        return this;
    }

    private static String createMessageSameLengthPacker(MsgField parent, MsgField child) {
        return "The *lengthPacker* value cannot be set to the both, the " +
                "parent and child. Only one of them should have the value. " +
                "Parent: " + NavigatorService.getPathRecursively(parent) + ", " +
                "child: " + NavigatorService.getPathRecursively(child) + ". \nThe value should be set " +
                "to the parent in cases when the length subfield precedes the tagNum subfield. \nThe " +
                "value should be set to the child when the tagNum subfield precedes the length subfield.";
    }

    public FieldBuilder defineChildrenTagPacker(TagPacker tagPacker) {
        msgField.setChildrenTagPacker(tagPacker);
        return this;
    }

    /**
     * Call the {@link MsgField#setChildTagLength(Integer)} method.
     *
     * @param length can be 'null' for unset. In this case the {@link MsgField#setChildrenTagPacker(TagPacker)} method
     *               with 'null' value will be called too.
     * @return The current instance of the {@link FieldBuilder} with a {@link #msgField} in its context.
     */
    public FieldBuilder defineChildrenTagLen(Integer length) {
        msgField.setChildTagLength(length);
        if (length == null) {
            msgField.setChildrenLengthPacker(null);
        }
        return this;
    }

    /**
     * Find the <b>first</b> child of the current context {@link MsgField} with defined name.
     *
     * @param childName the {@link MsgField#getName()} property for searching.
     * @return The found child. Throw an exception if this context does not have children or child not found.
     */
    public FieldBuilder jumpToChild(String childName) {
        msgField = NavigatorService.getChildOrThrowException(childName, msgField);
        return this;
    }

    /**
     * Set the {@link HeaderField#setLengthPacker(LengthPacker)} property.
     * 
     * Known packers: // TODO Kyrylo Semenko - doplnit
     * 
     * @param lengthPacker can be 'null' for unset.
     * @return The current instance of the {@link FieldBuilder} with a {@link #msgField} in its context.
     */
    public FieldBuilder defineHeaderLengthPacker(LengthPacker lengthPacker) {
        if (msgField.getParent() != null) {
            MsgField parent = msgField.getParent();
            if (parent.getChildrenLengthPacker() != null) {
                throw new PackerRuntimeException(createMessageSameLengthPacker(parent, msgField));
            }
        }
        msgField.getHeaderField().setLengthPacker(lengthPacker);
        return this;
    }

    public FieldBuilder jumpToParent() {
        if (msgField.getParent() == null) {
            throw new PackerRuntimeException("The field '" + NavigatorService.getPathRecursively(msgField) + "' has no parent.");
        }
        msgField = msgField.getParent();
        return this;
    }

    /**
     * Set the {@link MsgField#setExactlyLength(Integer)} value.
     *
     * @param exactlyLen can be a null for unset.
     * @return The current actual {@link FieldBuilder}.
     */
    public FieldBuilder defineExactlyLen(Integer exactlyLen) {
        msgField.setExactlyLength(exactlyLen);
        return this;
    }

    public FieldBuilder jumpToSibling(String siblingName) {
        this.msgField = NavigatorService.getSiblingOrThrowException(siblingName, msgField);
        return this;
    }

    /**
     * Find the root field and set it to the context of this {@link FieldBuilder}.
     * @return The current actual {@link FieldBuilder}.
     */
    public FieldBuilder jumpToRoot() {
        msgField = NavigatorService.findRoot(msgField);
        return this;
    }

    /**
     * For debugging purposes only.
     * @return The {@link DumpService#dumpMsgField(MsgField)} method result.
     */
    public FieldBuilder dump() {
        if (logger.isInfoEnabled()) {
            logger.info(DumpService.dumpMsgField(msgField));
        }
        return this;
    }

    /**
     * Set the {@link HeaderField#setBitMapPacker(BitmapPacker)} subfield of the current {@link #msgField}.
     * @param bitMapPacker the instance to set.
     *                       
     * @return The current actual {@link FieldBuilder}.
     */
    public FieldBuilder defineHeaderBitMapPacker(BitmapPacker bitMapPacker) {
        msgField.getHeaderField().setBitMapPacker(bitMapPacker);
        return this;
    }

    /**
     * Create a new empty child ot this {@link #msgField}.
     * 
     * @param msgFieldType mandatory type
     * @return The current actual {@link FieldBuilder} with the new child as its context {@link #msgField} value.
     */
    public FieldBuilder createChild(MsgFieldType msgFieldType) {
        MsgField child = new MsgField();
        child.setType(msgFieldType);
        child.setParent(msgField);
        List<MsgField> children = msgField.getChildren();
        if (children == null) {
            children = new ArrayList<>();
            msgField.setChildren(children);
        }
        children.add(child);
        msgField = child;
        return this;
    }

    /**
     * Call the {@link #clone(MsgField)} method and set its result to the context {@link #msgField}.
     * 
     * The other way is using of the {@link FieldBuilder#clone(MsgField)} method, but in this case you have to set
     * the {@link FieldBuilder#defineParent(MsgField)} property for the cloned subfield.
     * 
     * @return The current actual {@link FieldBuilder} with the new {@link #msgField} value.
     */
    public FieldBuilder cloneToSibling() {
        MsgField cloned = clone(msgField).getCurrentField();
        msgField.getParent().getChildren().add(cloned);
        cloned.setParent(msgField.getParent());
        msgField = cloned;
        return this;
    }

    /**
     * Set the {@link MsgField#setChildrenLengthPacker(LengthPacker)} value to the current {@link #msgField}.
     * @param lengthPacker packs and unpacks the {@link #msgField}'s children {@link LengthPacker}.
     *                     
     * @return The current actual {@link FieldBuilder} with the {@link #msgField} in its context.
     */
    public FieldBuilder defineChildrenLengthPacker(LengthPacker lengthPacker) {
        if (lengthPacker == null) {
            msgField.setChildrenLengthPacker(null);
            return this;
        }
        if (msgField.getChildren() != null) {
            for (MsgField child : msgField.getChildren()) {
                if (child.getChildrenLengthPacker() != null) {
                    throw new PackerRuntimeException(createMessageSameLengthPacker(msgField, child));
                }
            }
        }
        msgField.setChildrenLengthPacker(lengthPacker);
        return this;
    }

    /**
     * Set the {@link MsgField#setType(MsgFieldType)} value to the current {@link #msgField} in the builder context.
     * @param msgFieldType the value to be set.
     * @return The current actual {@link FieldBuilder} with the {@link #msgField} in its context.
     */
    public FieldBuilder defineType(MsgFieldType msgFieldType) {
        msgField.setType(msgFieldType);
        return this;
    }

    /**
     * Set the {@link MsgField#setMasker(Masker)} value to the current {@link #msgField} in the builder context.
     * @param masker the value to be set.
     * @return The current actual {@link FieldBuilder} with the {@link #msgField} in its context.
     */
    public FieldBuilder defineMasker(Masker masker) {
        msgField.setMasker(masker);
        return this;
    }

    /**
     * Set the {@link MsgField#setStringer(Stringer)} value to the current {@link #msgField} in the builder context.
     * @param stringer the value to be set.
     * @return The current actual {@link FieldBuilder} with the {@link #msgField} in its context.
     */
    public FieldBuilder defineStringer(Stringer stringer) {
        msgField.setStringer(stringer);
        return this;
    }
}
