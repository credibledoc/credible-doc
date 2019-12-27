package com.credibledoc.iso8583packer.ebcdic;

import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.string.StringUtils;

import java.util.Arrays;

/**
 * See examples in the {@link #pack(int, int)} and {@link #unpack(byte[], int, int)} methods description.
 * 
 * @author Kyrylo Semenko
 */
public class EbcdicDecimalLengthPacker implements LengthPacker {
    public static final EbcdicDecimalLengthPacker L = new EbcdicDecimalLengthPacker(1);
    public static final EbcdicDecimalLengthPacker LL = new EbcdicDecimalLengthPacker(2);
    public static final EbcdicDecimalLengthPacker LLL = new EbcdicDecimalLengthPacker(3);
    public static final EbcdicDecimalLengthPacker LLLL = new EbcdicDecimalLengthPacker(4);
    private static final String FILLER_F = "F";

    /**
     * How many bytes occupies the length sub-field in a packed message.
     */
    private int lenLength;
    
    private EbcdicDecimalLengthPacker(int lenLength) {
        this.lenLength = lenLength;
    }

    /**
     * Convert for example the <b>154</b> decimal int to <b>F1F5F4</b> bytes.
     */
    @Override
    public byte[] pack(int bodyBytesLength, int lenLength) {
        StringBuilder stringBuilder = new StringBuilder(lenLength * 2);
        String lenString = StringUtils.leftPad(Integer.toString(bodyBytesLength), lenLength, '0');
        for (char character : lenString.toCharArray()) {
            stringBuilder.append(FILLER_F).append(character);
        }
        return HexService.hex2byte(stringBuilder.toString());
    }

    /**
     * Convert for example the <b>F1F5F4</b> bytes to decimal int <b>154</b>
     */
    @Override
    public int unpack(byte[] messageBytes, int offset, int lenLength) {
        byte[] lenBytes = Arrays.copyOfRange(messageBytes, offset, offset + lenLength);
        String hex = HexService.bytesToHex(lenBytes);
        String withoutF = hex.replace(FILLER_F, "");
        return Integer.parseInt(withoutF);
    }

    @Override
    public int calculateLenLength(byte[] data, int offset) {
        return lenLength;
    }

    @Override
    public int calculateLenLength(int bodyBytesLength) {
        return lenLength;
    }
}
