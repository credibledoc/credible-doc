package com.credibledoc.iso8583packer.header;

import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.hex.HexService;

import java.util.BitSet;

/**
 * This container represents a header of a {@link com.credibledoc.iso8583packer.message.MsgValue}.
 * <p>
 * Header consists of two parts, mandatory {@link HeaderValue#getTagBytes()} and optional {@link HeaderValue#getLengthBytes()}.
 * For example this <b>DFEE0101</b> header contains <b>DFEE01</b> tag bytes and <b>01</b> length byte.
 * <p>
 * Header itself is optional in a {@link MsgValue#getHeaderValue()}, because some anonymous fields can have fixed length.
 * These fields contains {@link MsgField#getLen()} value with predefined fixed length.
 *
 * @author Kyrylo Semenko
 */
// TODO Kyrylo Semenko - remove to MsgValue
public class HeaderValue {
    
    /**
     * This field contains list of its children indexes. These children are located
     * in the {@link MsgValue#getChildren()} list.
     * <p>
     * This bit set can be 'null' for some nodes or leafs, but cannot be 'null' for a root {@link MsgValue}.
     */
    private BitSet bitSet;

    /**
     * // TODO Kyrylo Semenko - fix documentation
     * In case when the field has a {@link MsgValue#getFieldNum()}, this tag is represented as bytes when serialized.
     */
    private byte[] tagBytes;

    /**
     * Contains a part of this header with length of field body data. Fields with fixed length has no lengthBytes.
     */
    private byte[] lengthBytes;

    @Override
    public String toString() {
        String tagBytesString = tagBytes == null ? "null" : HexService.bytesToHex(tagBytes);
        String lengthBytesString = lengthBytes == null ? "null" : HexService.bytesToHex(lengthBytes);
        return "HeaderValue{" +
                "tagBytes=" + tagBytesString +
                ", lengthBytes=" + lengthBytesString +
                ", bitSet=" + bitSet +
                '}';
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

}
