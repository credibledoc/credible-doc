package com.credibledoc.iso8583packer.message;

/**
 * This container has a pair of objects, the {@link MsgField} and {@link MsgValue}. These objects belong to each other
 * by {@link Msg#getName()} and {@link Msg#getTagNum()} values.
 */
public class MsgPair {
    /**
     * The definition of the message structure.
     */
    private MsgField msgField;

    /**
     * The message data;
     */
    private MsgValue msgValue;

    public MsgPair() {
        // empty constructor
    }

    public MsgPair(MsgField msgField, MsgValue msgValue) {
        this.msgField = msgField;
        this.msgValue = msgValue;
    }

    @Override
    public String toString() {
        return "MsgPair{" +
                "msgField=" + msgField +
                ", msgValue=" + msgValue +
                '}';
    }

    /**
     * @return The {@link #msgField} field value.
     */
    public MsgField getMsgField() {
        return msgField;
    }

    /**
     * @param msgField see the {@link #msgField} field description.
     */
    public void setMsgField(MsgField msgField) {
        this.msgField = msgField;
    }

    /**
     * @return The {@link #msgValue} field value.
     */
    public MsgValue getMsgValue() {
        return msgValue;
    }

    /**
     * @param msgValue see the {@link #msgValue} field description.
     */
    public void setMsgValue(MsgValue msgValue) {
        this.msgValue = msgValue;
    }
}
