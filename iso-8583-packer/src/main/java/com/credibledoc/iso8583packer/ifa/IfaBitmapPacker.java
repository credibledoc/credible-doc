package com.credibledoc.iso8583packer.ifa;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.bitmap.BitmapService;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgValue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link BitmapPacker} implementation for IFA format. It uses {@link #ISO_88591} charset.
 * <p>
 * Actual documentation and examples
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/ifa/ifa-bitmap-packer.md">ifa-bitmap-packer.md</a>.
 *
 * @author Kyrylo Semenko
 */
public class IfaBitmapPacker implements BitmapPacker {
    private static final Charset ISO_88591 = StandardCharsets.ISO_8859_1;

    /**
     * Contains created instances. Each instance is a Singleton.
     */
    private static Map<Integer, IfaBitmapPacker> instances = new ConcurrentHashMap<>();

    /**
     * Number of bytes in a packed state.
     */
    private int packedBytesLength;

    private IfaBitmapPacker(int packedBytesLength) {
        this.packedBytesLength = packedBytesLength;
    }

    /**
     * Static factory. Creates and returns singletons.
     * @param packedBytesLength see {@link #packedBytesLength}
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static IfaBitmapPacker getInstance(int packedBytesLength) {
        // TODO Kyrylo Semenko - implement 32 bytes long bitmap
        if (packedBytesLength != 16) {
            throw new PackerRuntimeException("Expected value is 16. Other formats of the BitmapPacker will be implemented later.");
        }
        instances.computeIfAbsent(packedBytesLength, k -> new IfaBitmapPacker(packedBytesLength));
        return instances.get(packedBytesLength);
    }

    /**
     * @param bitSet for packing
     * @return packed bytes
     */
    @Override
    public byte[] pack(BitSet bitSet) {
        int len = packedBytesLength >= 8 ?
                bitSet.length() + 62 >> 6 << 3 : packedBytesLength;
        byte[] bytes = BitmapService.bitSet2byte(bitSet, len);
        byte[] result = HexService.bytesToHex(bytes).getBytes(ISO_88591);
        if (result.length != packedBytesLength) {
            throw new PackerRuntimeException("Result bytes length '" + result.length +
                "' not equals with required packedBytesLength '" + packedBytesLength + "'.");
        }
        return result;
    }

    /**
     * @param msgValue the target container for storing the unpacked {@link BitSet}
     * @param bytes       the source bytes
     * @param offset      starting offset within the bytes
     * @return consumed bytes number
     */
    @Override
    public int unpack(MsgValue msgValue, byte[] bytes, int offset) {
        BitSet bmap = BitmapService.hex2BitSet(bytes, offset, packedBytesLength << 3);
        msgValue.setBitSet(bmap);
        int len = bmap.get(1) ? 128 : 64;
        if (packedBytesLength > 16 && bmap.get(65)) {
            len = 192;
            bmap.clear(65);
        }
        int result = Math.min(packedBytesLength << 1, len >> 2);
        if (result != packedBytesLength) {
            throw new PackerRuntimeException("Result bytes length '" + result +
                "' not equals with required packedBytesLength '" + packedBytesLength + "'.");
        }
        return result;
    }

}
