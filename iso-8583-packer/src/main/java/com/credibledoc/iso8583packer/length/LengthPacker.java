package com.credibledoc.iso8583packer.length;

import com.credibledoc.iso8583packer.header.HeaderValue;
import com.credibledoc.iso8583packer.hex.HexLengthPacker;
import com.credibledoc.iso8583packer.message.MsgValue;

/**
 * See the <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/length/length-packer.md">length-packer.md</a> page.
 * <p>
 * The interface contains methods for packing and unpacking the {@link HeaderValue#getLengthBytes()} subfield.
 * The subfield contains length of the {@link MsgValue#getBodyBytes()} subfield.
 * <p>
 * Example of usage: {@link com.credibledoc.iso8583packer.FieldBuilder#defineHeaderLengthPacker(LengthPacker)}.
 * <pre>
 *     FieldBuilder.builder(MsgFieldType.LEN_VAL)
 *                 .defineHeaderLengthPacker(BcdLengthPacker.getInstance(2))
 *                 ...
 * </pre>
 *
 * @author Kyrylo Semenko
 *
 * @see HexLengthPacker
 */
public interface LengthPacker {

    /**
     * Pack a number how many bytes contains the {@link com.credibledoc.iso8583packer.message.MsgValue#getBodyBytes()} subfield.
     *
     * @param bodyBytesLength the number to be encoded and packed.
     * @return The encoded and packed bytes of the {@link MsgValue#getBodyBytes()}  subfield.
     */
    byte[] pack(int bodyBytesLength);

    /**
     * Unpack a number how many bytes contains the {@link MsgValue#getBodyBytes()} subfield.
     *
     * @param messageBytes packed bytes of incoming message.
     * @param offset       how many bytes to skip.
     * @return Number of bytes to be unpacked from the messageBytes to the {@link MsgValue#setBodyBytes(byte[])} subfield.
     */
    int unpack(byte[] messageBytes, int offset);

    /**
     * Some {@link LengthPacker}s has constant bytes length of the header part where packed length is a subfield
     * of a {@link HeaderValue#getLengthBytes()} data.
     * <p>
     * Others calculate the value depend on first byte value, for example see {@link HexLengthPacker}.
     * <p>
     * Obtained value is used for offset shifting during data unpacking.
     *
     * @param data message part started with field length bytes.
     * @param offset number of bytes in the data to skip.
     * @return Unpacked field data length.
     */
    int calculateLenLength(byte[] data, int offset);

}
