package com.credibledoc.iso8583packer.ebcdic;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.string.StringUtils;
import com.credibledoc.iso8583packer.tag.TagPacker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * See the
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/ebcdic/ebcdic-decimal-tag-packer.md">ebcdic-decimal-tag-packer.md</a>
 * documentation.
 * <p>
 * See the {@link #pack(int)} and {@link #unpack(byte[], int)} methods examples.
 *
 * @author Kyrylo Semenko
 */
public class EbcdicDecimalTagPacker implements TagPacker {

    private static final int TWO_CHARS_IN_HEX_BYTE = 2;
    private static final String FILLER_F = "F";
    private static final char PAD_CHAR_0 = '0';

    /**
     * Haw many bytes the TAG occupies in a packed state.
     */
    private int packedLength;


    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, EbcdicDecimalTagPacker> instances = new ConcurrentHashMap<>();


    /**
     * Only one instance is allowed, see the {@link #getInstance(int)} method.
     */
    private EbcdicDecimalTagPacker(int packedLength) {
        this.packedLength = packedLength;
    }

    /**
     * @return The singleton instance from the {@link #instances} map.
     */
    public static EbcdicDecimalTagPacker getInstance(int packedLength) {
        instances.computeIfAbsent(packedLength, k -> new EbcdicDecimalTagPacker(packedLength));
        return instances.get(packedLength);
    }

    /**
     * Pack for example decimal <b>90</b> to bytes <b>F9F0</b>.
     */
    @Override
    public byte[] pack(int fieldTag) {
        StringBuilder stringBuilder = new StringBuilder(packedLength * TWO_CHARS_IN_HEX_BYTE);
        String padded = StringUtils.leftPad(Integer.toString(fieldTag), packedLength, PAD_CHAR_0);
        for (char nextNum : padded.toCharArray()) {
            stringBuilder.append(FILLER_F).append(nextNum);
        }
        byte[] bytes = HexService.hex2byte(stringBuilder.toString());
        if (bytes.length > packedLength) {
            throw new PackerRuntimeException("Packed bytes with length '" + bytes.length +
                "' is greater than required packedLength '" + packedLength + "'.");
        }
        return bytes;
    }

    /**
     * Unpack for example bytes <b>F9F0</b> to decimal <b>90</b>.
     */
    @Override
    public int unpack(byte[] bytes, int offset) {
        int available = bytes.length - offset;
        if (available < packedLength) {
            throw new PackerRuntimeException("Required packedLength '" + packedLength +
                "' is greater than available bytes[] length '" + available + "'.");
        }
        byte[] tagBytes = new byte[packedLength];
        System.arraycopy(bytes, offset, tagBytes, 0, tagBytes.length);
        String hex = HexService.bytesToHex(tagBytes);
        StringBuilder stringBuilder = new StringBuilder(hex.length() / TWO_CHARS_IN_HEX_BYTE);
        for (int index = 0; index < hex.length(); index += TWO_CHARS_IN_HEX_BYTE) {
            stringBuilder.append(hex.charAt(index + 1));
        }
        return Integer.parseInt(stringBuilder.toString());
    }

    @Override
    public int getPackedLength() {
        return packedLength;
    }
}
