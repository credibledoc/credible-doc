package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.string.StringUtils;

/**
 * Static service. Converts <a href="https://en.wikipedia.org/wiki/Binary-coded_decimal">BCD</a> data.
 *
 * @author apr@jpos.org
 * @author Hani S. Kirollos
 * @author Alwyn Schoeman
 * @author Kyrylo Semenko
 */
public class BcdService {

    private BcdService() {
        throw new PackerRuntimeException("Please do not instantiate this static helper.");
    }

    /**
     * converts to BCD
     *
     * @param numberString       the number
     * @param padLeft flag indicating left/right padding
     * @param bytes       The byte array to copy into.
     * @param offset  Where to start copying into.
     * @return BCD representation of the number
     */
    static byte[] str2bcd(String numberString, boolean padLeft, byte[] bytes, int offset) {
        int len = numberString.length();
        int start = (((len & 1) == 1) && padLeft) ? 1 : 0;
        for (int i = start; i < len + start; i++) {
            bytes[offset + (i >> 1)] |= (numberString.charAt(i - start) - '0') << ((i & 1) == 1 ? 0 : 4);
        }
        return bytes;
    }
    
    /**
     * converts to BCD
     * @param s the number
     * @param padLeft flag indicating left/right padding
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft) {
        int len = s.length();
        byte[] d = new byte[len + 1 >> 1];
        return str2bcd(s, padLeft, d, 0);
    }
    
    /**
     * converts to BCD
     * @param s the number
     * @param padLeft flag indicating left/right padding
     * @param fill fill value
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft, byte fill) {
        int len = s.length();
        byte[] d = new byte[len + 1 >> 1];
        if (d.length > 0) {
            if (padLeft) {
                d[0] = (byte) ((fill & 0xF) << 4);
            }
            int start = (len & 1) == 1 && padLeft ? 1 : 0;
            int i;
            for (i = start; i < len + start; i++) {
                d[i >> 1] |= s.charAt(i - start) - '0' << ((i & 1) == 1 ? 0 : 4);
            }
            if ((i & 1) == 1) {
                d[i >> 1] |= fill & 0xF;
            }
        }
        return d;
    }

    /**
     * converts a BCD representation of a number to a String
     *
     * @param b       BCD representation
     * @param offset  starting offset
     * @param len     BCD field len
     * @param padLeft was padLeft packed?
     * @return the String representation of the number
     */
    static String bcd2str(byte[] b, int offset, int len, boolean padLeft) {
        StringBuilder d = new StringBuilder(len);
        int start = (((len & 1) == 1) && padLeft) ? 1 : 0;
        for (int i = start; i < len + start; i++) {
            int shift = ((i & 1) == 1 ? 0 : 4);
            char c = Character.forDigit(((b[offset + (i >> 1)] >> shift) & 0x0F), 16);
            if (c == 'd') {
                c = '=';
            }
            d.append(Character.toUpperCase(c));
        }
        return d.toString();
    }

    public static void validateIsStringBcdNumber(MsgValue msgValue) {
        Object value = msgValue.getBodyValue();
        if (value != null) {
            if (!(value instanceof String)) {
                throw new PackerRuntimeException("Expected String but found '" + value.getClass().getSimpleName() +
                    "'. Value: '" + value + "'");
            }
            String string = (String) value;
            if (!StringUtils.isNumeric(string)) {
                throw new PackerRuntimeException("Expected numeric string because the '" + BcdBodyPacker.class.getSimpleName() +
                    "' is used, but found '" + string + "'");
            }
        }
    }
}
