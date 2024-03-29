package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.masking.Masker;
import com.credibledoc.iso8583packer.message.Msg;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.navigator.Navigator;
import com.credibledoc.iso8583packer.navigator.NavigatorService;
import com.credibledoc.iso8583packer.stringer.Stringer;
import com.credibledoc.iso8583packer.tag.TagPacker;
import com.credibledoc.iso8583packer.validator.Validator;
import com.credibledoc.iso8583packer.validator.ValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The class represents a Builder of ISO message structure. Instances of the builder contain the {@link #msgField} state.
 * <p>
 * The instance of the builder has a single {@link #msgField} in its context. The {@link #msgField}
 * contains information about the objects graph.
 * <p>
 * The builder provides methods for creation of {@link MsgField}, see the {@link #builder(MsgFieldType)} method and
 * for handling existing {@link MsgField}, see the {@link #from(MsgField)} method.
 * <p>
 * The builder helps to set {@link MsgField}'s properties, see for example the {@link #defineName(String)} or
 * {@link #defineParent(MsgField)} methods.
 * <p>
 * Example of creation
 * <pre>
 *     FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG).defineName("msg");
 * </pre>
 *
 * @author Kyrylo Semenko
 */
public class FieldBuilder {
    private static final Logger logger = LoggerFactory.getLogger(FieldBuilder.class);

    /**
     * The context and state of the builder instance.
     */
    protected MsgField msgField;
    
    protected Validator validator;
    
    protected Navigator navigator;
    
    protected Visualizer visualizer;

    /**
     * If 'true' and fieldNums are defined, the fields will be sorted in their parent
     * by the {@link MsgField#getFieldNum()} natural order.
     * <p>
     * Else the fields will be sorted in the order of their insertion in the list.
     * <p>
     * Default value is 'true'.
     */
    protected boolean sortByFieldNum = true;

    /**
     * Create a new {@link FieldBuilder} with a new empty {@link #msgField}.
     * 
     * Example of the root field creation.
     * <pre>
     *     FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG).defineName("msg");
     * </pre>
     * 
     * Example of the first level field creation.
     * <pre>
     *     MsgField isoMsgField = fieldBuilder.getCurrentField();
     *
     *     MsgField mti = FieldBuilder.builder(MsgFieldType.VAL)
     *         .defineName(MTI_NAME)
     *         .defineBodyPacker(BcdBodyPacker.rightPaddingF())
     *         .defineLen(2)
     *         .defineParent(isoMsgField)
     *         .getCurrentField();
     * </pre>
     * @param msgFieldType mandatory type, see the {@link MsgFieldType} description
     * @return The new instance of the {@link FieldBuilder}.
     */
    public static FieldBuilder builder(MsgFieldType msgFieldType) {
        FieldBuilder fieldBuilder = new FieldBuilder();
        fieldBuilder.msgField = new MsgField();
        fieldBuilder.msgField.setRoot(fieldBuilder.msgField); // root references to itself
        fieldBuilder.msgField.setType(msgFieldType);
        
        // Technical domain
        fieldBuilder.createDefaultServices();
        
        return fieldBuilder;
    }

    /**
     * Call the {@link #builder(MsgFieldType)} method and set the {@link #sortByFieldNum} flag.
     * @param msgFieldType see the {@link #builder(MsgFieldType)} method description
     * @param sortByFieldNum see the {@link #sortByFieldNum} description
     * @return A new instance of the {@link FieldBuilder}.
     */
    public static FieldBuilder builder(MsgFieldType msgFieldType, boolean sortByFieldNum) {
        FieldBuilder fieldBuilder = builder(msgFieldType);
        fieldBuilder.setSortByFieldNum(sortByFieldNum);
        return fieldBuilder;
    }

    /**
     * Create instances of services used in the builder. The method may be overridden if needed.
     */
    protected void createDefaultServices() {
        validator = new ValidatorService();
        navigator = NavigatorService.getInstance();
        visualizer = DumpService.getInstance();
        validator.setNavigator(navigator);
        validator.setVisualizer(visualizer);
    }
    
    /**
     * Copy the field from the argument and create a new {@link FieldBuilder} instance with the new field in its 
     * context.
     * <p>
     * Set all properties from the argument 'example' to the created field except
     * {@link MsgField#setName(String)}, {@link MsgField#setFieldNum(Integer)}, {@link MsgField#setParent(MsgField)}
     * and {@link MsgField#setChildren(List)}.
     * <p>
     *
     * @param example where some properties are copied from.
     * @return this instance of {@link FieldBuilder}.
     */
    public static FieldBuilder clone(MsgField example) {
        FieldBuilder fieldBuilder = new FieldBuilder();
        fieldBuilder.createDefaultServices();
        MsgField msgField = fieldBuilder.cloneField(example);
        fieldBuilder.setMsgField(msgField);
        return fieldBuilder;
    }

    /**
     * Call the {@link #clone(MsgField)} method and set the {@link #sortByFieldNum} value.
     * @param example see the {@link #clone(MsgField)} method description
     * @param sortByFieldNum see the {@link #sortByFieldNum} field description
     * @return this instance of {@link FieldBuilder}.
     */
    public static FieldBuilder clone(MsgField example, boolean sortByFieldNum) {
        FieldBuilder fieldBuilder = clone(example);
        fieldBuilder.setSortByFieldNum(sortByFieldNum);
        return fieldBuilder;
    }

    /**
     * Copy the field from the argument and create a new {@link FieldBuilder} instance with the new field in its 
     * context.
     * <p>
     * Set all properties from the argument 'example' to the created field except
     * {@link MsgField#setName(String)}, {@link MsgField#setFieldNum(Integer)}, {@link MsgField#setParent(MsgField)}
     * and {@link MsgField#setChildren(List)}.
     * <p>
     *
     * @param example where some properties are copied from.
     * @return The new instance of {@link FieldBuilder}.
     */
    protected MsgField cloneField(MsgField example) {
        MsgField newMsgField = new MsgField();
        newMsgField.setRoot(example.getRoot());
        newMsgField.setType(example.getType());
        newMsgField.setChildrenLengthPacker(example.getChildrenLengthPacker());
        newMsgField.setChildrenBodyPacker(example.getChildrenBodyPacker());
        newMsgField.setBodyPacker(example.getBodyPacker());
        newMsgField.setTagPacker(example.getTagPacker());
        newMsgField.setChildrenTagPacker(example.getChildrenTagPacker());
        newMsgField.setExactlyLength(example.getExactlyLength());
        newMsgField.setLen(example.getLen());
        newMsgField.setMaxLen(example.getMaxLen());
        newMsgField.setMasker(example.getMasker());
        newMsgField.setStringer(example.getStringer());
        newMsgField.setLengthPacker(example.getLengthPacker());
        newMsgField.setBitMapPacker(example.getBitMapPacker());

        return newMsgField;
    }

    /**
     * Check restrictions of the Field structure and all its children recursively.
     *
     * @param current the {@link MsgField} to be checked.
     */
    public static void validateStructure(MsgField current) {
        FieldBuilder fieldBuilder = new FieldBuilder();
        fieldBuilder.msgField = current;
        fieldBuilder.createDefaultServices();
        fieldBuilder.validateStructure();
    }

    /**
     * Call the {@link Validator#validateStructure(MsgField)} method
     * with the current {@link #msgField} as argument.
     * 
     * @return The current instance of {@link FieldBuilder} with {@link #msgField} in its context.
     */
    public FieldBuilder validateStructure() {
        try {
            MsgField currentMsgField = msgField;
            jumpToRoot();
            validator.validateStructure(msgField);
            msgField = currentMsgField;
            return this;
        } catch (Exception e) {
            MsgField root = navigator.findRoot(msgField);
            throw new PackerRuntimeException("Validation failed, message:\n" + e.getMessage() + "\n" +
                "Root MsgField:\n" + visualizer.dumpMsgField(root), e);
        }
    }

    /**
     * Get the field from the argument and instantiate a new {@link FieldBuilder} with this field in its context.
     *
     * @param msgField will be set to the {@link FieldBuilder#msgField} field.
     * @return Created instance of the {@link FieldBuilder}.
     */
    public static FieldBuilder from(MsgField msgField) {
        FieldBuilder fieldBuilder = new FieldBuilder();
        fieldBuilder.msgField = msgField;

        fieldBuilder.createDefaultServices();
        
        return fieldBuilder;
    }

    /**
     * Call the {@link #from(MsgField)} method and set the {@link #sortByFieldNum} value.
     *
     * @param msgField see the {@link #from(MsgField)} method description.
     * @param sortByFieldNum see the {@link #sortByFieldNum} field description.
     * @return Created instance of the {@link FieldBuilder}.
     */
    public static FieldBuilder from(MsgField msgField, boolean sortByFieldNum) {
        FieldBuilder fieldBuilder = from(msgField);
        fieldBuilder.setSortByFieldNum(sortByFieldNum);
        return fieldBuilder;
    }

    /**
     * Set the {@link MsgField#setFieldNum(Integer)} value.
     * @param fieldNum see the {@link Msg#getFieldNum()} method description. May be 'null' for deactivation.
     *                 
     * @return The current instance of {@link FieldBuilder} with {@link #msgField} in its context.
     */
    public FieldBuilder defineFieldNum(Integer fieldNum) {
        if (fieldNum != null && msgField.getParent() == null || msgField.getParent().getType() != MsgFieldType.BIT_SET) {
            String path = navigator.getPathRecursively(msgField);
            String fieldDump = "\nActual structure:\n" + visualizer.dumpMsgField(navigator.findRoot(msgField));
            throw new PackerRuntimeException("The 'fieldNum' property is allowed for children of '" +
                MsgFieldType.class.getSimpleName() + "." + MsgFieldType.BIT_SET + "' only. " +
                "Field num: '" + fieldNum + "'. " +
                "Incorrect MsgField path: '" + path + "'." + fieldDump);
        }
        this.msgField.setFieldNum(fieldNum);
        if (isSortByFieldNum()) {
            MsgField parent = this.msgField.getParent();
            if (parent != null) {
                parent.getChildren().sort(
                    Comparator.comparing(MsgField::getFieldNum, Comparator.nullsLast(Comparator.naturalOrder()))
                );
            }
        }
        return this;
    }

    /**
     * Set the {@link MsgField#setTag(Object)} value.
     * @param tag see the {@link Msg#getFieldNum()} method description. May be 'null' for deactivation.
     *                 
     * @return The current instance of {@link FieldBuilder} with {@link #msgField} in its context.
     */
    public FieldBuilder defineHeaderTag(Object tag) {
        if (tag != null && MsgFieldType.isNotTaggedType(msgField)) {
            String path = navigator.getPathRecursively(msgField);
            throw new PackerRuntimeException("The 'tag' property is allowed for '" +
                MsgFieldType.getTaggedTypes() + "' MsgFieldTypes only. Current MsgField path: '" + path + "'.");
        }
        this.msgField.setTag(tag);
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

    /**
     * Set the {@link MsgField#setMaxLen(Integer)} value.
     *
     * @param maxLength Defines the maximum size of a <b>value</b> packed to bytes.
     *                  If the <b>value</b> packed to bytes is greater than maxLen, then an exception will be thrown 
     *                  during the packing.
     * @return The current instance of {@link FieldBuilder} with {@link #msgField} in its context.
     */
    public FieldBuilder defineMaxLen(int maxLength) {
        this.msgField.setMaxLen(maxLength);
        return this;
    }

    /**
     * Set the {@link MsgField#setBodyPacker(BodyPacker)} value to the current {@link #msgField}.
     * @param bodyPacker to be set or 'null' for deactivation.
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
     * Set the {@link MsgField#setLen(Integer)} value. The value can be set for fields with fixed length only.
     *
     * @param fieldLen can be 'null' for unsetting.
     * @return The current actual {@link FieldBuilder}
     */
    public FieldBuilder defineLen(Integer fieldLen) {
        this.msgField.setLen(fieldLen);
        return this;
    }

    /**
     * Set the {@link MsgField#setChildrenBodyLen(Integer)} value. The value can be set for fields with fixed length only.
     *
     * @param bodyLen can be 'null' for unsetting.
     * @return The current actual {@link FieldBuilder}
     */
    public FieldBuilder defineChildrenBodyLen(Integer bodyLen) {
        this.msgField.setChildrenBodyLen(bodyLen);
        return this;
    }

    public FieldBuilder defineParent(MsgField parentMsgField) {
        try {
            if (parentMsgField == msgField.getParent()) {
                logger.info("Parent field '{}' already set to the field '{}'. Skip the defineParent() method.",
                    navigator.getPathRecursively(parentMsgField),
                    navigator.getPathRecursively(msgField));
                return this;
            }
            if (parentMsgField.getChildrenLengthPacker() != null && msgField.getLengthPacker() != null) {
                throw new PackerRuntimeException(createMessageSameLengthPacker(parentMsgField, msgField));
            }
            this.msgField.setParent(parentMsgField);
            this.msgField.setDepth(parentMsgField.getDepth() + 1);
            this.msgField.setRoot(parentMsgField.getRoot());
            List<MsgField> msgFields = parentMsgField.getChildren();
            if (msgFields == null) {
                msgFields = new ArrayList<>();
                parentMsgField.setChildren(msgFields);
            }
            msgFields.add(msgField);
            return this;
        } catch (Exception e) {
            MsgField root = navigator.findRoot(msgField);
            String path = navigator.getPathRecursively(msgField);
            throw new PackerRuntimeException("Exception in defineParent(parentMsgField) method, " +
                "current msgField: '" + path + "', " +
                "message:\n" + e.getMessage() + "\n" +
                "Root MsgField:\n" + visualizer.dumpMsgField(root), e);
        }
    }

    protected String createMessageSameLengthPacker(MsgField parent, MsgField child) {
        return "The *lengthPacker* value cannot be set to the both, the " +
                "parent and child. Only one of them should have the value. " +
                "Parent: " + navigator.getPathRecursively(parent) + ", " +
                "child: " + navigator.getPathRecursively(child) + ". \nThe value should be set " +
                "to the parent in cases when the length subfield precedes the tag subfield. \nThe " +
                "value should be set to the child when the tag subfield precedes the length subfield.";
    }

    /**
     * Set the {@link MsgField#setTagPacker(TagPacker)} value.
     * @param tagPacker to be set.
     * @return The current instance of the {@link FieldBuilder} with a {@link #msgField} in its context.
     */
    public FieldBuilder defineHeaderTagPacker(TagPacker tagPacker) {
        msgField.setTagPacker(tagPacker);
        return this;
    }

    /**
     * Set the {@link MsgField#setChildrenTagPacker(TagPacker)} value.
     * @param tagPacker to be set.
     * @return The current instance of the {@link FieldBuilder} with a {@link #msgField} in its context.
     */
    public FieldBuilder defineChildrenTagPacker(TagPacker tagPacker) {
        msgField.setChildrenTagPacker(tagPacker);
        return this;
    }

    /**
     * Find the <b>first</b> child of the current context {@link MsgField} with defined name.
     *
     * @param childName the {@link MsgField#getName()} property for searching.
     * @return The found child. Throw an exception if this context does not have children or child not found.
     */
    public FieldBuilder jumpToChild(String childName) {
        msgField = navigator.getChildOrThrowException(childName, msgField);
        return this;
    }

    /**
     * Set the {@link MsgField#setLengthPacker(LengthPacker)} property.
     * 
     * Examples of {@link LengthPacker} see {@link com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker}
     * or {@link com.credibledoc.iso8583packer.bcd.BcdLengthPacker}.
     * 
     * @param lengthPacker can be 'null' for deactivation.
     * @return The current instance of the {@link FieldBuilder} with a {@link #msgField} in its context.
     */
    public FieldBuilder defineHeaderLengthPacker(LengthPacker lengthPacker) {
        msgField.setLengthPacker(lengthPacker);
        return this;
    }

    public FieldBuilder jumpToParent() {
        if (msgField.getParent() == null) {
            throw new PackerRuntimeException("The field '" + navigator.getPathRecursively(msgField) + "' has no parent.");
        }
        msgField = msgField.getParent();
        return this;
    }

    /**
     * Set the {@link MsgField#setExactlyLength(Integer)} value.
     *
     * @param exactlyLen can be a null for deactivation.
     * @return The current actual {@link FieldBuilder}.
     */
    public FieldBuilder defineExactlyLen(Integer exactlyLen) {
        msgField.setExactlyLength(exactlyLen);
        return this;
    }

    public FieldBuilder jumpToSibling(String siblingName) {
        this.msgField = navigator.getSiblingOrThrowException(siblingName, msgField);
        return this;
    }

    /**
     * Find the root field and set it to the context of this {@link FieldBuilder}.
     * @return The current actual {@link FieldBuilder}.
     */
    public FieldBuilder jumpToRoot() {
        msgField = navigator.findRoot(msgField);
        return this;
    }

    /**
     * For debugging purposes only.
     * @return The {@link DumpService#dumpMsgField(MsgField)} method result.
     */
    public FieldBuilder dump() {
        if (logger.isInfoEnabled()) {
            logger.info(visualizer.dumpMsgField(msgField));
        }
        return this;
    }

    /**
     * Set the {@link MsgField#setBitMapPacker(BitmapPacker)} subfield of the current {@link #msgField}.
     * @param bitMapPacker the instance to set.
     *                       
     * @return The current actual {@link FieldBuilder}.
     */
    public FieldBuilder defineHeaderBitmapPacker(BitmapPacker bitMapPacker) {
        msgField.setBitMapPacker(bitMapPacker);
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
        child.setRoot(msgField.getRoot());
        child.setType(msgFieldType);
        child.setParent(msgField);
        child.setDepth(msgField.getDepth() + 1);
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
     * Call the {@link #cloneField(MsgField)} method and set its result to the context {@link #msgField}.
     * 
     * An other way is using the {@link FieldBuilder#cloneField(MsgField)} method, but in the case you have to set
     * the {@link FieldBuilder#defineParent(MsgField)} property for the cloned subfield.
     * 
     * @return The current actual {@link FieldBuilder} with the new {@link #msgField} value.
     */
    public FieldBuilder cloneToSibling() {
        MsgField cloned = cloneField(msgField);
        MsgField parent = msgField.getParent();
        if (parent == null) {
            throw new PackerRuntimeException("Cannot define sibling to field without a parent. Current field: '" +
                navigator.getPathRecursively(msgField) + "'.");
        }
        parent.getChildren().add(cloned);
        cloned.setParent(parent);
        cloned.setDepth(parent.getDepth() + 1);
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
     * Set the {@link MsgField#setChildrenBodyPacker(BodyPacker)} value to the current {@link #msgField}.
     * @param bodyPacker packs and unpacks the children's {@link MsgValue#getBodyBytes()}. It may be used in
     *                   unknown (undefined) {@link MsgFieldType#TAG_LEN_VAL}, {@link MsgFieldType#TAG_LEN_VAL} and
     *                   {@link MsgFieldType#TAG_VAL} fields during unpacking.
     *                     
     * @return The current actual {@link FieldBuilder} with the {@link #msgField} in its context.
     */
    public FieldBuilder defineChildrenBodyPacker(BodyPacker bodyPacker) {
        if (bodyPacker == null) {
            msgField.setChildrenBodyPacker(null);
            return this;
        }
        msgField.setChildrenBodyPacker(bodyPacker);
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

    /**
     * Create new {@link MsgField} with type from the argument.
     * @param msgFieldType the new {@link MsgField} type.
     * @return The current actual {@link FieldBuilder} with the created {@link #msgField} in its context.
     */
    public FieldBuilder createSibling(MsgFieldType msgFieldType) {
        MsgField newMsgField = new MsgField();
        newMsgField.setType(msgFieldType);
        newMsgField.setRoot(msgField.getRoot());
        return createParentIfNotExists(newMsgField);
    }

    /**
     * Create new {@link MsgField} with the same type as the current {@link #msgField}.
     * @return The current actual {@link FieldBuilder} with the created {@link #msgField} in its context.
     */
    public FieldBuilder createSibling() {
        MsgField newMsgField = new MsgField();
        newMsgField.setType(msgField.getType());
        newMsgField.setRoot(msgField.getRoot());
        return createParentIfNotExists(newMsgField);
    }

    protected FieldBuilder createParentIfNotExists(MsgField newMsgField) {
        if (msgField.getParent() == null) {
            MsgField parent = new MsgField();
            parent.setRoot(parent);
            parent.setType(MsgFieldType.MSG);
            parent.setName("Root");
            msgField.setParent(parent);
            msgField.setDepth(parent.getDepth() + 1);
            List<MsgField> children = new ArrayList<>();
            children.add(msgField);
            parent.setChildren(children);
        }
        newMsgField.setParent(msgField.getParent());
        newMsgField.setDepth(msgField.getParent().getDepth() + 1);
        msgField = newMsgField;
        msgField.getParent().getChildren().add(newMsgField);
        msgField.setRoot(msgField.getParent().getRoot());
        return this;
    }

    /**
     * @param validator see the {@link #validator} field description.
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * @param msgField see the {@link #msgField} field description.
     */
    public void setMsgField(MsgField msgField) {
        this.msgField = msgField;
    }

    /**
     * @return The {@link #sortByFieldNum} field value.
     */
    public boolean isSortByFieldNum() {
        return sortByFieldNum;
    }

    /**
     * @param sortByFieldNum see the {@link #sortByFieldNum} field description.
     */
    public void setSortByFieldNum(boolean sortByFieldNum) {
        this.sortByFieldNum = sortByFieldNum;
    }
}
