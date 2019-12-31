package com.credibledoc.iso8583packer.message;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.header.HeaderField;
import com.credibledoc.iso8583packer.header.HeaderValue;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.masking.Masker;
import com.credibledoc.iso8583packer.stringer.StringStringer;
import com.credibledoc.iso8583packer.stringer.Stringer;
import com.credibledoc.iso8583packer.tag.TagPacker;

import java.util.List;

/**
 * The data object describes a data structure.
 * Instances of the {@link MsgField}s are created with {@link FieldBuilder}.
 * <p>
 * The {@link FieldBuilder} class contains two main methods,
 * {@link ValueHolder#pack()} and {@link ValueHolder#unpack(byte[], int, MsgField)}. These methods
 * uses the {@link MsgField} for packing or unpacking {@link MsgValue}s.
 * <p>
 * See {@link HeaderField} description for better understanding of the header properties.
 * <p>
 * In the unpacked state the {@link MsgField} contains some value, see the {@link MsgValue#getBodyValue()} method.
 *
 * @author Kyrylo Semenko
 */
public class MsgField implements Msg {

    /**
     * The number of this field in the {@link #parent} field.
     * The parent field lists the {@link #children}.
     * Can be 'null' in some cases.
     * Cannot be 'null' if the parent container contains the {@link HeaderValue#getBitSet()} subfield.
     */
    private Integer tagNum;

    /**
     * Some fields has no name, others have. This name is used for creation of a dump and for navigation in the graph.
     */
    private String name;

    /**
     * Parent is a node {@link MsgField}. It contains this child in the {@link MsgField#getChildren()} value.
     * <p>
     * Root field has no parent node.
     */
    private MsgField parent;

    /**
     * Sub fields of this node or ISOMsg. If this {@link MsgField} is a leaf, this value is 'null'.
     */
    private List<MsgField> children;

    /**
     * Packs {@link MsgValue#getBodyValue()}s to {@link MsgValue#getBodyBytes()} and wise versa. See the {@link BodyPacker}
     * and its methods description.
     */
    private BodyPacker bodyPacker;
    
    /**
     * Not cloned to other fields. Defines the packed bytes maximum size.
     */
    private Integer maxLen;

    /**
     * Number of bytes of the field in the packed state.
     * <p>
     * It uses for fixed length fields only.
     */
    private Integer len;

    /**
     * Length of bytes in children names. For example this <b>DFEE01010D</b> field has 3 bytes long tag DFEE01.
     * 
     * Contains 'null' in case of no children.
     */
    private Integer childTagLength;

    /**
     * Contains additional properties ot the {@link MsgField} with definition of its header subfields.
     */
    private HeaderField headerField = new HeaderField();

    /**
     * Packs and unpacks the children ({@link #children}) {@link #tagNum} subfields .
     */
    private TagPacker childrenTagPacker;

    /**
     * Packs and unpacks length of children fields. This value should be defined when the <i>length</i> sub-field
     * precedes the <i>tagNum</i> sub-field,
     * for example F0F0F3 F9F3 F0, where F0F0F3 is the length 003, F9F3 is the tagNum 93 and F0 is the body.
     * <p>
     * Only one {@link LengthPacker} can be defined, parent childrenLengthPacker or its child length packer. Else the
     * exception will be thrown.
     */
    private LengthPacker childrenLengthPacker;

    /**
     * If this field exists, the {@link MsgValue#getBodyBytes()} value must be exactly n bytes long.
     * Used for validation purposes only.
     */
    private Integer exactlyLength;

    /**
     * The {@link MsgFieldType} defines Tag, Length and Value sub-fields presents and order.
     */
    private MsgFieldType type;

    /**
     * Masker hides private sensitive data before logging.
     */
    private Masker masker;

    /**
     * Stringer converts the {@link MsgValue#getBodyValue()} to {@link String} for logging purposes. 
     */
    private Stringer stringer;
    
    public MsgField() {
        this.stringer = StringStringer.INSTANCE;
    }

    @Override
    public String toString() {
        String bodyPackerClass = bodyPacker == null ? "null" : bodyPacker.getClass().getSimpleName();
        String parentString = parent == null ? "null" : parent.getName();
        String childrenSizeString = children == null ? "0" : Integer.toString(children.size());
        String childrenTagPackerString = childrenTagPacker == null ? "null" : childrenTagPacker.getClass().getSimpleName();
        String maskerString = masker == null ? "null" : masker.getClass().getSimpleName();
        String stringerString = stringer == null ? "null" : stringer.getClass().getSimpleName();
        return "Field{" +
                "tagNum=" + tagNum +
                ", name=" + name +
                ", type=" + type +
                ", parent=" + parentString +
                ", childrenSize=" + childrenSizeString +
                ", bodyPacker=" + bodyPackerClass +
                ", masker=" + maskerString +
                ", stringer=" + stringerString +
                ", maxLen=" + maxLen +
                ", len=" + len +
                ", header=" + headerField +
                ", childrenTagPacker=" + childrenTagPackerString +
                ", exactlyLength=" + exactlyLength +
                '}';
    }

