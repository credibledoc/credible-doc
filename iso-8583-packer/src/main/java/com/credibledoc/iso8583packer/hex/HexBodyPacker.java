package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

import java.util.Arrays;

/**
 * Implements {@link BodyPacker} where hexadecimal chars '0'..'9', 'A'..'F' converted to and from bytes. Thus, "12CD" is
 * converted to 2 bytes: 0x12, 0xCD.
 * <p>
 * More examples
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/hex/hex-body-packer.md">hex-body-packer.md</a>
 * 
 * @author Kyrylo Semenko
 */
public class HexBodyPacker implements BodyPacker {

    /**
     * Single instance.
     */
    private static HexBodyPacker instance;

    /**
     * Only one instance is allowed, see the {@link #getInstance()} method.
     */
    private HexBodyPacker() {
        // empty
    }

    /**
     * @return The {@link #instance} singleton.
     */
    public static HexBodyPacker getInstance() {
        if (instance == null) {
            instance = new HexBodyPacker();
        }
        return instance;
    }

	/**
	 * Expected type of object is String, for example FFFF (hex of two bytes).
	 * @param object the String data to be packed.
	 * @param bytes  an empty or partially filled bytes.
	 * @param offset the index of the first unfilled byte in the bytes array from start packing at.
	 */
	@Override
	public void pack(Object object, byte[] bytes, int offset) {
		if (object == null) {
			return;
		}
		if (!(object instanceof String)) {
			throw new PackerRuntimeException("Expected String but found " + object.getClass().getSimpleName());
		}
		String data = (String) object;
        int packedLength = getPackedLength(data);
        int available = bytes.length - offset;
        if (available < packedLength) {
            throw new PackerRuntimeException("Available bytes number '" + available +
                "' is less than required packedLength '" + packedLength + "' of the data object.");
        }
		byte[] bytesFromHex = HexService.hex2byte(data);
		System.arraycopy(bytesFromHex, 0, bytes, offset, bytesFromHex.length);
	}

	@Override
	@SuppressWarnings("unchecked")
	public String unpack(byte[] rawData, int offset, int bytesCount) {
	    int available = rawData.length - offset;
        if (available < bytesCount) {
            throw new PackerRuntimeException("Available bytes number '" + available +
                "' is less than required bytesCount '" + bytesCount + "' from the parameter.");
        }
        byte[] bytes = Arrays.copyOfRange(rawData, offset, offset + bytesCount);
        return HexService.bytesToHex(bytes);
	}

	@Override
	public int getPackedLength(Object object) {
		if (object == null) {
			return 0;
		}
		if (!(object instanceof String)) {
			throw new PackerRuntimeException("Expected String but found " + object.getClass().getSimpleName());
		}
		String string = (String) object;
		return string.length() / 2;
	}
}
