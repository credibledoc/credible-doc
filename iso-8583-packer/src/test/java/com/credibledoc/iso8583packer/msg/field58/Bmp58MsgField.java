package com.credibledoc.iso8583packer.msg.field58;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicBodyPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalTagPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.literal.LiteralBodyPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;

/**
 * Test data
 *
 * @author Kyrylo Semenko
 */
// TODO Kyrylo Semenko - rename
public class Bmp58MsgField {

    public static final String F_58_NAME = "58";
    static final String RATE_REQUEST_REFERENCE_ID_35 = "35";
    static final String DCC_DATA_37 = "37";
    static final String NON_LOYALTY_GROUP_53 = "53";
    static final String BRANCH_PARTNER_ID_97 = "97";
    static final String PAYBACK_CARD_NUMBER_TOKEN_90 = "90";
    static final String PAYBACK_TOKEN_92 = "92";
    static final String RECEIPT_ID_TOKEN_93 = "93";
    static final String REFERENCE_NUMBER_95 = "95";
    static final String DCC_STATUS_37_1 = "dcc_status";
    static final String CURRENCY_CODE_37_2 = "currency_code";
    static final String TRANSACTION_AMOUNT_37_5 = "transaction_amount";
    static final String CONVERSION_RATE_37_17 = "conversion_rate";
    static final String ELAVON_POS_TERMINAL_CAPABILITIES_98 = "98";
    private static final int FIELD_NUM_58 = 58;

    /**
     * Please do not try to instantiate this static helper.
     */
    private Bmp58MsgField() {
        throw new PackerRuntimeException("Please do not try to instantiate the static helper.");
    }

    public static void defineBmp58(MsgField rootMsgField) {
        MsgField f58definition = FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineParent(rootMsgField)
                .defineTagNum(FIELD_NUM_58)
                .defineName(F_58_NAME)
                .defineBodyPacker(LiteralBodyPacker.INSTANCE)
                .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(3))
                .defineChildrenLengthPacker(EbcdicDecimalLengthPacker.getInstance(3))
                .defineChildrenTagPacker(EbcdicDecimalTagPacker.INSTANCE)
                .defineChildrenTagLen(2)
                .defineMaxLen(999)
                .getCurrentField();

        MsgField subfield35 = FieldBuilder.builder(MsgFieldType.LEN_TAG_VAL)
                .defineBodyPacker(AsciiBodyPacker.INSTANCE)
                .defineName(RATE_REQUEST_REFERENCE_ID_35)
                .defineTagNum(Integer.parseInt(RATE_REQUEST_REFERENCE_ID_35))
                .defineParent(f58definition)
                .getCurrentField();

        FieldBuilder.clone(subfield35)
                .defineName(DCC_DATA_37)
                .defineTagNum(Integer.parseInt(DCC_DATA_37))
                .defineBodyPacker(EbcdicBodyPacker.INSTANCE)
                .defineChildrenTagLen(0)
                .defineChildrenLengthPacker(null)
                .defineParent(subfield35.getParent())

                .createChild(MsgFieldType.TAG_LEN_VAL)

                .defineName(DCC_STATUS_37_1)
                .defineType(MsgFieldType.VAL)
                .defineTagNum(1)
                .defineLen(1)
                .defineBodyPacker(EbcdicBodyPacker.INSTANCE)

                .cloneToSibling()
                
                .defineName(CURRENCY_CODE_37_2)
                .defineTagNum(2)
                .defineLen(3)

                .cloneToSibling()

                .defineName(TRANSACTION_AMOUNT_37_5)
                .defineTagNum(5)
                .defineLen(12)

                .cloneToSibling()
                
                .defineName(CONVERSION_RATE_37_17)
                .defineTagNum(17)
                .defineLen(8);

        FieldBuilder.clone(subfield35)
                .defineName(NON_LOYALTY_GROUP_53)
                .defineTagNum(Integer.parseInt(NON_LOYALTY_GROUP_53))
                .defineBodyPacker(EbcdicBodyPacker.INSTANCE)
                .defineParent(f58definition)
                
                .cloneToSibling()
                
                .defineName(PAYBACK_CARD_NUMBER_TOKEN_90)
                .defineTagNum(Integer.parseInt(PAYBACK_CARD_NUMBER_TOKEN_90))
                
                .cloneToSibling()

                .defineName(PAYBACK_TOKEN_92)
                .defineTagNum(Integer.parseInt(PAYBACK_TOKEN_92))
                
                .cloneToSibling()
                
                .defineName(RECEIPT_ID_TOKEN_93)
                .defineTagNum(Integer.parseInt(RECEIPT_ID_TOKEN_93))
                
                .cloneToSibling()
                
                .defineName(REFERENCE_NUMBER_95)
                .defineTagNum(Integer.parseInt(REFERENCE_NUMBER_95))
                
                .cloneToSibling()
                
                .defineName(BRANCH_PARTNER_ID_97)
                .defineTagNum(Integer.parseInt(BRANCH_PARTNER_ID_97))
                
                .cloneToSibling()
                
                .defineName(ELAVON_POS_TERMINAL_CAPABILITIES_98)
                .defineTagNum(Integer.parseInt(ELAVON_POS_TERMINAL_CAPABILITIES_98));
    }

}
