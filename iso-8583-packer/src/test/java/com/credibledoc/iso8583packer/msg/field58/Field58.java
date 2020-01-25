package com.credibledoc.iso8583packer.msg.field58;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicBodyPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalTagPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;

/**
 * Test data
 *
 * @author Kyrylo Semenko
 */
public class Field58 {

    public static final String F_58_NAME = "field_58";
    public static final String RATE_REQUEST_REFERENCE_ID_35_NAME = "rate_request_reference";
    public static final String DCC_DATA_37_NAME = "dcc_data";
    public static final String NON_LOYALTY_GROUP_53_NAME = "non_loyalty_group";
    public static final String BRANCH_PARTNER_ID_97_NAME = "97";
    public static final String PAYBACK_CARD_NUMBER_TOKEN_90_NAME = "90";
    public static final String PAYBACK_TOKEN_92_NAME = "92";
    public static final String RECEIPT_ID_TOKEN_93_NAME = "93";
    public static final String REFERENCE_NUMBER_95_NAME = "95";
    public static final String DCC_STATUS_37_1 = "dcc_status";
    public static final String CURRENCY_CODE_37_2 = "currency_code";
    public static final String TRANSACTION_AMOUNT_37_5 = "transaction_amount";
    public static final String CONVERSION_RATE_37_17 = "conversion_rate";
    public static final String POS_TERMINAL_CAPABILITIES_98_NAME = "pos_terminal_capabilities";
    private static final int FIELD_NUM_58 = 58;
    private static final int RATE_REQUEST_REFERENCE_ID_35 = 35;
    private static final int DCC_DATA_37 = 37;
    private static final int NON_LOYALTY_GROUP_53 = 53;
    private static final int POS_TERMINAL_CAPABILITIES_98 = 98;
    private static final int BRANCH_PARTNER_ID_97 = 97;
    private static final int REFERENCE_NUMBER_95 = 95;
    private static final int RECEIPT_ID_TOKEN_93 = 93;
    private static final int PAYBACK_TOKEN_92 = 92;
    private static final int PAYBACK_CARD_NUMBER_TOKEN_90 = 90;

    /**
     * Please do not try to instantiate this static helper.
     */
    private Field58() {
        throw new PackerRuntimeException("Please do not try to instantiate the static helper.");
    }

    public static void defineField58(MsgField rootMsgField) {
        MsgField f58definition = FieldBuilder.builder(MsgFieldType.LEN_VAL)
                .defineParent(rootMsgField)
                .defineFieldNum(FIELD_NUM_58)
                .defineName(F_58_NAME)
                .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(3))
                .defineChildrenLengthPacker(EbcdicDecimalLengthPacker.getInstance(3))
                .defineChildrenTagPacker(EbcdicDecimalTagPacker.getInstance(2))
                .defineMaxLen(999)
                .getCurrentField();

        MsgField subfield35 = FieldBuilder.builder(MsgFieldType.LEN_TAG_VAL)
                .defineBodyPacker(AsciiBodyPacker.getInstance())
                .defineName(RATE_REQUEST_REFERENCE_ID_35_NAME)
                .defineHeaderTag(RATE_REQUEST_REFERENCE_ID_35)
                .defineParent(f58definition)
                .getCurrentField();

        FieldBuilder.from(subfield35).cloneToSibling()
                .defineName(DCC_DATA_37_NAME)
                .defineHeaderTag(DCC_DATA_37)
                .defineChildrenLengthPacker(null)
                .defineBodyPacker(null)

                .createChild(MsgFieldType.TAG_LEN_VAL)

                .defineName(DCC_STATUS_37_1)
                .defineType(MsgFieldType.VAL)
                .defineLen(1)
                .defineBodyPacker(EbcdicBodyPacker.getInstance())

                .cloneToSibling()
                
                .defineName(CURRENCY_CODE_37_2)
                .defineLen(3)

                .cloneToSibling()

                .defineName(TRANSACTION_AMOUNT_37_5)
                .defineLen(12)

                .cloneToSibling()
                
                .defineName(CONVERSION_RATE_37_17)
                .defineLen(8);

        FieldBuilder.from(subfield35).cloneToSibling()
                .defineName(NON_LOYALTY_GROUP_53_NAME)
                .defineHeaderTag(NON_LOYALTY_GROUP_53)
                .defineBodyPacker(EbcdicBodyPacker.getInstance())
                
                .cloneToSibling()
                
                .defineName(PAYBACK_CARD_NUMBER_TOKEN_90_NAME)
                .defineHeaderTag(PAYBACK_CARD_NUMBER_TOKEN_90)
                
                .cloneToSibling()

                .defineName(PAYBACK_TOKEN_92_NAME)
                .defineHeaderTag(PAYBACK_TOKEN_92)
                
                .cloneToSibling()
                
                .defineName(RECEIPT_ID_TOKEN_93_NAME)
                .defineHeaderTag(RECEIPT_ID_TOKEN_93)
                
                .cloneToSibling()
                
                .defineName(REFERENCE_NUMBER_95_NAME)
                .defineHeaderTag(REFERENCE_NUMBER_95)
                
                .cloneToSibling()
                
                .defineName(BRANCH_PARTNER_ID_97_NAME)
                .defineHeaderTag(BRANCH_PARTNER_ID_97)
                
                .cloneToSibling()
                
                .defineName(POS_TERMINAL_CAPABILITIES_98_NAME)
                .defineHeaderTag(POS_TERMINAL_CAPABILITIES_98);
    }

}
