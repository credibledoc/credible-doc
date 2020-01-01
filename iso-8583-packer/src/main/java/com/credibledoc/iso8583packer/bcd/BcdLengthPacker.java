package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.string.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Packs and unpacks {@link MsgValue#getBodyBytes()} length in the 
 * <a href="https://en.wikipedia.org/wiki/Binary-coded_decimal">BCD</a> format with fixed lenLength.
 * <p>
 * See the 
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/bcd/bcd-length-packer.md">bcd-length-packer.md</a> page.
 *
 * @author Kyrylo Semenko
 */
public class BcdLengthPacker implements LengthPacker {

    private static final char PAD_CHAR_0 = '0';
    
    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, BcdLengthPacker> instances = new ConcurrentHashMap<>();

    /**
     * How many bytes of the {@link MsgValue#getHeaderValue()} field occupies the <b>LEN</b> subfield
     * in a packed state.
     */
    private int numBytes;

    private BcdLengthPacker(int numBytes) {
        this.numBytes = numBytes;
    }

    /**
     * Static factory. Creates and returns singletons.
     * @param numBytes see {@link #numBytes}
     * @return Existing instance from {@link #instances} or a new created instance.
     */
    public static BcdLengthPacker getInstance(int numBytes) {
        instances.computeIfAbsent(numBytes, k -> new BcdLengthPacker(numBytes));
        return instances.get(numBytes);
    }

    @Override
    public byte[] pack(int bodyBytesLength) {
        String lenString = Integer.toString(bodyBytesLength);
        String hex = StringUtils.leftPad(lenString, numBytes * 2, PAD_CHAR_0);
        if (hex.length() > numBytes * 2) {
            throw new PackerRuntimeException("The bodyBytesLength '" + bodyBytesLength +
                "' cannot be packed to '" + numBytes + "' bytes " +
                "because it is longer and '" + Math.round(hex.length() / 2f) + "' bytes is needed for packing " +
                "the '" + hex + "' value.");
        }
        return HexService.hex2byte(hex);
    }

    @Override
    public int unpack(byte[] messageBytes, int offset) {
        byte[] lenBytes = new byte[numBytes];
        System.arraycopy(messageBytes, offset, lenBytes, 0, lenBytes.length);
        return Integer.parseInt(HexService.bytesToHex(lenBytes));
    }

    @Override
    public int calculateLenLength(byte[] data, int offset) {
        return numBytes;
    }

}
