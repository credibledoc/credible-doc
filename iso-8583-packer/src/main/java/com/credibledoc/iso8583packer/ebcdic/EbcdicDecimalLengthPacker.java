package com.credibledoc.iso8583packer.ebcdic;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.string.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link LengthPacker} with fixed length <b>length</b> subfield
 * in <a href="https://en.wikipedia.org/wiki/EBCDIC">EBCDIC</a> format.
 * <p>
 * See examples in the {@link #pack(int)} and {@link #unpack(byte[], int)} methods description.
 *
 * @author Kyrylo Semenko
 */
public class EbcdicDecimalLengthPacker implements LengthPacker {
    private static final String FILLER_F = "F";
    private static final char PAD_CHAR_0 = '0';

    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, EbcdicDecimalLengthPacker> instances = new ConcurrentHashMap<>();
    
    /**
     * How many bytes the {@link MsgValue#getTagBytes()} (optional) and {@link MsgValue#getBodyBytes()} fields
     * occupy in a packed state.
     */
    private int numBytes;
    
    private EbcdicDecimalLengthPacker(int numBytes) {
        this.numBytes = numBytes;
    }

    /**
     * Static factory. Creates and returns singletons.
     * @param numBytes see {@link #numBytes}
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static EbcdicDecimalLengthPacker getInstance(int numBytes) {
        instances.computeIfAbsent(numBytes, k -> new EbcdicDecimalLengthPacker(numBytes));
        return instances.get(numBytes);
    }

    /**
     * Convert for example the <b>154</b> decimal int to <b>F1F5F4</b> bytes.
     */
    @Override
    public byte[] pack(int bodyBytesLength) {
        StringBuilder stringBuilder = new StringBuilder(numBytes * 2);
        String lenString = StringUtils.leftPad(Integer.toString(bodyBytesLength), numBytes, PAD_CHAR_0);
        for (char character : lenString.toCharArray()) {
            stringBuilder.append(FILLER_F).append(character);
        }
        if (stringBuilder.length() > numBytes * 2) {
            throw new PackerRuntimeException("The bodyBytesLength '" + bodyBytesLength +
                "' cannot be packed to '" + numBytes + "' bytes " +
                "because it is longer and '" + Math.round(stringBuilder.length() / 2f) + "' bytes is needed for packing " +
                "the '" + stringBuilder.toString() + "' value.");
        }
        return HexService.hex2byte(stringBuilder.toString());
    }

    /**
     * Convert for example the <b>F1F5F4</b> bytes to decimal int <b>154</b>.
     */
    @Override
    public int unpack(byte[] messageBytes, int offset) {
        byte[] lenBytes = Arrays.copyOfRange(messageBytes, offset, offset + numBytes);
        String hex = HexService.bytesToHex(lenBytes);
        String withoutF = hex.replace(FILLER_F, "");
        return Integer.parseInt(withoutF);
    }

    @Override
    public int calculateLenLength(byte[] data, int offset) {
        return numBytes;
    }

}
