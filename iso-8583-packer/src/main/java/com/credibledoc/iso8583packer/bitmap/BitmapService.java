package com.credibledoc.iso8583packer.bitmap;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

import java.util.BitSet;

/**
 * Static service. Converts <a href="https://en.wikipedia.org/wiki/ISO_8583#Bitmaps">Bitmap</a> data.
 *
 * @author apr@cs.com.uy
 * @author Hani S. Kirollos
 * @author Alwyn Schoeman
 * @author Kyrylo Semenko
 */
public class BitmapService {

    private BitmapService() {
        throw new PackerRuntimeException("Please do not instantiate this static helper.");
    }

    /**
     * converts a BitSet into a binary field
     * used in pack routines
     *
     * @param b     - the BitSet
     * @param bytes - number of bytes to return
     * @return binary representation
     */
    public static byte[] bitSet2byte(BitSet b, int bytes) {
        int len = bytes * 8;

        byte[] d = new byte[bytes];
        for (int i = 0; i < len; i++) {
            if (b.get(i + 1)) {
                d[i >> 3] |= (0x80 >> (i % 8));
            }
        }
        if (len > 64) {
            d[0] |= 0x80;
        }
        if (len > 128) {
            d[8] |= 0x80;
        }
        return d;
    }

    /**
     * Converts an ASCII representation of a Bitmap field
     * into a Java BitSet
     *
     * @param b       hex representation
     * @param offset  starting offset
     * @param maxBits max number of bits (supports 8, 16, 24, 32, 48, 52, 64,.. 128 or 192)
     * @return java BitSet object
     */
    public static BitSet hex2BitSet(byte[] b, int offset, int maxBits) {
        int shiftedLength = Character.digit((char) b[offset], 16) & 0x08;
        int decidedLength = shiftedLength == 8 ? 128 : 64;
        int len = maxBits > 64 ? decidedLength : maxBits;
        BitSet bmap = new BitSet(len);
        for (int i = 0; i < len; i++) {
            int digit = Character.digit((char) b[offset + (i >> 2)], 16);
            if ((digit & (0x08 >> (i % 4))) > 0) {
                bmap.set(i + 1);
                if (i == 65 && maxBits > 128) {
                    len = 192;
                }
            }
        }
        return bmap;
    }

    /**
     * Converts a binary representation of a Bitmap field
     * into a Java BitSet
     *
     * @param b       - binary representation
     * @param offset  - staring offset
     * @param maxBits - max number of bits (supports 64,128 or 192)
     * @return java BitSet object
     */
    public static BitSet byte2BitSet(byte[] b, int offset, int maxBits) {
        int decidedLength = (b[offset] & 0x80) == 0x80 ? 128 : 64;
        int len = maxBits > 64 ? decidedLength : maxBits;

        if (maxBits > 128 && b.length > offset + 8 && (b[offset + 8] & 0x80) == 0x80) {
            len = 192;
        }
        BitSet bmap = new BitSet(len);
        for (int i = 0; i < len; i++) {
            int shifted = i >> 3;
            int shiftedOffset = offset + shifted;
            if (((b[shiftedOffset]) & (0x80 >> (i % 8))) > 0) {
                bmap.set(i + 1);
            }
        }
        return bmap;
    }
}
