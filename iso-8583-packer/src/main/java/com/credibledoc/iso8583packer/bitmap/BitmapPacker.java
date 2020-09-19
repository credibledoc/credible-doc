package com.credibledoc.iso8583packer.bitmap;

import com.credibledoc.iso8583packer.message.MsgValue;

import java.util.BitSet;

/**
 * Packs and unpacks BitSet, see the <a href="https://en.wikipedia.org/wiki/ISO_8583#Bitmaps">Wikipedia</a> description.
 * <p>
 * Actual documentation and examples
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/bitmap/bitmap-packer.md">bitmap-packer.md</a>.
 * 
 * @author Kyrylo Semenko
 */
public interface BitmapPacker {

    /**
     * @param bitSet the {@link BitSet} for packing
     * @return packed {@link BitSet}
     */
    byte[] pack (BitSet bitSet);

    /**
     * @param msgValue where the unpacked {@link BitSet} will be stored
     * @param bytes the data source
     * @param offset starting offset within the bytes
     * @return number of consumed bytes
     */
    int unpack(MsgValue msgValue, byte[] bytes, int offset);

    /**
     * @return The number of the {@link BitSet} bytes in a packed state without the children of the {@link BitmapPacker}.
     */
    int getPackedBytesLength();
}
