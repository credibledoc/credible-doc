package com.credibledoc.iso8583packer.body;

/**
 * Implementations of the interface convert Objects into byte arrays and vice versa,
 * see the {@link #pack(Object, byte[], int)} and {@link #unpack(byte[], int, int)} methods.
 * <p>
 * Actual documentation and examples
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/body/body-packer.md">body-packer.md</a>.
 *
 * @author Kyrylo Semenko
 */
public interface BodyPacker {
    /**
     * Converts the data into bytes.
     *
     * @param object the data to be packed.
     * @param bytes  an empty or partially filled bytes.
     * @param offset the index of the first unfilled byte in the bytes array from start packing at.
     */
    void pack(Object object, byte[] bytes, int offset);

    /**
     * Converts the byte array into an Object of T type.
     * The method reverses the {@link #pack(Object, byte[], int)} method.
     *
     * @param sourceData the packed source data.
     * @param offset     the index in sourceData to start unpacking at.
     * @param bytesCount the number of bytes to unpack.
     * @param <T>        the type of the returned data.
     * @return The unpacked data.
     */
    // TODO Kyrylo Semenko - remove bytesCount
    <T> T unpack(byte[] sourceData, int offset, int bytesCount);

    /**
     * @param object the Object for packing.
     * @return The number of bytes required to pack an Object from the argument.
     */
    int getPackedLength(Object object);
}
