package com.credibledoc.iso8583packer.asciihex;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.string.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Packs and unpacks {@link MsgValue#getBodyBytes()} length in the ASCII format with fixed lenLength, for example
 * three bytes 30 32 34 decoded as decimal String 024 and int 24.
 * <p>
 * See the 
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/ascii/ascii-length-packer.md">ascii-length-packer.md</a> page.
 *
 * @author Kyrylo Semenko
 */
public class AsciiLengthPacker implements LengthPacker {

    private static final Charset ISO_88591 = StandardCharsets.ISO_8859_1;

    private static final char PAD_CHAR_0 = '0';
    
    /**
     * Contains created instances. Each instance is a Singleton.
     */
    private static Map<Integer, AsciiLengthPacker> instances = new ConcurrentHashMap<>();

    /**
     * How many bytes the {@link MsgValue#getTagBytes()} (optional) and {@link MsgValue#getBodyBytes()} fields
     * occupy in a packed state.
     */
    private int numBytes;

    private AsciiLengthPacker(int numBytes) {
        this.numBytes = numBytes;
    }

    /**
     * Static factory. Creates and returns singletons.
     * @param numBytes see {@link #numBytes}
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static AsciiLengthPacker getInstance(int numBytes) {
        instances.computeIfAbsent(numBytes, k -> new AsciiLengthPacker(numBytes));
        return instances.get(numBytes);
    }

    @Override
    public byte[] pack(int bodyBytesLength) {
        String lenString = Integer.toString(bodyBytesLength);
        if (lenString.length() > numBytes) {
            throw new PackerRuntimeException("Cannot pack bodyBytesLength '" + bodyBytesLength +
                "' to a byte array with length '" + numBytes +
                "' bytes because the value required '" + lenString.length() + "' bytes for packing.");
        }
        String formattedLength = StringUtils.leftPad(lenString, numBytes, PAD_CHAR_0);
        byte[] bytes = new byte[numBytes];
        for (int i = 0; i < formattedLength.length(); i++) {
            bytes[i] = (byte) formattedLength.charAt(i);
        }
        return bytes;
    }

    @Override
    public int unpack(byte[] messageBytes, int offset) {
        int availableBytes = messageBytes.length - offset;
        if (availableBytes < numBytes) {
            throw new PackerRuntimeException("Required bytes length '" + numBytes +
                "' is greater than available sourceData length '" + availableBytes + "'");
        }
        byte[] lenBytes = new byte[numBytes];
        System.arraycopy(messageBytes, offset, lenBytes, 0, numBytes);
        String number = new String(lenBytes, ISO_88591);
        return Integer.parseInt(number);
    }

    @Override
    public int calculateLenLength(byte[] data, int offset) {
        return numBytes;
    }

}
