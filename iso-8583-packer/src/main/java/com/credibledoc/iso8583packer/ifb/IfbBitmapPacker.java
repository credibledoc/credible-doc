package com.credibledoc.iso8583packer.ifb;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.bitmap.BitmapService;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgField;
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
    private static final int SINGLE_BITMAP_LENGTH_8 = 8;

    /**
     * Number of bytes in a packed state. Accepted values are from 1 to 8, 16 or 24 or -1.
     * In case of 8 bytes the packed length depends on
     * a resolved {@link BitSet} maximal fieldNum,
     * for example fieldNum 65 cannot be encoded in 8 bytes and an additional
     * 8 bytes will be appended to the packed bytes.
     * <p>
     * The -1 value means, that the packer is adaptable. It will have 8 or 16 or 24 bytes,
     * depending on the maximal {@link MsgField#getFieldNum()} value.
     */
    private final int packedBytesLength;

    private IfbBitmapPacker(int packedBytesLength) {
        if (packedBytesLength < -1 || packedBytesLength == 0 ||
                (packedBytesLength > SINGLE_BITMAP_LENGTH_8 && !(packedBytesLength == 16 || packedBytesLength == 24))) {
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
     * Static factory that creates an adaptable bit map, where its length depends on a maximal
     * child {@link MsgField#getFieldNum()} value. The packed
     * bitMap bytes may have 8, 16 or 24 bytes.
     *
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static IfbBitmapPacker getInstance() {
        int packedBytesLength = -1;
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

        int resolvedBytesLength;
        if (getPackedBytesLength() == -1) {
            int maxFieldNum = bitSet.previousSetBit(MAX_FIELD_NUM_192 + 1);
            if (maxFieldNum > 192) {
                throw new PackerRuntimeException("Maximal allowed fieldNum is 192. Current field num is " + maxFieldNum);
            }
            if (maxFieldNum > 128) {
                resolvedBytesLength = 24;
            } else if (maxFieldNum > 64) {
                resolvedBytesLength = 16;
            } else {
                resolvedBytesLength = SINGLE_BITMAP_LENGTH_8;
            }
        } else {
            resolvedBytesLength = getPackedBytesLength();
            if (resolvedBytesLength == SINGLE_BITMAP_LENGTH_8) {
                int maxFieldNum = bitSet.previousSetBit(MAX_FIELD_NUM_192 + 1);
                if (maxFieldNum > 128) {
                    resolvedBytesLength = SINGLE_BITMAP_LENGTH_8 * 3;
                } else if (maxFieldNum > 64) {
                    resolvedBytesLength = SINGLE_BITMAP_LENGTH_8 * 2;
                }
            }
        }
        int maxPossibleSetBit = resolvedBytesLength * SINGLE_BITMAP_LENGTH_8;
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
        int maxFieldNum = resolveMaxFieldNum(bytes, offset);
        BitSet bitSet = BitmapService.byte2BitSet(bytes, offset, maxFieldNum);
        int unpackedBytesLength = SINGLE_BITMAP_LENGTH_8;
        if (bitSet.get(1)) {
            maxFieldNum = 128;
            bitSet = BitmapService.byte2BitSet(bytes, offset, maxFieldNum);
            unpackedBytesLength = unpackedBytesLength + SINGLE_BITMAP_LENGTH_8;
            if (bitSet.get(65)) {
                maxFieldNum = 192;
                bitSet = BitmapService.byte2BitSet(bytes, offset, maxFieldNum);
                unpackedBytesLength = unpackedBytesLength + SINGLE_BITMAP_LENGTH_8;
            }
        }
        msgValue.setBitSet(bitSet);
        return unpackedBytesLength;
    }

    private int resolveMaxFieldNum(byte[] bytes, int offset) {
        if (packedBytesLength == -1) {
            int result = 64;
            if (BitmapService.hasFlag(bytes[offset])) {
                result = 128;
                if (bytes.length >= offset + SINGLE_BITMAP_LENGTH_8 && BitmapService.hasFlag(bytes[offset + SINGLE_BITMAP_LENGTH_8])) {
                    result = 192;
                }
            }
            return result;
        }
        return packedBytesLength << 3;
    }

    @Override
    public int getPackedBytesLength() {
        return packedBytesLength;
    }
}
