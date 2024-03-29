package com.credibledoc.iso8583packer.message;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.body.BodyPacker;
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
 * In the unpacked state the {@link MsgField} contains some value, see the {@link MsgValue#getBodyValue()} method.
 *
 * @author Kyrylo Semenko
 */
public class MsgField implements Msg {

    /**
     * See the {@link Msg#getFieldNum()} description.
     */
    private Integer fieldNum;

    /**
     * See the {@link Msg#getTag()} description.
     */
    private Object tag;

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
     * Not cloned to other fields. Defines the maximum size of a <b>value</b> packed to bytes.
     * If the <b>value</b> packed to bytes is greater than
     * maxLen, then an exception will be thrown during the packing.
     */
    private Integer maxLen;

    /**
     * Number of bytes the field's body contains in its packed state.
     * <p>
     * It used for fixed length fields only.
     */
    private Integer len;

    /**
     * Number of bytes the field's children have in bodies in packed state. If some child has defined {@link #len} and
     * childrenBodyLen, the {@link #len} wins and childrenBodyLen is ignored.
     * <p>
     * It used for fixed length children fields only.
     */
    private Integer childrenBodyLen;

    /**
     * Packs and unpacks the {@link #tag} subfield. Only one tagPacker or {@link #childrenLengthPacker} is allowed.
     */
    private TagPacker tagPacker;

    /**
     * Packs and unpacks the ({@link #children})'s {@link #tag} subfields.
     * Only one {@link #tagPacker} or childrenTagPacker is allowed.
     */
    private TagPacker childrenTagPacker;

    /**
     * Packs and unpacks length of ({@link #children})'s fields. This value should be defined when the <i>length</i> sub-field
     * precedes the <i>tag</i> sub-field,
     * for example F0F0F3 F9F3 F0, where F0F0F3 is the length 003, F9F3 is the tag 93 and F0 is the body.
     * <p>
     * Only one {@link LengthPacker} can be defined, parent childrenLengthPacker or its child length packer. Else the
     * exception will be thrown.
     */
    private LengthPacker childrenLengthPacker;

    /**
     * Packs and unpacks {@link MsgValue#getBodyBytes()} of ({@link #children})'s fields. The packer may be set in a field
     * or in its parent. Packer in a field has precedence over a packer in a parent.
     */
    private BodyPacker childrenBodyPacker;

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

    /**
     * Packs from int to bytes and wise versa the {@link MsgValue#getLengthBytes()} subfield.
     * <p>
     * The calculated value says how many bytes contains the {@link MsgValue#getBodyBytes()} subfield.
     */
    private LengthPacker lengthPacker;

    /**
     * Packs and unpacks bytes of the {@link MsgValue#getBitSet()} subfield.
     */
    private BitmapPacker bitMapPacker;

    /**
     * How far from the root is the field. Root has 0 depth.
     */
    private volatile int depth;

    /**
     * See the {@link Msg#getRoot()} method description.
     */
    private MsgField root;
    
    public MsgField() {
        this.stringer = StringStringer.getInstance();
    }

    @Override
    public String toString() {
        String bodyPackerClass = bodyPacker == null ? "null" : bodyPacker.getClass().getSimpleName();
        String parentString = parent == null ? "null" : parent.getName();
        String childrenSizeString = children == null ? "0" : Integer.toString(children.size());
        String tagPackerString = tagPacker == null ? "null" : tagPacker.getClass().getSimpleName();
        String childrenTagPackerString = childrenTagPacker == null ? "null" : childrenTagPacker.getClass().getSimpleName();
        String maskerString = masker == null ? "null" : masker.getClass().getSimpleName();
        String stringerString = stringer == null ? "null" : stringer.getClass().getSimpleName();
        String lengthPackerString = lengthPacker == null ? "null" : lengthPacker.getClass().getSimpleName();
        String bitMapPackerString = bitMapPacker == null ? "null" : bitMapPacker.getClass().getSimpleName();
        return "Field{" +
                "fieldNum=" + fieldNum +
                ", tag=" + tag +
                ", name=" + name +
                ", type=" + type +
                ", parent=" + parentString +
                ", childrenSize=" + childrenSizeString +
                ", bodyPacker=" + bodyPackerClass +
                ", masker=" + maskerString +
                ", stringer=" + stringerString +
                ", maxLen=" + maxLen +
                ", len=" + len +
                ", childrenBodyLen=" + childrenBodyLen +
                ", tagPacker=" + tagPackerString +
                ", childrenTagPacker=" + childrenTagPackerString +
                ", exactlyLength=" + exactlyLength +
                ", lengthPacker=" + lengthPackerString +
                ", bitMapPacker=" + bitMapPackerString +
                '}';
    }

    /**
     * @return The {@link #fieldNum} field value.
     */
    @Override
    public Integer getFieldNum() {
        return fieldNum;
    }

    /**
     * @param fieldNum see the {@link #fieldNum} field description.
     */
    public void setFieldNum(Integer fieldNum) {
        this.fieldNum = fieldNum;
    }

    /**
     * @return The {@link #tag} field value.
     */
    @Override
    public Object getTag() {
        return tag;
    }

    /**
     * @param type required type of returned {@link #tag}.
     * @param <T> the required type.
     * @return The {@link #tag} casted to the required type.
     */
    public <T> T getTag(Class<T> type) {
        return type.cast(getTag());
    }

    /**
     * @param tag see the {@link #tag} field description.
     */
    public void setTag(Object tag) {
        this.tag = tag;
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
     * @return The {@link #childrenBodyLen} field value.
     */
    public Integer getChildrenBodyLen() {
        return childrenBodyLen;
    }

    /**
     * @param childrenBodyLen see the {@link #childrenBodyLen} field description.
     */
    public void setChildrenBodyLen(Integer childrenBodyLen) {
        this.childrenBodyLen = childrenBodyLen;
    }

    /**
     * @return The {@link #tagPacker} field value.
     */
    public TagPacker getTagPacker() {
        return tagPacker;
    }

    /**
     * @param tagPacker see the {@link #tagPacker} field description.
     */
    public void setTagPacker(TagPacker tagPacker) {
        this.tagPacker = tagPacker;
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
     * @return The {@link #childrenBodyPacker} field value.
     */
    public BodyPacker getChildrenBodyPacker() {
        return childrenBodyPacker;
    }

    /**
     * @param childrenBodyPacker see the {@link #childrenBodyPacker} field description.
     */
    public void setChildrenBodyPacker(BodyPacker childrenBodyPacker) {
        this.childrenBodyPacker = childrenBodyPacker;
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

    /**
     * @return The {@link #lengthPacker} field value.
     */
    public LengthPacker getLengthPacker() {
        return lengthPacker;
    }

    /**
     * @param lengthPacker see the {@link #lengthPacker} field description.
     */
    public void setLengthPacker(LengthPacker lengthPacker) {
        this.lengthPacker = lengthPacker;
    }

    /**
     * @return The {@link #bitMapPacker} field value.
     */
    public BitmapPacker getBitMapPacker() {
        return bitMapPacker;
    }

    /**
     * @param bitMapPacker see the {@link #bitMapPacker} field description.
     */
    public void setBitMapPacker(BitmapPacker bitMapPacker) {
        this.bitMapPacker = bitMapPacker;
    }

    /**
     * @return The {@link #depth} field value.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * @param depth see the {@link #depth} field description.
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * @return The {@link #root} field value.
     */
    @Override
    public MsgField getRoot() {
        return root;
    }

    /**
     * @param root see the {@link #root} field description.
     */
    public void setRoot(MsgField root) {
        this.root = root;
    }
}
