package com.credibledoc.iso8583packer.empty;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.header.HeaderField;
import com.credibledoc.iso8583packer.tag.TagPacker;

/**
 * Packs and unpacks fields with no {@link com.credibledoc.iso8583packer.header.HeaderValue#getTagBytes()},
 * for example root fields of ISO 8583 messages has no tag names. Information of this field is stored in the
 * {@link HeaderField#getBitSet()} value.
 * <p>
 * For example Field 2 may contain the nex bytes F0F3545721 where F0F3 is the length 3 and 545721 is the field body.
 *
 * @author Kyrylo Semenko
 */
public class EmptyTagPacker implements TagPacker {
    public static final EmptyTagPacker INSTANCE = new EmptyTagPacker();
    
    private EmptyTagPacker() {
        throw new PackerRuntimeException("Please do not instantiate the static helper.");
    }
    
    @Override
    public byte[] pack(int fieldTag, int tagLength) {
        return new byte[0];
    }

    @Override
    public int unpack(byte[] bytes, int offset, int tagLength) {
        return 0;
    }
}
