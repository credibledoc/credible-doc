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

    private static final int MAX_FIELD_NUM_192 = 192;
    /**
     * Contains created instances. Each instance is a Singleton.
     */
    private static final Map<Integer, IfbBitmapPacker> instances = new ConcurrentHashMap<>();

    /**
     * Number of bytes in a packed state. Accepted values are from 1 to 8, 16 or 24.
     * In case of 8 bytes the packed length depends on
     * a resolved {@link BitSet} maximal fieldNum,
     * for example fieldNum 65 cannot be encoded in 8 bytes and an additional
     * 8 bytes will be appended to the packed bytes.
     */
    private final int packedBytesLength;

    private IfbBitmapPacker(int packedBytesLength) {
        if (packedBytesLength < 1 || (packedBytesLength > 8 && !(packedBytesLength == 16 || packedBytesLength == 24))) {
            throw new PackerRuntimeException("PackedBytesLength '" + packedBytesLength +
                "' cannot be accepted. Expected values are from 1 to 8 or 16 or 24.");
        }
        this.packedBytesLength = packedBytesLength;
    }

    /**
     * Static factory. Creates and returns singletons.
     * @param packedBytesLength see {@link #packedBytesLength}
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static IfbBitmapPacker getInstance(int packedBytesLength) {
        instances.computeIfAbsent(packedBytesLength, k -> new IfbBitmapPacker(packedBytesLength));
        return instances.get(packedBytesLength);
    }

    /**
     * @param bitSet for packing
     * @return packed bytes
     */
    @Override
    public byte[] pack(BitSet bitSet) {
        // 1 byte max fieldNum 8
        // 2 bytes max fieldNum 16
        // 3 bytes max fieldNum 24
        // ...
        // 8 bytes max fieldNum 64
        // 16 bytes max fieldNum 128
        // 24 bytes max fieldNum 192

        int resolvedBytesLength = this.packedBytesLength;
        if (resolvedBytesLength == 8) {
            int maxFieldNum = bitSet.previousSetBit(MAX_FIELD_NUM_192 + 1);
            if (maxFieldNum > 128) {
                resolvedBytesLength = 8 * 3;
            } else if (maxFieldNum > 64) {
                resolvedBytesLength = 8 * 2;
            }
        }
        int maxPossibleSetBit = resolvedBytesLength * 8;
        int bitOutOfBoundary = bitSet.nextSetBit(maxPossibleSetBit + 1);
        boolean existsBitOutOfBoundary = bitOutOfBoundary > -1;
        if (existsBitOutOfBoundary) {
            throw new PackerRuntimeException("BitSet '" + bitSet + "' contains bit '" + bitOutOfBoundary +
                "' that is greater than maximum possible bit '" + maxPossibleSetBit +
                "' that can be encoded to a bytes array with length '" + resolvedBytesLength + "'.");
        }

        return BitmapService.bitSet2byte(bitSet, resolvedBytesLength);
    }

    /**
     * @param msgValue the target container for storing the unpacked {@link BitSet}
     * @param bytes       the source bytes
     * @param offset      starting offset within the bytes
     * @return consumed bytes number
     */
    @Override
    public int unpack(MsgValue msgValue, byte[] bytes, int offset) {
        int maxFieldNum = packedBytesLength << 3;
        BitSet bitSet = BitmapService.byte2BitSet(bytes, offset, maxFieldNum);
        if (packedBytesLength != 8) {
            msgValue.setBitSet(bitSet);
            return packedBytesLength;
        }
        int unpackedBytesLength = packedBytesLength;
        if (bitSet.get(1)) {
            maxFieldNum = 128;
            bitSet = BitmapService.byte2BitSet(bytes, offset, maxFieldNum);
            unpackedBytesLength = unpackedBytesLength + packedBytesLength;
            if (bitSet.get(65)) {
                maxFieldNum = 192;
                bitSet = BitmapService.byte2BitSet(bytes, offset, maxFieldNum);
                unpackedBytesLength = unpackedBytesLength + packedBytesLength;
            }
        }
        msgValue.setBitSet(bitSet);
        return unpackedBytesLength;
    }

    @Override
    public int getPackedBytesLength() {
        return packedBytesLength;
    }
}
