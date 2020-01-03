package com.credibledoc.iso8583packer.ebcdic;

import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.string.StringUtils;
import com.credibledoc.iso8583packer.tag.TagPacker;

/**
 * See the {@link #pack(int, int)} and {@link #unpack(byte[], int, int)} methods examples.
 * 
 * @author Kyrylo Semenko
 */
public class EbcdicDecimalTagPacker implements TagPacker {

    /**
     * Single instance.
     */
    private static EbcdicDecimalTagPacker instance;

    /**
     * Only one instance is allowed, see the {@link #getInstance()} method.
     */
    private EbcdicDecimalTagPacker() {
        // empty
    }

    /**
     * @return The {@link #instance} singleton.
     */
    public static EbcdicDecimalTagPacker getInstance() {
        if (instance == null) {
            instance = new EbcdicDecimalTagPacker();
        }
        return instance;
    }
    
    private static final int TWO_CHARS_IN_HEX_BYTE = 2;
    private static final String FILLER_F = "F";

    /**
     * Pack for example decimal <b>90</b> to bytes <b>F9F0</b>.
     */
    @Override
    public byte[] pack(int fieldTag, int tagLength) {
        StringBuilder stringBuilder = new StringBuilder(tagLength * TWO_CHARS_IN_HEX_BYTE);
        String padded = StringUtils.leftPad(Integer.toString(fieldTag), tagLength, '0');
        for (char nextNum : padded.toCharArray()) {
            stringBuilder.append(FILLER_F).append(nextNum);
        }
        return HexService.hex2byte(stringBuilder.toString());
    }

    /**
     * Unpack for example bytes <b>F9F0</b> to decimal <b>90</b>.
     */
    @Override
    public int unpack(byte[] bytes, int offset, int tagLength) {
        byte[] tagBytes = new byte[tagLength];
        System.arraycopy(bytes, offset, tagBytes, 0, tagBytes.length);
        String hex = HexService.bytesToHex(tagBytes);
        StringBuilder stringBuilder = new StringBuilder(hex.length() / TWO_CHARS_IN_HEX_BYTE);
        for (int index = 0; index <= tagBytes.length; index += TWO_CHARS_IN_HEX_BYTE) {
            stringBuilder.append(hex.charAt(index + 1));
        }
        return Integer.parseInt(stringBuilder.toString());
    }
}
