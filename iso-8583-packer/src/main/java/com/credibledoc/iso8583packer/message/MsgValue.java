package com.credibledoc.iso8583packer.message;

import com.credibledoc.iso8583packer.hex.HexService;

import java.util.BitSet;
import java.util.List;

/**
 * This Data Object contains the value in a structure defined in the {@link MsgField}.
 * <p>
 * In the packed state the {@link MsgValue} contains a Header (optional) and a body (mandatory),
 * for example this "hex" string represents the packed leaf field
 * <pre>DFEE01010D</pre>
 * where the header is <b>DFEE0101</b> and body is <b>0D</b>.
 *
 * @author Kyrylo Semenko
 */
public class MsgValue implements Msg {

    /**
     * See the {@link Msg#getFieldNum()} description.
     */
    private Integer fieldNum;

    /**
     * See the {@link Msg#getTag()} description.
     */
    private Object tag;

    /**
     * In case when the {@link MsgValue} has defined a {@link MsgValue#getTag()},
     * the tag is represented as bytes in a packed state.
     */
    private byte[] tagBytes;

    /**
     * Contains a part of this header with length of field body data. Fields with fixed length has no lengthBytes.
     */
    private byte[] lengthBytes;

    /**
     * Field body. These bytes contains the {@link #bodyValue} packed or unpacked with {@link MsgField#getBodyPacker()}.
     */
    private byte[] bodyBytes;

    /**
     * Readable value that will be unpacked from {@link #bodyBytes} or packed to {@link #bodyBytes}
     * by an {@link MsgField#getBodyPacker()}.
     * <p>
     * In most cases the value will have the String type.
     */
    private Object bodyValue;

    /**
     * Some fields has no name, others have. This name is used for dump.
     */
    private String name;

    /**
     * Parent is a node {@link MsgValue}. It contains this child in the {@link MsgValue#getChildren()} value.
     * <p>
     * Root field has no parent node.
     */
    private MsgValue parent;

    /**
     * Sub fields of this node or ISOMsg. If this {@link MsgValue} is a leaf, this value is 'null'.
     */
    private List<MsgValue> children;

    /**
     * This field contains list of its children indexes. These children are located
     * in the {@link MsgValue#getChildren()} list.
     * <p>
     * This bit set can be 'null' for some nodes or leafs, but cannot be 'null' for a root {@link MsgValue}.
     */
    private BitSet bitSet;

    @Override
    public String toString() {
        String tagBytesString = tagBytes == null ? "null" : HexService.bytesToHex(tagBytes);
        String lengthBytesString = lengthBytes == null ? "null" : HexService.bytesToHex(lengthBytes);
        String bytesString = bodyBytes == null ? "null" : HexService.bytesToHex(bodyBytes);
        String parentString = parent == null ? "null" : parent.getName();
        String childrenSizeString = children == null ? "0" : Integer.toString(children.size());
        return "Field{" +
            "fieldNum=" + fieldNum +
            ", name=" + name +
            ", tag=" + tag +
            ", tagBytes=" + tagBytesString +
            ", lengthBytes=" + lengthBytesString +
            ", bodyBytes=" + bytesString +
            ", parent=" + parentString +
            ", value=" + bodyValue +
            ", childrenSize=" + childrenSizeString +
            ", bitSet=" + bitSet +
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
     * @return The {@link #tagBytes} field value.
     */
    public byte[] getTagBytes() {
        return tagBytes;
    }

    /**
     * @param tagBytes see the {@link #tagBytes} field description.
     */
    public void setTagBytes(byte[] tagBytes) {
        this.tagBytes = tagBytes;
    }

    /**
     * @return The {@link #lengthBytes} field value.
     */
    public byte[] getLengthBytes() {
        return lengthBytes;
    }

    /**
     * @param lengthBytes see the {@link #lengthBytes} field description.
     */
    public void setLengthBytes(byte[] lengthBytes) {
        this.lengthBytes = lengthBytes;
    }

    /**
     * @return The {@link #bodyBytes} field value.
     */
    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    /**
     * @param bodyBytes see the {@link #bodyBytes} field description.
     */
    public void setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
    }

    /**
     * @return The {@link #bodyValue} field value.
     */
    public Object getBodyValue() {
        return bodyValue;
    }

    /**
     * @param type required type of returned {@link #bodyValue}.
     * @param <T> the required type.
     * @return The {@link #bodyValue} casted to the required type.
     */
    public <T> T getBodyValue(Class<T> type) {
        return type.cast(getBodyValue());
    }

    /**
     * @param bodyValue see the {@link #bodyValue} field description.
     */
    public void setBodyValue(Object bodyValue) {
        this.bodyValue = bodyValue;
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
    public MsgValue getParent() {
        return parent;
    }

    /**
     * @param parent see the {@link #parent} field description.
     */
    public void setParent(MsgValue parent) {
        this.parent = parent;
    }

    /**
     * @return The {@link #children} field value.
     */
    @Override
    public List<MsgValue> getChildren() {
        return children;
    }

    /**
     * @param children see the {@link #children} field description.
     */
    public void setChildren(List<MsgValue> children) {
        this.children = children;
    }

    /**
     * @return The {@link #bitSet} field value.
     */
    public BitSet getBitSet() {
        return bitSet;
    }

    /**
     * @param bitSet see the {@link #bitSet} field description.
     */
    public void setBitSet(BitSet bitSet) {
        this.bitSet = bitSet;
    }
}
