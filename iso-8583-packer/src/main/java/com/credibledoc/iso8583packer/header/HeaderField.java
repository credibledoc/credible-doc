package com.credibledoc.iso8583packer.header;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgValue;

import java.util.BitSet;

/**
 * The container represents a header of a {@link MsgField}. It contains the definition of the {@link MsgField} header.
 * <p>
 * The header itself is optional in a {@link MsgField#getHeaderField()}, because some anonymous fields
 * can have the fixed length.
 * <p>
 * These fields contains the {@link MsgField#getLen()} value with predefined fixed length.
 *
 * @author Kyrylo Semenko
 */
public class HeaderField {
    /**
     * This field contains list of indexes of its children. These children are located
     * in the {@link MsgField#getChildren()} list.
     * <p>
     * This bit set can be 'null' for some nodes or leafs, but cannot be 'null' for a root {@link MsgField}.
     */
    private BitSet bitSet;

    /**
     * Packs from int to bytes and wise versa the {@link HeaderValue#getLengthBytes()} subfield.
     * <p>
     * The calculated value says how many bytes contains the {@link MsgValue#getBodyBytes()} subfield.
     * <p>
     * Only one {@link LengthPacker} can be defined, parent {@link MsgField#getChildrenLengthPacker()}
     * or the lengthPacker. Else an exception is thrown.
     */
    private LengthPacker lengthPacker;

    /**
     * Packs from {@link BitSet} to bytes and wise versa the {@link #bitSet} subfield.
     */
    private BitmapPacker bitMapPacker;
    
    @Override
    public String toString() {
        String lengthPackerString = lengthPacker == null ? "null" : lengthPacker.getClass().getSimpleName();
        String bitMapPackerString = bitMapPacker == null ? "null" : bitMapPacker.getClass().getSimpleName();
        return "HeaderField{" +
                "lengthPacker=" + lengthPackerString +
                ", bitSet=" + bitSet +
                ", bitMapPacker=" + bitMapPackerString +
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
}
