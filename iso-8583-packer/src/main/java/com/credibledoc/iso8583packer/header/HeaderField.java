package com.credibledoc.iso8583packer.header;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgValue;

/**
 * The container represents a header of a {@link MsgField}. It contains the definition of the {@link MsgField} header.
 * <p>
 * The header itself is optional in a {@link MsgField#getHeaderField()}, because some anonymous fields
 * can have a fixed length.
 * <p>
 * These fields with header contain a {@link MsgField#getLen()} value with predefined fixed length.
 *
 * @author Kyrylo Semenko
 */
// TODO Kyrylo Semenko - remove to MsgField
public class HeaderField {

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
     * Packs and unpacks bytes of the {@link HeaderValue#getBitSet()} subfield.
     */
    private BitmapPacker bitMapPacker;
    
    @Override
    public String toString() {
        String lengthPackerString = lengthPacker == null ? "null" : lengthPacker.getClass().getSimpleName();
        String bitMapPackerString = bitMapPacker == null ? "null" : bitMapPacker.getClass().getSimpleName();
        return "HeaderField{" +
                "lengthPacker=" + lengthPackerString +
                ", bitMapPacker=" + bitMapPackerString +
                '}';
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
