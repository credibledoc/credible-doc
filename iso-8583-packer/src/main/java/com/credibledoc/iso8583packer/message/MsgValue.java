package com.credibledoc.iso8583packer.message;

import com.credibledoc.iso8583packer.header.HeaderValue;
import com.credibledoc.iso8583packer.hex.HexService;

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
     * The decimal number of this field. The number can be referred as a hex value, for example the FFEE2E hex value
     * equals with decimal 16772654.
     * The parent field contains the list of its {@link #children}.
     * Can be 'null' in some cases, for example when the parent container
     * contains the {@link HeaderValue#getBitSet()} subfield.
     */
    private Integer tagNum;

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
     * Contains subfields of the {@link MsgValue} field,
     * for example {@link HeaderValue#getTagBytes()} and {@link HeaderValue#getLengthBytes()}.
     */
    private HeaderValue headerValue = new HeaderValue();

    @Override
    public String toString() {
        String bytesString = bodyBytes == null ? "null" : HexService.bytesToHex(bodyBytes);
        String parentString = parent == null ? "null" : parent.getName();
        String childrenSizeString = children == null ? "0" : Integer.toString(children.size());
        return "Field{" +
                "tagNum=" + tagNum +
                ", name=" + name +
                ", parent=" + parentString +
                ", bodyBytes=" + bytesString +
                ", value=" + bodyValue +
                ", childrenSize=" + childrenSizeString +
                ", header=" + headerValue +
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
     * @return The {@link #headerValue} field value.
     */
    public HeaderValue getHeaderValue() {
        return headerValue;
    }

    /**
     * @param headerValue see the {@link #headerValue} field description.
     */
    public void setHeaderValue(HeaderValue headerValue) {
        this.headerValue = headerValue;
    }
}
