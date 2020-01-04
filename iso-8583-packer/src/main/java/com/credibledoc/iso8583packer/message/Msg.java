package com.credibledoc.iso8583packer.message;

import java.util.List;

/**
 * This interface represents common parts of {@link MsgField} and {@link MsgValue} objects.
 * 
 * @author Kyrylo Semenko
 */
public interface Msg {
    
    /**
     * The value represents subfield number in the {@link MsgFieldType#BIT_SET} field.
     * It cannot be 'null' if the parent field contains the {@link com.credibledoc.iso8583packer.header.HeaderValue#getBitSet()} subfield.
     * 
     * @return The {@link MsgField#getFieldNum()} or {@link MsgValue#getFieldNum()} field value.
     */
    Integer getFieldNum();

    /**
     * The value represents tag of {@link MsgFieldType#getTaggedTypes()} fields.
     *
     * @return The {@link MsgField#getTag()} or {@link MsgValue#getTag()} field value.
     */
    Object getTag();

    /**
     * Some fields has no name, others have. This name is used for the dump and navigation in the graph.
     * 
     * @return The {@link MsgField#getName()} or {@link MsgValue#getName()} field value.
     */
    String getName();

    /**
     * The parent is a node {@link MsgValue} or {@link MsgField}.
     * It contains this child and optionally other children.
     * 
     * @return The {@link MsgField#getParent()} or {@link MsgValue#getParent()} field value.
     */
    Msg getParent();

    /**
     * Sub fields of this node or ISOMsg. If this {@link MsgValue} or {@link MsgField} is a leaf, this value is 'null'.
     * 
     * @return The {@link MsgField#getChildren()} or {@link MsgValue#getChildren()} field value.
     */
    List<? extends Msg> getChildren(); // NOSONAR, because this method will not be used by this module clients.
}
