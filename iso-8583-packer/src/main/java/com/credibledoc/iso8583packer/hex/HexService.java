package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

/**
 * Static service for conversions of <a href="https://en.wikipedia.org/wiki/Hexadecimal">HEX</a> data format.
 * 
 * @author Kyrylo Semenko
 */
public class HexService {

    private static final String[] hexStrings;

    static {
        hexStrings = new String[256];
        for (int i = 0; i < 256; i++ ) {
            StringBuilder d = new StringBuilder(2);
            char ch = Character.forDigit((byte)i >> 4 & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            ch = Character.forDigit((byte)i & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            HexService.hexStrings[i] = d.toString();
        }

    }

    private HexService() {
        throw new PackerRuntimeException("Please do not instantiate the static helper.");
    }

    /**
     * Convert a byte array to a hex string (suitable for dumps and ASCII packaging of Binary fields)
     *
     * @param bytes a byte array
     * @return String representation
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
        for (byte aB : bytes) {
            stringBuilder.append(hexStrings[(int) aB & 0xFF]);
        }
        return stringBuilder.toString();
    }

    /**
     * Call the {@link #bytesToHex(byte[])} method and optionally interlace single bytes with some separator (divider).
     * @param bytes a byte array
     * @param separator the single bytes will be interlaced with separator (divider), for example <b>09 FF 0F</b>
     * @return String representation
     */
    public static String bytesToHex(byte[] bytes, String separator) {
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            byte nextByte = bytes[i];
            stringBuilder.append(hexStrings[(int) nextByte & 0xFF]);
            if (i < bytes.length - 1) {
                stringBuilder.append(separator);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * @param b      source byte array
     * @param offset starting offset
     * @param len    number of bytes in destination (processes len*2)
     * @return byte[len]
     */
    public static byte[] hex2byte(byte[] b, int offset, int len) {
        byte[] d = new byte[len];
        for (int i = 0; i < len * 2; i++) {
            int shift = i % 2 == 1 ? 0 : 4;
            d[i >> 1] |= Character.digit((char) b[offset + i], 16) << shift;
        }
        return d;
    }

    /**
     * Convert a hex string into a byte array.
     *
     * @param hexStringOfBytes source string (with Hex representation)
     * @return Byte array
     */
    public static byte[] hex2byte(String hexStringOfBytes) {
        hexStringOfBytes = hexStringOfBytes.replaceAll("\\s+","");
        if (hexStringOfBytes.length() % 2 == 0) {
            return hex2byte(hexStringOfBytes.getBytes(), 0, hexStringOfBytes.length() >> 1);
        } else {
            // Padding left zero to make it even size #Bug raised by tommy
            return hex2byte("0" + hexStringOfBytes);
        }
    }

    /**
     * Delete defined separators before the conversion.
     * Then call the {@link #hex2byte(String)} method.
     *
     * @param hexStringOfBytes source string (with Hex representation)
     * @param separatorRegex will be deleted. For deletion of spaces for example, use the <b>\\s+</b> regex.
     * @return Byte array
     */
    public static byte[] hex2byte(String hexStringOfBytes, String separatorRegex) {
        hexStringOfBytes = hexStringOfBytes.replaceAll(separatorRegex,"");
        return hex2byte(hexStringOfBytes);
    }

}
