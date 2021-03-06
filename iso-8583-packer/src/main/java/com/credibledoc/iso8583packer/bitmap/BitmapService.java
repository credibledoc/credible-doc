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
     * Convert a BitSet into a binary field
     *
     * @param bitSet      the BitSet
     * @param bytesNumber number of bytes to return
     * @return binary representation
     */
    public static byte[] bitSet2byte(BitSet bitSet, int bytesNumber) {
        int len = bytesNumber * 8;

        byte[] bytes = new byte[bytesNumber];
        for (int i = 0; i < len; i++) {
            if (bitSet.get(i + 1)) {
                bytes[i >> 3] |= (0x80 >> (i % 8));
            }
        }
        if (len > 64) {
            bytes[0] |= 0x80;
        }
        if (len > 128) {
            bytes[8] |= 0x80;
        }
        return bytes;
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
     * Converts a binary representation of a Bitmap field into a Java BitSet
     *
     * @param bytes   binary representation
     * @param offset  staring offset
     * @param maxBits max number of bits (supports 64,128 or 192)
     * @return java BitSet object
     */
    public static BitSet byte2BitSet(byte[] bytes, int offset, int maxBits) {
        int decidedLength = (bytes[offset] & 0x80) == 0x80 ? 128 : 64;
        int len = maxBits > 64 ? decidedLength : maxBits;

        if (maxBits > 128 && bytes.length > offset + 8 && (bytes[offset + 8] & 0x80) == 0x80) {
            len = 192;
        }
        BitSet bmap = new BitSet(len);
        for (int i = 0; i < len; i++) {
            int shifted = i >> 3;
            int shiftedOffset = offset + shifted;
            if (((bytes[shiftedOffset]) & (0x80 >> (i % 8))) > 0) {
                bmap.set(i + 1);
            }
        }
        return bmap;
    }
}
