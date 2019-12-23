package com.credibledoc.iso8583packer.ifa;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.bitmap.BitmapService;
import com.credibledoc.iso8583packer.header.HeaderField;
import com.credibledoc.iso8583packer.hex.HexService;

import java.util.BitSet;

/**
 * The {@link BitmapPacker} implementation for IFB format.
 *
 * @author Kyrylo Semenko
 */
public class IfaBitmapPacker implements BitmapPacker {
    public static final IfaBitmapPacker L1 = new IfaBitmapPacker(1);
    public static final IfaBitmapPacker L8 = new IfaBitmapPacker(8);
    public static final IfaBitmapPacker L16 = new IfaBitmapPacker(16);
    public static final IfaBitmapPacker L24 = new IfaBitmapPacker(24);

    /**
     * Number of bytes in the bitmap {@link BitSet}.
     */
    private int length;

    /**
     * @param length - see the {@link #length} field.
     */
    public IfaBitmapPacker(int length) {
        this.length = length;
    }

    /**
     * @param bitSet for packing
     * @return packed bytes
     */
    @Override
    public byte[] pack(BitSet bitSet) {
        int len =
            length >= 8 ?
                bitSet.length() + 62 >> 6 << 3 : length;
        return HexService.hexString(BitmapService.bitSet2byte(bitSet, len)).getBytes();
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
        BitSet bmap = BitmapService.hex2BitSet(bytes, offset, length << 3);
        headerField.setBitSet(bmap);
        len = bmap.get(1) ? 128 : 64;
        if (length > 16 && bmap.get(65)) {
            len = 192;
            bmap.clear(65);
        }
        return Math.min(length << 1, len >> 2);
    }

    @Override
    public int getMaxPackedLength() {
        return length >> 2;
    }
}
