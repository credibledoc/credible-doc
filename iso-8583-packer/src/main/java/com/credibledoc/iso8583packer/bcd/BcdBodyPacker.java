package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

/**
 * Implements BCD {@link BodyPacker} with padding. Numeric Strings (consisting of chars '0'..'9') are converted
 * to and from BCD bytes. Thus, "1234" is converted into 2 bytes: 0x12, 0x34.
 * <p>
 * More examples
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/bcd/bcd-body-packer.md">bcd-body-packer.md</a>
 *
 * @author Kyrylo Semenko
 */
public class BcdBodyPacker implements BodyPacker {
    
    /**
     * See {@link #leftPadding0()}.
     */
    private static BcdBodyPacker leftPadding0Instance = null;
    
    /**
     * See {@link #rightPaddingF()}.
     */
    private static BcdBodyPacker rightPaddingFInstance = null;
    
    /**
     * See {@link #leftPaddingF()}.
     */
    private static BcdBodyPacker leftPaddingFInstance = null;
    
    /**
     * See {@link #noPadding()}.
     */
    private static BcdBodyPacker noPaddingInstance = null;
    
    private static final int F0_FILLER = 0xF0;
    private static final int FILLER_0F = 0x0F;
    public static final char FILLER_F = 'F';
    public static final char FILLER_0 = '0';

    /**
     * Packing and unpacking without padding. Throw an exception if the number of value digits in unpacked state is odd.
     * @return Existing instance of {@link #noPaddingInstance} or created new instance.
     */
    public static BcdBodyPacker noPadding() {
        if (noPaddingInstance == null) {
            noPaddingInstance = new BcdBodyPacker();
        }
        return noPaddingInstance;
    }

    /**
     * Adds a F-nibble to the left if the number of value digits in unpacked state is odd.
     * @return Existing instance of {@link #leftPaddingFInstance} or created new instance.
     */
    public static BcdBodyPacker leftPaddingF() {
        if (leftPaddingFInstance == null) {
            leftPaddingFInstance = new BcdBodyPacker();
        }
        return leftPaddingFInstance;
    }

    /**
     * Adds a F-nibble to the right if the number of value digits in unpacked state is odd.
     * @return Existing instance of {@link #rightPaddingFInstance} or created new instance.
     */
    public static BcdBodyPacker rightPaddingF() {
        if (rightPaddingFInstance == null) {
            rightPaddingFInstance = new BcdBodyPacker();
        }
        return rightPaddingFInstance;
    }

    /**
     * Adds a 0-nibble to the left if the number of value digits in unpacked state is odd.
     * @return Existing instance of {@link #leftPadding0Instance} or created new instance.
     */
    public static BcdBodyPacker leftPadding0() {
        if (leftPadding0Instance == null) {
            leftPadding0Instance = new BcdBodyPacker();
        }
        return leftPadding0Instance;
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

        String value = (String) object;
        if (this == noPaddingInstance && value.length() % 2 != 0) {
            throw new PackerRuntimeException("Odd value length is not allowed with 'noPadding()' instance. " +
                "Value '" + value + "' has odd length '" + value.length() + "'. " +
                "Please use even length value " +
                "or another instance of the " + BcdBodyPacker.class.getSimpleName() + " class.");
        }
        boolean leftPadded = leftPadding0Instance == this || leftPaddingFInstance == this;
        boolean fPadded = leftPaddingFInstance == this || rightPaddingFInstance == this;

        BcdService.str2bcd(value, leftPadded, bytes, offset);
        int paddedSize = value.length() >> 1;
        if (fPadded && value.length() % 2 == 1) {
            if (leftPadded) {
                bytes[offset] |= (byte) F0_FILLER;
            } else {
                bytes[paddedSize] |= (byte) FILLER_0F;
            }
        }
    }

    /**
     * @param sourceData the packed source data.
     * @param offset     the index in sourceData to start unpacking at.
     * @param bytesCount the number of bytes for unpacking.
     * @return The String representation of the data.
     */
    @Override
    @SuppressWarnings("unchecked")
    public String unpack(byte[] sourceData, int offset, int bytesCount) {
        boolean leftPadded = leftPadding0Instance == this || leftPaddingFInstance == this;
        String result = BcdService.bcd2str(sourceData, offset, bytesCount * 2, leftPadded);
        
        if (rightPaddingFInstance == this && result.charAt(result.length() - 1) == FILLER_F) {
            return result.substring(0, result.length() - 1);
        }
        
        if (leftPadded && (result.charAt(0) == FILLER_0 || result.charAt(0) == FILLER_F)) {
            return result.substring(1);
        }
        
        return result;
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
