package com.credibledoc.iso8583packer.ifa;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.bitmap.BitmapService;
import com.credibledoc.iso8583packer.header.HeaderValue;
import com.credibledoc.iso8583packer.hex.HexService;

import java.util.BitSet;

/**
 * The {@link BitmapPacker} implementation for IFB format.
 *
 * @author Kyrylo Semenko
 */
public class IfaBitmapPacker implements BitmapPacker {

    /**
     * Single instance.
     */
    private static IfaBitmapPacker instance;

    /**
     * @return the {@link #instance} value (Singleton pattern).
     */
    public static IfaBitmapPacker getInstance() {
        if (instance == null) {
            instance = new IfaBitmapPacker();
        }
        return instance;
    }

    /**
     * @param bitSet for packing
     * @return packed bytes
     */
    @Override
    public byte[] pack(BitSet bitSet, int packedBytesLength) {
        int len =
            packedBytesLength >= 8 ?
                bitSet.length() + 62 >> 6 << 3 : packedBytesLength;
        return HexService.bytesToHex(BitmapService.bitSet2byte(bitSet, len)).getBytes();
    }

    /**
     * @param headerValue the target container for storing the unpacked {@link BitSet}
     * @param bytes       the source bytes
     * @param offset      starting offset within the bytes
     * @return consumed bytes number
     */
    @Override
    public int unpack(HeaderValue headerValue, byte[] bytes, int offset, int packedBytesLength) {
        int len;
        BitSet bmap = BitmapService.hex2BitSet(bytes, offset, packedBytesLength << 3);
        headerValue.setBitSet(bmap);
        len = bmap.get(1) ? 128 : 64;
        if (packedBytesLength > 16 && bmap.get(65)) {
            len = 192;
            bmap.clear(65);
        }
        return Math.min(packedBytesLength << 1, len >> 2);
    }

}
