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
    
    /**
     * Single instance.
     */
    private static IfbBitmapPacker instance;

    /**
     * @return the {@link #instance} value (Singleton pattern).
     */
    public static IfbBitmapPacker getInstance() {
        if (instance == null) {
            instance = new IfbBitmapPacker();
        }
        return instance;
    }

    /**
     * @param bitSet for packing
     * @return packed bytes
     */
    @Override
    public byte[] pack(BitSet bitSet, int packedBytesLength) {
        int len =                                           // bytes needed to encode BitSet (in 8-byte chunks)
            packedBytesLength >= 8 ?
                bitSet.length() + 62 >> 6 << 3 : packedBytesLength;    // +62 because we don't use bit 0 in the BitSet
        return BitmapService.bitSet2byte(bitSet, len);
    }

    /**
     * @param headerField the target container for storing the unpacked {@link BitSet}
     * @param bytes       the source bytes
     * @param offset      starting offset within the bytes
     * @return consumed bytes number
     */
    @Override
    public int unpack(HeaderField headerField, byte[] bytes, int offset, int packedBytesLength) {
        int len;
        BitSet bitSet = BitmapService.byte2BitSet(bytes, offset, packedBytesLength << 3);
        headerField.setBitSet(bitSet);
        len = bitSet.get(1) ? 128 : 64;
        if (packedBytesLength > 16 && bitSet.get(1) && bitSet.get(65)) {
            len = 192;
        }
        return Math.min(packedBytesLength, len >> 3);
    }

}
