package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.tag.TagPacker;

/**
 * The implementation of the {@link TagPacker} transforms int to hex, see the {@link #pack(int, int)} and
 * {@link #unpack(byte[], int, int)} methods.
 * <p>
 * More examples
 *  <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/hex/hex-tag-packer.md">hex-tag-packer.md</a>
 * 
 * @author Kyrylo Semenko
 */
public class HexTagPacker implements TagPacker {

    /**
     * Single instance.
     */
    private static HexTagPacker instance;

    /**
     * Only one instance is allowed, see the {@link #getInstance()} method.
     */
    private HexTagPacker() {
        // empty
    }

    /**
     * @return The {@link #instance} singleton.
     */
    public static HexTagPacker getInstance() {
        if (instance == null) {
            instance = new HexTagPacker();
        }
        return instance;
    }

    /**
     * Convert for example decimal int <b>14675457</b> to bytes <b>dfee01</b>.
     */
    @Override
    public byte[] pack(int fieldTag, int tagLength) {
        String fieldNumHex = Integer.toHexString(fieldTag);
        byte[] bytes = HexService.hex2byte(fieldNumHex);
        if (bytes.length > tagLength) {
            throw new PackerRuntimeException("Packed bytes with length '" + bytes.length +
                "' is greater than required tagLength '" + tagLength + "'.");
        }
        byte[] result = new byte[tagLength];
        // If result.length > than bytes.length then copy bytes to right side of result with 00 filler at left side.
        int destPos = result.length - bytes.length;
        System.arraycopy(bytes, 0, result, destPos, bytes.length);
        return result;
    }

    /**
     * Convert for example the <b>dfee01</b> bytes to decimal int <b>14675457</b>,<br>
     * or the <b>FFEE2E</b> bytes to decimal int <b>16772654</b>.
     */
    @Override
    public int unpack(byte[] bytes, int offset, int tagLength) {
        int available = bytes.length - offset;
        if (available < tagLength) {
            throw new PackerRuntimeException("Required tagLength '" + tagLength +
                "' is greater than available bytes[] length '" + available + "'.");
        }
        byte[] tagBytes = new byte[tagLength];
        System.arraycopy(bytes, offset, tagBytes, 0, tagBytes.length);
        String hex = HexService.bytesToHex(tagBytes);
        return Integer.parseInt(hex, 16);
    }
}
