package com.credibledoc.iso8583packer.msg.field02;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.pan.PanMasker;

/**
 * Test data.
 *
 * @author Kyrylo Semenko
 */
public class Bmp02MsgField {

    public static final String BMP_02_PAN_NAME = "02_PAN";
    private static final int FIELD_NUM_2 = 2;

    /**
     * Please do not try to instantiate this static helper.
     */
    private Bmp02MsgField() {
        throw new PackerRuntimeException("Please do not try to instantiate the static helper.");
    }

    public static void defineBmp02(MsgField rootMsgField) {
        FieldBuilder.from(rootMsgField)
                .createChild(MsgFieldType.LEN_VAL)
                .defineName(BMP_02_PAN_NAME)
                .defineTagNum(FIELD_NUM_2)
                .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2))
                .defineBodyPacker(BcdBodyPacker.rightPaddingF())
                .defineMasker(PanMasker.getInstance())
                .defineMaxLen(20);
    }
}
