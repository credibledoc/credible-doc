package com.credibledoc.iso8583packer.ebcdic;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Implements EBCDIC {@link BodyPacker}. Strings are converted to and from EBCDIC bytes.
 * The {@link #ISO_88591} charset is used.
 * 
 * // TODO Kyrylo Semenko - example in documentation
 *
 * @author Kyrylo Semenko
 */
public class EbcdicBodyPacker implements BodyPacker {

    /**
     * An instance of this #BodyPacker. Only one needed for the whole system
     */
    public static final EbcdicBodyPacker INSTANCE = new EbcdicBodyPacker();
    private static final Charset ISO_88591 = StandardCharsets.ISO_8859_1;

    /**
     * @param object the data to be packed. Expected String value.
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
        EbcdicService.asciiToEbcdic(data, bytes, offset);
    }

    /**
     * @param sourceData the packed source data.
     * @param offset     the index in sourceData to start unpacking at.
     * @param bytesCount the number of bytes to unpack.
     * @return The String value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T unpack(byte[] sourceData, int offset, int bytesCount) {
        return (T) EbcdicService.ebcdicToAscii(sourceData, offset, bytesCount, ISO_88591);
    }

    /**
     * @param object the String for packing.
     * @return The String length.
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
        return string.length();
    }
}