    /**
     * @return The {@link #tagNum} field value.
     */
    @Override
    public Integer getTagNum() {
        return tagNum;
    }

    /**
     * @param tagNum see the {@link #tagNum} field description.
     */
    public void setTagNum(Integer tagNum) {
        this.tagNum = tagNum;
    }

    /**
     * @return The {@link #name} field value.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name see the {@link #name} field description.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The {@link #parent} field value.
     */
    @Override
    public MsgField getParent() {
        return parent;
    }

    /**
     * @param parent see the {@link #parent} field description.
     */
    public void setParent(MsgField parent) {
        this.parent = parent;
    }

    /**
     * @return The {@link #children} field value.
     */
    @Override
    public List<MsgField> getChildren() {
        return children;
    }

    /**
     * @param children see the {@link #children} field description.
     */
    public void setChildren(List<MsgField> children) {
        this.children = children;
    }

    /**
     * @return The {@link #maxLen} field value.
     */
    public Integer getMaxLen() {
        return maxLen;
    }

    /**
     * @param maxLen see the {@link #maxLen} field description.
     */
    public void setMaxLen(Integer maxLen) {
        this.maxLen = maxLen;
    }

    /**
     * @return The {@link #len} field value.
     */
    public Integer getLen() {
        return len;
    }

    /**
     * @param len see the {@link #len} field description.
     */
    public void setLen(Integer len) {
        this.len = len;
    }

    /**
     * @return The {@link #bodyPacker} field value.
     */
    public BodyPacker getBodyPacker() {
        return bodyPacker;
    }

    /**
     * @param bodyPacker see the {@link #bodyPacker} field description.
     */
    public void setBodyPacker(BodyPacker bodyPacker) {
        this.bodyPacker = bodyPacker;
    }

    /**
     * @return The {@link #childTagLength} field value.
     */
    public Integer getChildTagLength() {
        return childTagLength;
    }

    /**
     * @param childTagLength see the {@link #childTagLength} field description.
     */
    public void setChildTagLength(Integer childTagLength) {
        this.childTagLength = childTagLength;
    }

    /**
     * @return The {@link #headerField} field value.
     */
    public HeaderField getHeaderField() {
        return headerField;
    }

    /**
     * @param headerField see the {@link #headerField} field description.
     */
    public void setHeaderField(HeaderField headerField) {
        this.headerField = headerField;
    }

    /**
     * @return The {@link #childrenTagPacker} field value.
     */
    public TagPacker getChildrenTagPacker() {
        return childrenTagPacker;
    }

    /**
     * @param childrenTagPacker see the {@link #childrenTagPacker} field description.
     */
    public void setChildrenTagPacker(TagPacker childrenTagPacker) {
        this.childrenTagPacker = childrenTagPacker;
    }

    /**
     * @return The {@link #childrenLengthPacker} field value.
     */
    public LengthPacker getChildrenLengthPacker() {
        return childrenLengthPacker;
    }

    /**
     * @param childrenLengthPacker see the {@link #childrenLengthPacker} field description.
     */
    public void setChildrenLengthPacker(LengthPacker childrenLengthPacker) {
        this.childrenLengthPacker = childrenLengthPacker;
    }

    /**
     * @return The {@link #exactlyLength} field value.
     */
    public Integer getExactlyLength() {
        return exactlyLength;
    }

    /**
     * @param exactlyLength see the {@link #exactlyLength} field description.
     */
    public void setExactlyLength(Integer exactlyLength) {
        this.exactlyLength = exactlyLength;
    }

    /**
     * @return The {@link #type} field value.
     */
    public MsgFieldType getType() {
        return type;
    }

    /**
     * @param type see the {@link #type} field description.
     */
    public void setType(MsgFieldType type) {
        this.type = type;
    }

    /**
     * @return The {@link #masker} field value.
     */
    public Masker getMasker() {
        return masker;
    }

    /**
     * @param masker see the {@link #masker} field description.
     */
    public void setMasker(Masker masker) {
        this.masker = masker;
    }

    /**
     * @return The {@link #stringer} field value.
     */
    public Stringer getStringer() {
        return stringer;
    }

    /**
     * @param stringer see the {@link #stringer} field description.
     */
    public void setStringer(Stringer stringer) {
        this.stringer = stringer;
    }
}
