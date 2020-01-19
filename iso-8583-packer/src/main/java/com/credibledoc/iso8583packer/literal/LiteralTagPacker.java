package com.credibledoc.iso8583packer.literal;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.tag.TagPacker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation of the {@link TagPacker} hex String, for example F0C1 to bytes as is.
 * <p>
 * More examples
 *  <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/literal/literal-tag-packer.md">literal-tag-packer.md</a>
 * 
 * @author Kyrylo Semenko
 */
public class LiteralTagPacker implements TagPacker {
    /**
     * Haw many bytes the TAG occupies in a packed state.
     */
    private int packedLength;


    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, LiteralTagPacker> instances = new ConcurrentHashMap<>();


    /**
     * Only one instance is allowed, see the {@link #getInstance(int)} method.
     * @param packedLength the {@link MsgValue#getTagBytes()} length.
     */
    private LiteralTagPacker(int packedLength) {
        this.packedLength = packedLength;
    }

    /**
     * @param packedLength the {@link MsgValue#getTagBytes()} length.
     * @return The singleton instance from the {@link #instances} map.
     */
    public static LiteralTagPacker getInstance(int packedLength) {
        instances.computeIfAbsent(packedLength, k -> new LiteralTagPacker(packedLength));
        return instances.get(packedLength);
    }

    /**
     * Convert for example String <b>dfee01</b> to bytes <b>dfee01</b>.
     */
    @Override
    public byte[] pack(Object tag) {
        if (!(tag instanceof String)) {
            throw new PackerRuntimeException("Expected String but found " + tag.getClass().getSimpleName());
        }
        String tagHex = (String) tag;
        byte[] bytes = HexService.hex2byte(tagHex);
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
     * Convert for example the <b>FFEE2E</b> bytes to String <b>FFEE2E</b>.
     */
    @Override
    @SuppressWarnings("unchecked")
    public String unpack(byte[] bytes, int offset) {
        int available = bytes.length - offset;
        if (available < packedLength) {
            throw new PackerRuntimeException("Required tagLength '" + packedLength +
                "' is greater than available bytes[] length '" + available + "'.");
        }
        byte[] tagBytes = new byte[packedLength];
        System.arraycopy(bytes, offset, tagBytes, 0, tagBytes.length);
        return HexService.bytesToHex(tagBytes);
    }

    @Override
    public int getPackedLength() {
        return packedLength;
    }
}
