package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.tag.TagPacker;

/**
 * The implementation of the {@link TagPacker} transforms int to hex, see the {@link #pack(int, int)} and
 * {@link #unpack(byte[], int, int)} methods.
 * 
 * @author Kyrylo Semenko
 */
public class HexTagPacker implements TagPacker {
    public static final HexTagPacker INSTANCE = new HexTagPacker();

    /**
     * Convert for example decimal int <b>14675457</b> to bytes <b>dfee01</b>.
     */
    @Override
    public byte[] pack(int fieldTag, int tagLength) {
        String fieldNumHex = Integer.toHexString(fieldTag);
        return HexService.hex2byte(fieldNumHex);
    }

    /**
     * Convert for example the <b>dfee01</b> bytes to decimal int <b>14675457</b>,<br>
     * or the <b>FFEE2E</b> bytes to decimal int <b>16772654</b>.
     */
    @Override
    public int unpack(byte[] bytes, int offset, int tagLength) {
        byte[] tagBytes = new byte[tagLength];
        System.arraycopy(bytes, offset, tagBytes, 0, tagBytes.length);
        String hex = HexService.bytesToHex(tagBytes);
        return Integer.parseInt(hex, 16);
    }
}
