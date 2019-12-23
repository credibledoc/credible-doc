package com.credibledoc.iso8583packer.hex;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

import java.util.Arrays;

/**
 * Implements {@link BodyPacker}. Hex {@link String}s consisting of
 * chars '0'..'9' and 'A'..'F' are converted to and from bytes. Thus, "12CD" is
 * converted into 2 bytes: 0x12, 0xCD.
 * 
 * @author sh
 * @author Kyrylo Semenko
 */
public class HexBodyPacker implements BodyPacker {

	public static final HexBodyPacker INSTANCE = new HexBodyPacker();

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
		byte[] bytesFromHex = HexService.hex2byte(data);
		System.arraycopy(bytesFromHex, 0, bytes, offset, bytesFromHex.length);
	}

	@Override
	@SuppressWarnings("unchecked")
	public String unpack(byte[] rawData, int offset, int bytesCount) {
        byte[] bytes = Arrays.copyOfRange(rawData, offset, offset + bytesCount);
        return HexService.hexString(bytes);
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
