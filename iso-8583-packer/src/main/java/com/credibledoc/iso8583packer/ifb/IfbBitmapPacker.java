package com.credibledoc.iso8583packer.ifb;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.bitmap.BitmapService;
import com.credibledoc.iso8583packer.header.HeaderField;

import java.util.BitSet;

/**
 * The {@link BitmapPacker} implementation for IFB format.
 *
 * @author Kyrylo Semenko
 */
public class IfbBitmapPacker implements BitmapPacker {
    public static final IfbBitmapPacker L1 = new IfbBitmapPacker(1);
    public static final IfbBitmapPacker L8 = new IfbBitmapPacker(8);
    public static final IfbBitmapPacker L16 = new IfbBitmapPacker(16);
    public static final IfbBitmapPacker L24 = new IfbBitmapPacker(24);

    /**
     * Number of bytes in the bitmap {@link BitSet}.
     */
    private int length;

    /**
     * @param length - see the {@link #length} field.
     */
    public IfbBitmapPacker(int length) {
        this.length = length;
    }

    /**
     * @param bitSet for packing
     * @return packed bytes
     */
    @Override
    public byte[] pack(BitSet bitSet) {
        int len =                                           // bytes needed to encode BitSet (in 8-byte chunks)
            length >= 8 ?
                bitSet.length() + 62 >> 6 << 3 : length;    // +62 because we don't use bit 0 in the BitSet
        return BitmapService.bitSet2byte(bitSet, len);
    }

    /**
     * @param headerField the target container for storing the unpacked {@link BitSet}
     * @param bytes       the source bytes
     * @param offset      starting offset within the bytes
     * @return consumed bytes number
     */
    @Override
    public int unpack(HeaderField headerField, byte[] bytes, int offset) {
        int len;
        BitSet bitSet = BitmapService.byte2BitSet(bytes, offset, length << 3);
        headerField.setBitSet(bitSet);
        len = bitSet.get(1) ? 128 : 64;
        if (length > 16 && bitSet.get(1) && bitSet.get(65)) {
            len = 192;
        }
        return Math.min(length, len >> 3);
    }

    @Override
    public int getMaxPackedLength() {
        return length >> 3;
    }
}
