package com.credibledoc.iso8583packer.ifb;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.bitmap.BitmapService;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgValue;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link BitmapPacker} implementation for IFB format.
 * <p>
 * Actual documentation and examples
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/ifb/ifb-bitmap-packer.md">ifb-bitmap-packer.md</a>.
 *
 * @author Kyrylo Semenko
 */
public class IfbBitmapPacker implements BitmapPacker {

    /**
     * Contains created instances. Each instance is a Singleton.
     */
    private static Map<Integer, IfbBitmapPacker> instances = new ConcurrentHashMap<>();

    /**
     * Number of bytes in a packed state.
     */
    private int packedBytesLength;

    private IfbBitmapPacker(int packedBytesLength) {
        this.packedBytesLength = packedBytesLength;
    }

    /**
     * Static factory. Creates and returns singletons.
     * @param packedBytesLength see {@link #packedBytesLength}
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static IfbBitmapPacker getInstance(int packedBytesLength) {
        // TODO Kyrylo Semenko - implement 32 bytes long bitmap
        if (packedBytesLength != 16) {
            throw new PackerRuntimeException("Expected value is 16. Other formats of the BitmapPacker will be implemented later.");
        }
        instances.computeIfAbsent(packedBytesLength, k -> new IfbBitmapPacker(packedBytesLength));
        return instances.get(packedBytesLength);
    }

    /**
     * @param bitSet for packing
     * @return packed bytes
     */
    @Override
    public byte[] pack(BitSet bitSet) {
        byte[] bytes = BitmapService.bitSet2byte(bitSet, packedBytesLength);
        if (bytes.length != packedBytesLength) {
            throw new PackerRuntimeException("Result bytes length '" + bytes.length +
                "' not equals with required packedBytesLength '" + packedBytesLength + "'.");
        }
        return bytes;
    }

    /**
     * @param msgValue the target container for storing the unpacked {@link BitSet}
     * @param bytes       the source bytes
     * @param offset      starting offset within the bytes
     * @return consumed bytes number
     */
    @Override
    public int unpack(MsgValue msgValue, byte[] bytes, int offset) {
        BitSet bitSet = BitmapService.byte2BitSet(bytes, offset, packedBytesLength << 3);
        msgValue.setBitSet(bitSet);
        int len = bitSet.get(1) ? 128 : 64;
        if (packedBytesLength > 16 && bitSet.get(1) && bitSet.get(65)) {
            len = 192;
        }
        int result = Math.min(packedBytesLength, len >> 3);
        if (result != packedBytesLength) {
            throw new PackerRuntimeException("Result bytes length '" + result +
                "' not equals with required packedBytesLength '" + packedBytesLength + "'.");
        }
        return result;
    }

}
