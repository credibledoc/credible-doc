package com.credibledoc.iso8583packer.ebcdic;

import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Implements EBCDIC {@link BodyPacker}. Strings are converted to and from EBCDIC bytes.
 * The {@link #ISO_88591} charset is used.
 * <p>
 * More examples
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/ebcdic/ebcdic-body-packer.md">ebcdic-body-packer.md</a>
 *
 * @author Kyrylo Semenko
 */
public class EbcdicBodyPacker implements BodyPacker {

    private static final Charset ISO_88591 = StandardCharsets.ISO_8859_1;

    /**
     * Single instance.
     */
    private static EbcdicBodyPacker instance;

    /**
     * Only one instance is allowed, see the {@link #getInstance()} method.
     */
    private EbcdicBodyPacker() {
        // empty
    }

    /**
     * @return The {@link #instance} singleton.
     */
    public static EbcdicBodyPacker getInstance() {
        if (instance == null) {
            instance = new EbcdicBodyPacker();
        }
        return instance;
    }

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
    public String unpack(byte[] sourceData, int offset, int bytesCount) {
        int available = sourceData.length - offset;
        if (bytesCount > available) {
            throw new PackerRuntimeException("Available number of bytes '" + available +
                "' in the sourceData[] array is less than required number of bytes '" + bytesCount +
                "' in bytesCount parameter.");
        }
        return EbcdicService.ebcdicToAscii(sourceData, offset, bytesCount, ISO_88591);
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
