package com.credibledoc.iso8583packer.examples;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.bcd.BcdLengthPacker;
import com.credibledoc.iso8583packer.binary.BinaryLengthPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.ebcdic.EbcdicBodyPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.hex.HexBodyPacker;
import com.credibledoc.iso8583packer.hex.HexLengthPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.literal.LiteralBodyPacker;
import com.credibledoc.iso8583packer.literal.LiteralTagPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * See the
 * <a href="https://stackoverflow.com/questions/59089591/how-to-parse-a-given-line-using-jpos">How to parse a given line using jpos?</a>
 * question on StackOverflow.
 * 
 * @author Kyrylo Semenko
 */
public class StackOverflow59089591Test {
    private static final Logger logger = LoggerFactory.getLogger(StackOverflow59089591Test.class);

    /**
     * Creation of a message structure definition.
     */
    private FieldBuilder defineMessageStructure() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineName("MTI")
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            .defineLen(4)
            
            .createSibling(MsgFieldType.BIT_SET)
            .defineName("Bitmap")
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance(16))

            .createChild(MsgFieldType.VAL)
            .defineName("PAN")
            .defineFieldNum(2)
            .defineLen(11)
            .defineBodyPacker(HexBodyPacker.getInstance())

            .createSibling(MsgFieldType.MSG)
            .defineName("Processing Code")
            .defineFieldNum(3)
            
                .createChild(MsgFieldType.VAL)
                .defineName("01 Transaction Type")
                .defineLen(1)
                .defineBodyPacker(LiteralBodyPacker.getInstance())
                .defineBodyPacker(AsciiBodyPacker.getInstance())
                .defineBodyPacker(EbcdicBodyPacker.getInstance())
                .defineBodyPacker(BcdBodyPacker.noPadding())
                .defineBodyPacker(HexBodyPacker.getInstance())
                
                .cloneToSibling()
                .defineName("02 Account Type (From)")
                
                .cloneToSibling()
                .defineName("03 Account Type (To)")
            
            .jumpToParent()
            
            .createSibling(MsgFieldType.VAL)
            .defineName("Amount, Transaction")
            .defineFieldNum(4)
            .defineLen(6)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("Amount, Cardholder Billing")
            .defineFieldNum(6)
            .defineLen(6)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("Transmission Date and Time")
            .defineFieldNum(7)
            .defineLen(5)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("Conversion Rate, Cardholder Billing")
            .defineFieldNum(10)
            .defineLen(4)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("Systems Trace Audit Number")
            .defineFieldNum(11)
            .defineLen(3)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("Time, Local Transaction HHMMSS")
            .defineFieldNum(12)
            .defineLen(3)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling()
            .defineName("Date, Local Transaction MMDD")
            .defineFieldNum(13)
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling()
            .defineName("Acq. Inst. Country Code")
            .defineFieldNum(19)
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.leftPadding0())
            
            .cloneToSibling()
            .defineName("PAN Country Code")
            .defineFieldNum(20)
            
            .cloneToSibling()
            .defineName("Card Sequence Number")
            .defineFieldNum(23)
            
            .createSibling()
            .defineName("POS Condition Code")
            .defineFieldNum(25)
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling()
            .defineName("Acquiring Inst. Ident Code")
            .defineFieldNum(32)
            .defineLen(3)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling()
            .defineName("Retrieval reference number")
            .defineFieldNum(37)
            .defineLen(12)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling()
            .defineName("Approval Code")
            .defineFieldNum(38)
            .defineLen(6)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling()
            .defineName("Action Code")
            .defineFieldNum(39)
            .defineLen(2)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling()
            .defineName("Card Acceptor Terminal Id")
            .defineFieldNum(41)
            .defineLen(8)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling()
            .defineName("Card Acceptor Ident. Code")
            .defineFieldNum(42)
            .defineLen(15)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling(MsgFieldType.LEN_VAL)
            .defineName("Proprietary Field 47")
            .defineFieldNum(47)
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2))
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .cloneToSibling()
            .defineName("Proprietary Field 48")
            .defineFieldNum(48)
            
            .createSibling(MsgFieldType.VAL)
            .defineName("Currency Code, Transaction")
            .defineFieldNum(49)
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.leftPadding0())
            
            .cloneToSibling()
            .defineName("Currency Code, Cardholder Billing")
            .defineFieldNum(51)

            .createSibling(MsgFieldType.LEN_VAL)
            .defineName("Smart Card Specific Data (EMV)")
            .defineFieldNum(55)
            .defineHeaderLengthPacker(BcdLengthPacker.getInstance(2))
            .defineBodyPacker(HexBodyPacker.getInstance())

            .createSibling()
            .defineName("Account Identification-1")
            .defineFieldNum(102)
            .defineHeaderLengthPacker(BcdLengthPacker.getInstance(1))
            .defineBodyPacker(AsciiBodyPacker.getInstance())

            .cloneToSibling()
            .defineName("Account Identification-2")
            .defineFieldNum(103)

            .cloneToSibling()
            .defineName("Transaction Description")
            .defineFieldNum(104)

            .createSibling()
            .defineName("Proprietary Field 112")
            .defineFieldNum(112)
            .defineHeaderLengthPacker(BcdLengthPacker.getInstance(3))
            
                .createChild(MsgFieldType.TAG_LEN_VAL)
                .defineName("Local fleet data")
                .defineHeaderTag("F0")
                .defineHeaderTagPacker(LiteralTagPacker.getInstance(1))
                .defineHeaderLengthPacker(BinaryLengthPacker.getInstance(1))
                
                    .createChild(MsgFieldType.TAG_LEN_VAL)
                    .defineName("Protocol Version")
                    .defineHeaderTag("C1")
                    .defineHeaderTagPacker(LiteralTagPacker.getInstance(1))
                    .defineHeaderLengthPacker(HexLengthPacker.getInstance())
                    .defineBodyPacker(HexBodyPacker.getInstance())
                
                    .cloneToSibling()
                    .defineName("Vehicle And Driver info")
                    .defineHeaderTag("E3")
                
                    .cloneToSibling()
                    .defineName("POS Additional Info")
                    .defineHeaderTag("E4")
                
                    .cloneToSibling()
                    .defineName("Commodity Info")
                    .defineHeaderTag("EA")
                    .defineBodyPacker(null)
                
                        .createChild(MsgFieldType.TAG_LEN_VAL)
                        .defineName("Product")
                        .defineHeaderTag("FF01")
                        .defineHeaderTagPacker(LiteralTagPacker.getInstance(2))
                        .defineHeaderLengthPacker(HexLengthPacker.getInstance())
                
                            .createChild(MsgFieldType.TAG_LEN_VAL)
                            .defineName("Commodity Code")
                            .defineHeaderTag("D1")
                            .defineHeaderTagPacker(LiteralTagPacker.getInstance(1))
                            .defineHeaderLengthPacker(BcdLengthPacker.getInstance(1))
                            .defineBodyPacker(AsciiBodyPacker.getInstance())
                
                            .cloneToSibling()
                            .defineName("Quantity")
                            .defineHeaderTag("D2")
                            .defineBodyPacker(HexBodyPacker.getInstance())
                
                            .cloneToSibling()
                            .defineName("Price")
                            .defineHeaderTag("D3")
                
                            .cloneToSibling()
                            .defineName("Cost")
                            .defineHeaderTag("D5")
                
                            .cloneToSibling()
                            .defineName("Additional Data")
                            .defineHeaderTag("D9")
                            .defineBodyPacker(AsciiBodyPacker.getInstance())

            .jumpToRoot()
            .validateStructure();
    }
    
    @Test
    public void unpackAndPack() {
        // unpack
        // bitmap is F66032810EC3A2000000000007010000
        // pan is 1907013330040000000011
        String hexString = "30313130F66032810EC3A20000000000070100001907013330040000000011000000000000001000" +
            "1234567890121216161120123456781829510222064300000006555444303739353633313830373739363937425A4E3030" +
            "52553435303039393233343233343533342020202020200032393038303032303539313930303133393536303131303030" +
            "303030303030303800243831393030317A3835363031313030303030303030303038064306430042910A12073E49A98826" +
            "FD3030711C861A84DA0000158003770C060000271000000190A534793E8E0B5CF617464E5F4143434F554E545F4944454E" +
            "543117464E5F4143434F554E545F4944454E543217464E5F5452414E535F4445534352495054" +
            "000085" +
            "F053C1024204E31DC11B54504F5254414C2020202020202020202020202020202020202020E41381024401820513102800058306131028100454EA19FF0116D103543233D2024204D3034202D5D5024210D9025431";
        byte[] bytes = HexService.hex2byte(hexString);

        FieldBuilder fieldBuilder = defineMessageStructure().jumpToRoot();
        MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
        
        // print result
        String msgFieldStructure = DumpService.getInstance().dumpMsgField(fieldBuilder.getCurrentField());
        String msgValueData = DumpService.getInstance().dumpMsgValue(fieldBuilder.getCurrentField(), msgValue, false);
        logger.info("Example of a message.\nMessage Structure:\n{}\nMessage data:\n{}",
            msgFieldStructure, msgValueData);
        
        // pack again
        MsgField msgField = defineMessageStructure().jumpToRoot().getCurrentField();
        ValueHolder valueHolder = ValueHolder.newInstance(msgValue, msgField);
        byte[] packedBytes = valueHolder.pack();
        String packedHexString = HexService.bytesToHex(packedBytes);
        assertEquals(hexString, packedHexString);
    }

}
