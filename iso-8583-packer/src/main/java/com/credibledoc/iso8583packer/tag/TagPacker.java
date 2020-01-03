package com.credibledoc.iso8583packer.tag;

import com.credibledoc.iso8583packer.message.MsgValue;

/**
 * Defines methods fro packing and unpacking of the {@link MsgValue#getTagNum()} sub-field from bytes to int.
 * 
 * See the
 * <a href="https://github.com/credibledoc/credible-doc/blob/master/iso-8583-packer/doc/tag/tag-packer.md">tag-packer.md</a>
 * documentation.
 */
public interface TagPacker {
    
    /**
     * Create bytes from {@link MsgValue#getTagNum()} (field number).
     *
     * @param fieldTag for example 16772654 or 31.
     * @param tagLength length in bytes.
     * @return For example FFEE2E for hex 16772654 or 31 for 3331.
     */
    byte[] pack(int fieldTag, int tagLength);

    /**
     * Create the int value for the {@link MsgValue#setTagNum(Integer)} from the source bytes.
     * @param bytes the source bytes of ISO message.
     * @param offset how many bytes to skip.
     * @param tagLength how many bytes should be read.
     * @return The value of the {@link MsgValue#setTagNum(Integer)} sub-field. For example the <b>3331</b> can be
     * unpacked as <b>31</b> or the <b>FFEE2E</b> bytes can be unpacked as 16772654.
     */
    int unpack(byte[] bytes, int offset, int tagLength);
}
