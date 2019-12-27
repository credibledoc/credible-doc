package com.credibledoc.iso8583packer.msg.field00;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.msg.field02.Bmp02MsgField;
import com.credibledoc.iso8583packer.msg.field58.Bmp58MsgField;

/**
 * Test data
 *
 * @author Kyrylo Semenko
 */
// TODO Kyrylo Semenko - use or delete
public class TestIsoMsgField {
    private static final String ISO_MSG_NAME = "IsoMsg";

    /**
     * The message structure definition. The field structure should be initialized once and should not be changed later. 
     */
    private static MsgField msgField;

    /**
     * Please do not try to instantiate this static helper.
     */
    private TestIsoMsgField() {
        throw new PackerRuntimeException("Please do not try to instantiate the static helper.");
    }

    private static MsgField defineIsoMsg() {
        MsgField rootMsgField = FieldBuilder.builder(MsgFieldType.BIT_SET)
                .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance())
                .defineLen(16)
                .defineName(ISO_MSG_NAME)
                .defineChildrenTagLen(0)
                .getCurrentField();

        Bmp02MsgField.defineBmp02(rootMsgField);
        Bmp58MsgField.defineBmp58(rootMsgField);

        FieldBuilder.validateStructure(rootMsgField);
        return rootMsgField;
    }

    /**
     * @return The {@link #msgField} field value.
     */
    public static MsgField getMsgField() {
        if (msgField == null) {
            msgField = defineIsoMsg();
        }
        return msgField;
    }
}
