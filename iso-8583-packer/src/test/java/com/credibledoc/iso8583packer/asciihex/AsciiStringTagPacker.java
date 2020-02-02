package com.credibledoc.iso8583packer.asciihex;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.string.StringUtils;
import com.credibledoc.iso8583packer.tag.TagPacker;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO Kyrylo Semenko - rename asciihex to hex and create documentation.
public class AsciiStringTagPacker implements TagPacker {

    private static Charset CHARSET = StandardCharsets.UTF_8;
    
    /**
     * Haw many bytes the TAG occupies in a packed state.
     */
    private int packedLength;

    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, AsciiStringTagPacker> instances = new ConcurrentHashMap<>();

    /**
     * Only one instance is allowed, see the {@link #getInstance(int)} method.
     * @param packedLength the {@link MsgValue#getTagBytes()} length.
     */
    private AsciiStringTagPacker(int packedLength) {
        this.packedLength = packedLength;
    }

    /**
     * @param packedLength the {@link MsgValue#getTagBytes()} length.
     * @return The singleton instance from the {@link #instances} map.
     */
    public static AsciiStringTagPacker getInstance(int packedLength) {
        instances.computeIfAbsent(packedLength, k -> new AsciiStringTagPacker(packedLength));
        return instances.get(packedLength);
    }
    
    @Override
    public byte[] pack(Object object) {
        if (object == null) {
            return new byte[0];
        }
        if (!(object instanceof String)) {
            throw new PackerRuntimeException("Expected String but found " + object.getClass().getName());
        }
        String data = (String) object;
        String dataString = StringUtils.leftPad(data, packedLength, '0');
        if (dataString.length() > packedLength) {
            throw new PackerRuntimeException("Byte array available length '" + packedLength +
                    "' is less than required data length '" + dataString.length() + "'");
        }
        byte[] bytes = new byte[packedLength];
        for (int i = dataString.length() - 1; i >= 0; i--) {
            bytes[i] = (byte) dataString.charAt(i);
        }
        return bytes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String unpack(byte[] sourceData, int offset) {
        int available = sourceData.length - offset;
        if (packedLength > available) {
            throw new PackerRuntimeException("Required bytes length '" + packedLength +
                    "' is greater than available sourceData length '" + available + "'");
        }
        byte[] ret = new byte[packedLength];
        System.arraycopy(sourceData, offset, ret, 0, packedLength);
        return new String(ret, CHARSET);
    }

    @Override
    public int getPackedLength() {
        return packedLength;
    }
}
