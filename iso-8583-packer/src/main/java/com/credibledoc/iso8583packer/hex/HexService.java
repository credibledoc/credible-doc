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
     * converts a byte array to hex string 
     * (suitable for dumps and ASCII packaging of Binary fields
     * @param b - byte array
     * @return String representation
     */
    public static String hexString(byte[] b) {
        StringBuilder d = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            d.append(hexStrings[(int) aB & 0xFF]);
        }
        return d.toString();
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
     * Converts a hex string into a byte array
     *
     * @param s source string (with Hex representation)
     * @return byte array
     */
    public static byte[] hex2byte(String s) {
        if (s.length() % 2 == 0) {
            return hex2byte(s.getBytes(), 0, s.length() >> 1);
        } else {
            // Padding left zero to make it even size #Bug raised by tommy
            return hex2byte("0" + s);
        }
    }

    public static String hexString(byte[] array, int i, int len) {
        StringBuilder sb = new StringBuilder(len * 2);

        for(int x = i; x < len + i; ++x) {
            int nibble1 = (array[x] & 240) / 16;
            sb.append(hexStrings[nibble1]);
            int nibble2 = array[x] & 15;
            sb.append(hexStrings[nibble2]);
        }

        return sb.toString();
    }
}
