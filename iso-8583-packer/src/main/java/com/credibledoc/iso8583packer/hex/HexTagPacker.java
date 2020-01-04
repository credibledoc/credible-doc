package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.header.HeaderValue;
import com.credibledoc.iso8583packer.tag.TagPacker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation of the {@link TagPacker} transforms int to hex, see the {@link #pack(Object)} and
 * {@link #unpack(byte[], int)} methods.
 * <p>
 * More examples
 *  <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/hex/hex-tag-packer.md">hex-tag-packer.md</a>
 * 
 * @author Kyrylo Semenko
 */
public class HexTagPacker implements TagPacker {
    /**
     * Haw many bytes the TAG occupies in a packed state.
     */
    private int packedLength;


    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, HexTagPacker> instances = new ConcurrentHashMap<>();


    /**
     * Only one instance is allowed, see the {@link #getInstance(int)} method.
     * @param packedLength the {@link HeaderValue#getTagBytes()} length.
     */
    private HexTagPacker(int packedLength) {
        this.packedLength = packedLength;
    }

    /**
     * @param packedLength the {@link HeaderValue#getTagBytes()} length.
     * @return The singleton instance from the {@link #instances} map.
     */
    public static HexTagPacker getInstance(int packedLength) {
        instances.computeIfAbsent(packedLength, k -> new HexTagPacker(packedLength));
        return instances.get(packedLength);
    }

    /**
     * Convert for example decimal int <b>14675457</b> to bytes <b>dfee01</b>.
     */
    @Override
    public byte[] pack(Object tag) {
        if (!(tag instanceof Integer)) {
            throw new PackerRuntimeException("Expected Integer but found " + tag.getClass().getSimpleName());
        }
        Integer fieldTag = (Integer) tag;
        String fieldNumHex = Integer.toHexString(fieldTag);
        byte[] bytes = HexService.hex2byte(fieldNumHex);
        if (bytes.length > packedLength) {
            throw new PackerRuntimeException("Packed bytes with length '" + bytes.length +
                "' is greater than required tagLength '" + packedLength + "'.");
        }
        byte[] result = new byte[packedLength];
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
    @SuppressWarnings("unchecked")
    public Integer unpack(byte[] bytes, int offset) {
        int available = bytes.length - offset;
        if (available < packedLength) {
            throw new PackerRuntimeException("Required tagLength '" + packedLength +
                "' is greater than available bytes[] length '" + available + "'.");
        }
        byte[] tagBytes = new byte[packedLength];
        System.arraycopy(bytes, offset, tagBytes, 0, tagBytes.length);
        String hex = HexService.bytesToHex(tagBytes);
        return Integer.parseInt(hex, 16);
    }

    @Override
    public int getPackedLength() {
        return packedLength;
    }
}
