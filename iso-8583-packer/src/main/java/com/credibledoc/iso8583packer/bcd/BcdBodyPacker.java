package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

/**
 * Implements BCD {@link BodyPacker} with padding. Numeric Strings (consisting of chars '0'..'9') are converted
 * to and from BCD bytes. Thus, "1234" is converted into 2 bytes: 0x12, 0x34.
 *
 * @author Kyrylo Semenko
 */
public class BcdBodyPacker implements BodyPacker {
    
    /**
     * Adds a 0-nibble to the left if the number of value digits is even.
     */
    public static final BcdBodyPacker LEFT_PADDED_0 = new BcdBodyPacker(true, false);
    
    /**
     * Adds a F-nibble to the right if the number of value digits is even.
     */
    public static final BcdBodyPacker RIGHT_PADDED_F = new BcdBodyPacker(false, true);
    
    /**
     * Adds a F-nibble to the left if the number of value digits is even.
     */
    public static final BcdBodyPacker LEFT_PADDED_F = new BcdBodyPacker(true, true);
    
    private static final int F0_PADDING = 0xF0;
    private static final int PADDING_0F = 0x0F;
    private static final char PADDING_F = 'F';
    private static final char PADDING_0 = '0';

    /**
     * Fill even value at left or right side?
     */
    private boolean leftPadded;

    /**
     * Fill even value with '0' or 'F'?
     */
    private boolean fPadded;

    /**
     * Kept private. Only three instances are possible.
     * @param leftPadded see {@link #leftPadded}
     * @param fPadded see {@link #fPadded}
     */
    private BcdBodyPacker(boolean leftPadded, boolean fPadded) {
        this.leftPadded = leftPadded;
        this.fPadded = fPadded;
    }

    /**
     * @param object the data to be packed. Expected String.
     * @param bytes  an empty or partially filled bytes.
     * @param offset the index of the first unfilled byte in the bytes array from start packing at.
     */
    @Override
    public void pack(Object object, byte[] bytes, int offset) {
        if (object == null) {
            return;
        }

        if (!(object instanceof String)) {
            throw new PackerRuntimeException("Expected String but found " + object.getClass().getName());
        }

        String data = (String) object;

        BcdService.str2bcd(data, leftPadded, bytes, offset);
        int paddedSize = data.length() >> 1;
        if (fPadded && data.length() % 2 == 1) {
            if (leftPadded) {
                bytes[offset] |= (byte) F0_PADDING;
            } else {
                bytes[paddedSize] |= (byte) PADDING_0F;
            }
        }
    }

    /**
     * @param sourceData the packed source data.
     * @param offset     the index in sourceData to start unpacking at.
     * @param bytesCount the number of bytes for unpacking.
     * @param <T>        the String type.
     * @return The String type only
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T unpack(byte[] sourceData, int offset, int bytesCount) {
        String result = BcdService.bcd2str(sourceData, offset, bytesCount * 2, leftPadded);
        
        if (!leftPadded &&
            (result.charAt(result.length() - 1) == PADDING_F || result.charAt(result.length() - 1) == PADDING_0)) {
            
            return (T) result.substring(0, result.length() - 1);
        }
        
        if (leftPadded && result.charAt(0) == PADDING_0) {
            return (T) result.substring(1);
        }
        
        return (T) result;
    }

    /**
     * Each numeric digit is packed into a nibble, so 2 digits per byte, plus the
     * possibility of padding.
     */
    @Override
    public int getPackedLength(Object object) {
        if (object == null) {
            return 0;
        }

        if (!(object instanceof String)) {
            throw new PackerRuntimeException("Expected String but found " + object.getClass().getName());
        }
        
        String string = (String) object;
        
        return (string.length() + 1) / 2;
    }
    
    
}
