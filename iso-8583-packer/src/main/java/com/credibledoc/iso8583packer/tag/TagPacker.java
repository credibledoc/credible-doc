package com.credibledoc.iso8583packer.tag;

import com.credibledoc.iso8583packer.message.Msg;
import com.credibledoc.iso8583packer.message.MsgValue;

/**
 * Defines methods fro packing and unpacking of the {@link MsgValue#getTag()} sub-field from bytes to int.
 * <p>
 * See the
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/tag/tag-packer.md">tag-packer.md</a>
 * documentation.
 */
public interface TagPacker {
    
    /**
     * Create bytes from {@link MsgValue#getFieldNum()} (field number).
     *
     * @param tag the {@link Msg#getTag()} value, for example 16772654 or 31.
     * @return For example FFEE2E for hex 16772654 or 31 for 3331.
     */
    byte[] pack(Object tag);

    /**
     * Create the int value for the {@link MsgValue#setTag(Object)} from the source bytes.
     * @param bytes the source bytes of ISO message.
     * @param offset how many bytes to skip.
     * @param <T> the {@link MsgValue#setTag(Object)} type.
     * @return The value of the {@link MsgValue#setTag(Object)} sub-field. For example the <b>3331</b> can be
     * unpacked as <b>31</b> or the <b>FFEE2E</b> bytes can be unpacked as 16772654.
     */
    <T> T unpack(byte[] bytes, int offset);

    /**
     * @return The number of bytes the TAG occupies in the packed state.
     */
    int getPackedLength();
}
