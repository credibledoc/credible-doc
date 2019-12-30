package com.credibledoc.iso8583packer.bitmap;

import com.credibledoc.iso8583packer.header.HeaderValue;

import java.util.BitSet;

/**
 * Packs and unpacks BitSet, see the <a href="https://en.wikipedia.org/wiki/ISO_8583#Bitmaps">Wikipedia</a> description.
 * 
 * @author Kyrylo Semenko
 */
public interface BitmapPacker {
    
    /**
     * @param bitSet the {@link BitSet} for packing
     * @param packedBytesLength bytes number in a packed state
     * @return packed {@link BitSet}
     */
    byte[] pack (BitSet bitSet, int packedBytesLength);

    /**
     * @param headerValue where the unpacked {@link BitSet} will be stored
     * @param bytes the data source
     * @param offset starting offset within the bytes
     * @param packedBytesLength bytes number in a packed state
     * @return number of consumed bytes
     */
    int unpack(HeaderValue headerValue, byte[] bytes, int offset, int packedBytesLength);

}
