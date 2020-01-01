package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.length.LengthPacker;

import java.io.ByteArrayOutputStream;

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
    private static final int MAX_LEN_LENGTH_3_BYTES = 3;
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

    @Override
    public int unpack(byte[] messageBytes, int offset, Integer lenLength) {
        if (lenLength == 0) {
            throw new PackerRuntimeException("Value of lenLength cannot be " + lenLength);
        }
        if (lenLength > MAX_LEN_LENGTH_3_BYTES) {
            throw new PackerRuntimeException("Max expected lenLength is " + MAX_LEN_LENGTH_3_BYTES + ", but found " +
                    lenLength + " bytes");
        }
        /*
            Tag + Length + Value
            1) if Length is 1, send it in hex as one byte
            2) if Length is 2, first send 81 and then a length in hex as one byte
            3) if Length is 3, first send 82 and then a length in hex as two bytes
         */
        int decodedLenLength = lenLength < 3 ? 1 : 2;
        byte[] tagBytes = new byte[decodedLenLength];

        if (lenLength == 1) {
            // tagBytes 1 byte
            System.arraycopy(messageBytes, offset, tagBytes, 0, tagBytes.length);
        } else {
            // tagBytes 2 bytes
            System.arraycopy(messageBytes, offset + 1, tagBytes, 0, tagBytes.length);
        }
        
        String hex = HexService.bytesToHex(tagBytes);
        return Integer.parseInt(hex, RADIX_16);
    }

    @Override
    public byte[] pack(int bodyBytesLength, Integer lenLength) {
        try {
            if (lenLength > MAX_LEN_LENGTH_3_BYTES) {
                throw new PackerRuntimeException("Max expected lenLength is " + MAX_LEN_LENGTH_3_BYTES +
                    ", but found " + lenLength + " bytes");
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            /*
                Tag + Length + Value
                1) když je Length menší než 7F, pošli to v hexa v jednom bajtu
                2) když je Lenght delší než 7F a kratší než FF, pošli napřed 81 a pak délku v hexa v jednom bytu
                3) když je Length delší než FF, pošli napřed 82 a pak délku v hexa ve dvou bajtech
             */
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

    @Override
    public int calculateLenLength(int bodyBytesLength) {
        if (bodyBytesLength <= 127) {
            return 1;
        }
        if (bodyBytesLength <= 255) {
            return 2;
        }
        if (bodyBytesLength <= MAX_LENGTH_65535) {
            return 3;
        }
        throw new PackerRuntimeException("Body bytes length '" + bodyBytesLength +
                "' is greater then '" + MAX_LENGTH_65535 +
                "' bytes");
    }
}
