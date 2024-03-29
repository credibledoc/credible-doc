package com.credibledoc.iso8583packer.ifa;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.bitmap.BitmapService;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgValue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
     * Contains created instances. Each instance is a Singleton. Key is the {@link #bitsetBytesLength} value.
     */
    private static final Map<Integer, IfaBitmapPacker> instances = new ConcurrentHashMap<>();

    /**
     * Number of bytes in a packed state. Value -1 means the number of bytes
     * depends on a maximal child's {@link MsgField#getFieldNum()} value, see the {@link #getInstance()} description.
     */
    private final int bitsetBytesLength;

    /**
     * Helps to prepare {@link BitSet} data.
     */
    private final IfbBitmapPacker ifbBitmapPacker;

    private IfaBitmapPacker(int bitsetBytesLength) {
        this.bitsetBytesLength = bitsetBytesLength;
        ifbBitmapPacker = IfbBitmapPacker.getInstance(bitsetBytesLength);
    }

    /**
     * Static factory. Creates and returns singletons stored in the {@link #instances} map.
     * @param bitsetBytesLength number of bytes in {@link BitSet}. For example:
        <table caption="Primary, secondary and tertiary bitmap examples">
            <tr>
                <th><b>BitsetBytesLength &nbsp;</b></th>
                <th><b>Bitset HEX &nbsp;</b></th>
                <th><b>Packed HEX &nbsp;</b></th>
            </tr>
            <tr>
                <td>24</td>
                <td>C000000000000000C0000000000000004000000000000000 &nbsp;</td>
                <td>433030303030303030303030303030304330303030303030303030303030303034303030303030303030303030303030</td>
            </tr>
            <tr>
                <td>16</td>
                <td>D0000000000000004000000000000000</td>
                <td>4430303030303030303030303030303034303030303030303030303030303030</td>
            </tr>
            <tr>
                <td>8</td>
                <td>5000000000000000</td>
                <td>35303030303030303030303030303030</td>
            </tr>
            <tr>
                <td>1</td>
                <td>14</td>
                <td>3530</td>
            </tr>
        </table>
     *                          See https://neapay.com/online-tools/bitmap-fields-decoder.html
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static IfaBitmapPacker getInstance(int bitsetBytesLength) {
        if ((bitsetBytesLength < 1 || bitsetBytesLength > 8) && bitsetBytesLength != 16 && bitsetBytesLength != 24) {
            throw new PackerRuntimeException("Expected value are from 1 to 8, 16 or 24.");
        }
        instances.computeIfAbsent(bitsetBytesLength, k -> new IfaBitmapPacker(bitsetBytesLength));
        return instances.get(bitsetBytesLength);
    }

    /**
     * Static factory that creates an adaptable bit map, where its length depends on a maximal
     * child {@link MsgField#getFieldNum()} value. The packed
     * bitMap bytes may have 8*2, 16*2 or 24*2 bytes.
     * See examples in the {@link #getInstance(int)} method description.
     * 
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static IfaBitmapPacker getInstance() {
        int bitsetBytesLength = -1;
        instances.computeIfAbsent(bitsetBytesLength, k -> new IfaBitmapPacker(bitsetBytesLength));
        return instances.get(bitsetBytesLength);
    }

    /**
     * @param bitSet for packing
     * @return Packed bytes
     */
    @Override
    public byte[] pack(BitSet bitSet) {
        byte[] ifb = ifbBitmapPacker.pack(bitSet);
        return HexService.bytesToHex(ifb).getBytes(ISO_88591);
    }

    /**
     * @param msgValue the target container for storing the unpacked {@link BitSet}
     * @param bytes    the source bytes
     * @param offset   starting offset within the bytes
     * @return Consumed bytes number
     */
    @Override
    public int unpack(MsgValue msgValue, byte[] bytes, int offset) {
        int resolvedLen = resolvePackedLen(bytes, offset);
        byte[] ifaBytes = Arrays.copyOfRange(bytes, offset, offset + resolvedLen);
        String ifbString = new String(ifaBytes, ISO_88591);
        byte[] ifbBytes = HexService.hex2byte(ifbString);
        int unpackedLen = ifbBitmapPacker.unpack(msgValue, ifbBytes, 0);
        if (bitsetBytesLength != -1 && unpackedLen != bitsetBytesLength) {
            throw new PackerRuntimeException("Result bytes length '" + unpackedLen +
                "' not equals with required packedBytesLength '" + resolvedLen + "'.");
        }
        return resolvedLen;
    }

    private int resolvePackedLen(byte[] bytes, int offset) {
        int result = 8;
        if (getPackedBytesLength() != -1) {
            return getPackedBytesLength();
        }
        if (firstBitHasFlag(bytes, offset, 0)) {
            result = 16; // existing secondary bitmap
            if (firstBitHasFlag(bytes, offset, 8 * 2)) {
                result = 24; // existing tertiary bitmap
            }
        }
        return result * 2;
    }

    private boolean firstBitHasFlag(byte[] bytes, int firstOffset, int secondOffset) {
        int offset = firstOffset + secondOffset;
        if (bytes.length < offset + 2) {
            return false;
        }
        byte[] ifaBytes = Arrays.copyOfRange(bytes, offset, offset + 2);
        String ifbString = new String(ifaBytes, ISO_88591);
        byte[] ifbBytes = HexService.hex2byte(ifbString);
        return BitmapService.hasFlag(ifbBytes[0]);
    }

    @Override
    public int getPackedBytesLength() {
        if (bitsetBytesLength > 0) {
            return bitsetBytesLength * 2;
        }
        return bitsetBytesLength;
    }
}
