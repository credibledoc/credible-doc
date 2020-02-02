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
public class AsciiIntTagPacker implements TagPacker {

    private static final Charset ISO_88591 = StandardCharsets.ISO_8859_1;
    
    /**
     * Haw many bytes the TAG occupies in a packed state.
     */
    private int packedLength;


    /**
     * Contains created instances. Each instance is Singleton.
     */
    private static Map<Integer, AsciiIntTagPacker> instances = new ConcurrentHashMap<>();


    /**
     * Only one instance is allowed, see the {@link #getInstance(int)} method.
     * @param packedLength the {@link MsgValue#getTagBytes()} length.
     */
    private AsciiIntTagPacker(int packedLength) {
        this.packedLength = packedLength;
    }

    /**
     * @param packedLength the {@link MsgValue#getTagBytes()} length.
     * @return The singleton instance from the {@link #instances} map.
     */
    public static AsciiIntTagPacker getInstance(int packedLength) {
        instances.computeIfAbsent(packedLength, k -> new AsciiIntTagPacker(packedLength));
        return instances.get(packedLength);
    }
    
    @Override
    public byte[] pack(Object object) {
        if (object == null) {
            return new byte[0];
        }
        if (!(object instanceof Integer)) {
            throw new PackerRuntimeException("Expected Integer but found " + object.getClass().getName());
        }
        Integer data = (Integer) object;
        String string = Integer.toString(data);
        String dataString = StringUtils.leftPad(string, packedLength, '0');
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
    public Integer unpack(byte[] sourceData, int offset) {
        int available = sourceData.length - offset;
        if (packedLength > available) {
            throw new PackerRuntimeException("Required bytes length '" + packedLength +
                    "' is greater than available sourceData length '" + available + "'");
        }
        byte[] ret = new byte[packedLength];
        System.arraycopy(sourceData, offset, ret, 0, packedLength);
        String string = new String(ret, ISO_88591);
        return Integer.valueOf(string);
    }

    @Override
    public int getPackedLength() {
        return packedLength;
    }
}
