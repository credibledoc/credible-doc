package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.length.LengthPacker;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Variable length {@link LengthPacker} for <a href="https://en.wikipedia.org/wiki/Type-length-value">TLV</a>
 * structures.
 * 
 * See example on the <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/hex/hex-length-packer.md">hex-length-packer.md</a> page.
 * 
 * @author Kyrylo Semenko
 */
public class HexLengthPacker implements LengthPacker {
    private static final int MAX_LENGTH_65535 = 65535;
    private static final String FLAG_TWO_BYTES_82 = "82";
    private static final byte FLAG_TWO_BYTES_82_AS_BYTE = HexService.hex2byte(FLAG_TWO_BYTES_82)[0];
    private static final String FLAG_ONE_BYTE_81 = "81";
    private static final byte FLAG_ONE_BYTE_81_AS_BYTE = HexService.hex2byte(FLAG_ONE_BYTE_81)[0];
    private static final int RADIX_16 = 16;

    /**
     * Single instance.
     */
    private static HexLengthPacker instance;

    /**
     * @return The {@link #instance} singleton.
     */
    public static HexLengthPacker getInstance() {
        if (instance == null) {
            instance = new HexLengthPacker();
        }
        return instance;
    }

    /**
     * <pre>
     * Tag + Length + Value
     * 1) if Length less than 7F, send it in hex as one byte
     * 2) if length is greater than 7F and less than FF,, first send 81 and then a length in hex as one byte
     * 3) if length is greater than FF, first send 82 and then a length in hex as two bytes
     * </pre>
     */
    @Override
    public int unpack(byte[] messageBytes, int offset) {
        byte firstByte = messageBytes[offset];
        byte[] value;
        if (firstByte == FLAG_ONE_BYTE_81_AS_BYTE) {
            value = Arrays.copyOfRange(messageBytes, offset + 1, offset + 1 + 1);
        } else if (firstByte == FLAG_TWO_BYTES_82_AS_BYTE) {
            value = Arrays.copyOfRange(messageBytes, offset + 1, offset + 1 + 2);
        } else {
            value = new byte[]{firstByte};
        }
        
        String hex = HexService.bytesToHex(value);
        return Integer.parseInt(hex, RADIX_16);
    }

    /**
     * <pre>
     * Tag + Length + Value
     * 1) if Length less than 7F, send it in hex as one byte
     * 2) if length is greater than 7F and less than FF,, first send 81 and then a length in hex as one byte
     * 3) if length is greater than FF, first send 82 and then a length in hex as two bytes
     * </pre>
     */
    @Override
    public byte[] pack(int bodyBytesLength) {
        if (bodyBytesLength > MAX_LENGTH_65535) {
            throw new PackerRuntimeException("Body bytes length '" + bodyBytesLength +
                "' is greater than '" + MAX_LENGTH_65535 + "' bytes");
        }
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            if (bodyBytesLength > 255) {
                result.write(FLAG_TWO_BYTES_82_AS_BYTE);
            } else if (bodyBytesLength > 127) {
                result.write(FLAG_ONE_BYTE_81_AS_BYTE);
            }
            String hexLength = Integer.toHexString(bodyBytesLength);
            byte[] bytesLength = HexService.hex2byte(hexLength);
            result.write(bytesLength);
            return result.toByteArray();
        } catch (Exception e) {
            throw new PackerRuntimeException(e);
        }
    }

    @Override
    public int calculateLenLength(byte[] data, int offset) {
        byte firstByte = data[offset];
        if (firstByte == FLAG_TWO_BYTES_82_AS_BYTE) {
            return 3;
        }
        if (firstByte == FLAG_ONE_BYTE_81_AS_BYTE) {
            return 2;
        }
        return 1;
    }

}
