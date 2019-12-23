package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.string.StringUtils;
import com.credibledoc.iso8583packer.hex.HexService;

/**
 * // TODO Kyrylo Semenko - examples
 * 
 * @author Kyrylo Semenko
 */
public class BcdLengthPacker implements LengthPacker {
    public static final BcdLengthPacker L = new BcdLengthPacker(1);
    public static final BcdLengthPacker LL = new BcdLengthPacker(2);
    public static final BcdLengthPacker LLL = new BcdLengthPacker(3);
    public static final BcdLengthPacker LLLL = new BcdLengthPacker(4);
    public static final BcdLengthPacker LLLLL = new BcdLengthPacker(5);

    /**
     * How many bytes this subfield occupies in the {@link com.credibledoc.iso8583packer.message.MsgValue} field.
     */
    private int numBytes;

    private BcdLengthPacker(int numBytes) {
        this.numBytes = numBytes;
    }
    
    @Override
    public byte[] pack(int bodyBytesLength, int lenLength) {
        String lenString = Integer.toString(bodyBytesLength);
        String hex = StringUtils.leftPad(lenString, numBytes * 2, '0');
        return HexService.hex2byte(hex);
    }

    @Override
    public int unpack(byte[] messageBytes, int offset, int lenLength) {
        byte[] lenBytes = new byte[lenLength];
        System.arraycopy(messageBytes, offset, lenBytes, 0, lenBytes.length);
        return Integer.parseInt(HexService.hexString(lenBytes));
    }

    @Override
    public int calculateLenLength(byte[] data, int offset) {
        return numBytes;
    }

    @Override
    public int calculateLenLength(int bodyBytesLength) {
        return numBytes;
    }
}
