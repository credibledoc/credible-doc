package com.credibledoc.iso8583packer.message;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Defines the type of the current {@link MsgField}, for example {@link #TAG_LEN_VAL}, {@link #LEN_TAG_VAL} and so on.
 * <p>
 * The type is used for decision how to pack and unpack a {@link MsgValue}, that corresponds to the {@link MsgField}.
 *
 * @author Kyrylo Semenko
 */
public enum MsgFieldType {
    /**
     * Tag - Length - Value. See https://en.wikipedia.org/wiki/Type-length-value.
     */
    TAG_LEN_VAL,

    /**
     * Length - Tag - Value. Similar to the {@link #TAG_LEN_VAL} type, but the length is first.
     */
    LEN_TAG_VAL,

    /**
     * Tag - Value. similar to the {@link #TAG_LEN_VAL} type, but with fixed length.
     */
    TAG_VAL,

    /**
     * Length - Value. Similar to the {@link #TAG_LEN_VAL} type, but without a tag.
     */
    LEN_VAL,

    /**
     * The bitmap {@link MsgField} with definition of its children.
     */
    BIT_SET,

    /**
     * Similar to {@link #TAG_LEN_VAL}, but without tag and length. It may be a leaf field or a parent for test purposes.
     */
    VAL,

    /**
     * Container for a message.
     */
    MSG;

    /**
     * Contains {@link MsgFieldType}s with specified {@link MsgField#getFieldNum()} sub-field.
     */
    private static final List<MsgFieldType> tagTypes = Arrays.asList(TAG_LEN_VAL, LEN_TAG_VAL, TAG_VAL);

    /**
     * Contains  {@link MsgFieldType}s with enclosed {@link MsgValue}s
     * with non-null {@link MsgValue#getLengthBytes()} value.
     */
    private static final List<MsgFieldType> lengthTypes = Arrays.asList(TAG_LEN_VAL, LEN_TAG_VAL, LEN_VAL);

    /**
     * Contains  {@link MsgFieldType}s with enclosed {@link MsgValue}s
     * with {@link MsgValue#getLengthBytes()} as the first value.
     */
    private static final List<MsgFieldType> lengthFirstTypes = Arrays.asList(LEN_TAG_VAL, LEN_VAL);

    /**
     * Contains  {@link MsgFieldType}s with fixed body length.
     */
    private static final List<MsgFieldType> fixedLengthTypes = Arrays.asList(VAL, BIT_SET, TAG_VAL);

    /**
     * @return The {@link #tagTypes} list.
     */
    public static List<MsgFieldType> getTaggedTypes() {
        return tagTypes;
    }

    /**
     * @return The {@link #lengthTypes} list.
     */
    public static Collection<MsgFieldType> getLengthTypes() {
        return lengthTypes;
    }

    /**
     * @return The {@link #lengthFirstTypes} list.
     */
    public static Collection<MsgFieldType> getLengthFirstTypes() {
        return lengthFirstTypes;
    }

    /**
     * @return The {@link #fixedLengthTypes} field value.
     */
    public static List<MsgFieldType> getFixedLengthTypes() {
        return fixedLengthTypes;
    }

    /**
     * @param msgField contains a {@link MsgFieldType} value.
     * @return 'true' if the argument {@link MsgField#getType()} is not in the {@link #getTaggedTypes()} list.
     */
    public static boolean isNotTaggedType(MsgField msgField) {
        return !getTaggedTypes().contains(msgField.getType());
    }

    /**
     * @param msgField contains a {@link MsgFieldType} value.
     * @return 'true' if the {@link #getTaggedTypes()} list contains the argument {@link MsgField#getType()}.
     */
    public static boolean isTaggedType(MsgField msgField) {
        return getTaggedTypes().contains(msgField.getType());
    }

    /**
     * @param msgField contains a {@link MsgFieldType} value.
     * @return 'true' if the {@link #fixedLengthTypes} list contains the argument {@link MsgField#getType()}.
     */
    public static boolean isFixedLengthType(MsgField msgField) {
        return fixedLengthTypes.contains(msgField.getType());
    }

    /**
     * @param msgField contains a {@link MsgFieldType} value.
     * @return 'true' if the {@link #lengthTypes} list contains the argument {@link MsgField#getType()}.
     */
    public static boolean isLengthType(MsgField msgField) {
        return lengthTypes.contains(msgField.getType());
    }
}
