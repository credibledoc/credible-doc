package com.credibledoc.iso8583packer.bcd;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.string.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements BCD {@link BodyPacker} with padding. Integers are converted
 * to and from BCD bytes. Thus, "1234" is converted into 2 bytes: 0x12, 0x34.
 * <p>
 * More examples
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/bcd/bcd-int-body-packer.md">bcd-int-body-packer.md</a>
 *
 * @author Kyrylo Semenko
 */
public class BcdIntBodyPacker implements BodyPacker {

    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, BcdIntBodyPacker> instances = new ConcurrentHashMap<>();

    /**
     * How many bytes the {@link MsgValue#getBodyBytes()} field occupies in a packed state.
     */
    private int numBytes;
    
    public static final char FILLER_0 = '0';

    private BcdIntBodyPacker(int numBytes) {
        this.numBytes = numBytes;
    }

    /**
     * Adds a 0-nibble to the left if the number of value digits in unpacked state is odd
     * or shorter than required bytes number.
     * @param numBytes see the {@link #numBytes} value description.
     * 
     * @return Existing instance from the {@link #instances} map or a newly created instance.
     */
    public static BcdIntBodyPacker getInstance(Integer numBytes) {
        if (numBytes == null) {
            throw new PackerRuntimeException("Number of bytes in a packed state cannot be 'null'");
        }
        instances.computeIfAbsent(numBytes, k -> new BcdIntBodyPacker(numBytes));
        return instances.get(numBytes);
    }

    /**
     * @param object the data to be packed. Expected Integer.
     * @param bytes  an empty or partially filled bytes.
     * @param offset the index of the first unfilled byte in the bytes array from start packing at.
     */
    @Override
    public void pack(Object object, byte[] bytes, int offset) {
        if (object == null) {
            return;
        }

        if (!(object instanceof Integer)) {
            throw new PackerRuntimeException("Expected Integer but found " + object.getClass().getName());
        }

        String value = Integer.toString((Integer) object);
        if (value.length() > numBytes * 2) {
            throw new PackerRuntimeException("Length '" + value.length() + "' of value '" + value +
                "' is greater than the packer is able to pack because it has defined numBytes '" + numBytes + "'.");
        }
        String leftPadded = StringUtils.leftPad(value, numBytes * 2, FILLER_0);

        BcdService.str2bcd(leftPadded, false, bytes, offset);
    }

    /**
     * @param sourceData the packed source data.
     * @param offset     the index in sourceData to start unpacking at.
     * @param bytesCount the number of bytes for unpacking.
     * @return The Integer representation of the data.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Integer unpack(byte[] sourceData, int offset, int bytesCount) {
        String result = BcdService.bcd2str(sourceData, offset, bytesCount * 2, false);
        return Integer.valueOf(result);
    }

    /**
     * @return The {@link #numBytes} value.
     */
    @Override
    public int getPackedLength(Object object) {
        return numBytes;
    }
    
}
