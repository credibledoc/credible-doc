package com.credibledoc.iso8583packer.examples;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.bcd.BcdLengthPacker;
import com.credibledoc.iso8583packer.binary.BinaryToHexStringer;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.hex.HexBodyPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.literal.LiteralBodyPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * See the
 * <a href="https://stackoverflow.com/questions/53740336/invalid-iso-8583-header">Invalid ISO 8583 Header</a>
 * question on StackOverflow.
 * 
 * @author Kyrylo Semenko
 */
public class StackOverflow53740336Test {
    private static final Logger logger = LoggerFactory.getLogger(StackOverflow53740336Test.class);

    /**
     * Creation of a message structure definition.
     */
    private FieldBuilder defineMessageStructure() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineName("Header")
            .defineBodyPacker(LiteralBodyPacker.getInstance())
            .defineStringer(BinaryToHexStringer.getInstance())
            .defineLen(5)
            
            .createSibling(MsgFieldType.VAL)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            .defineName("MTI")
            .defineLen(2)
            
            .createSibling(MsgFieldType.BIT_SET)
            .defineName("Bitmap")
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance(8))
            
            .createChild(MsgFieldType.VAL)
            .defineName("ProcessingCode")
            .defineFieldNum(3)
            .defineLen(3)
            .defineBodyPacker(HexBodyPacker.getInstance())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("Amount")
            .defineFieldNum(4)
            .defineLen(6)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("SystemTraceAuditNumber")
            .defineFieldNum(11)
            .defineLen(3)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("LocalTransactionTimeHHMMSS")
            .defineFieldNum(12)
            .defineLen(3)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling()
            .defineName("LocalTransactionDateMMDD")
            .defineFieldNum(13)
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .createSibling()
            .defineName("PosNumber")
            .defineFieldNum(37)
            .defineLen(13)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling()
            .defineName("ApprovalCode")
            .defineFieldNum(38)
            .defineLen(6)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling()
            .defineName("ActionCode")
            .defineFieldNum(39)
            .defineLen(2)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling()
            .defineName("TerminalId")
            .defineFieldNum(41)
            .defineLen(7)
            .defineBodyPacker(AsciiBodyPacker.getInstance())

            .createSibling(MsgFieldType.LEN_VAL)
            .defineName("EMV")
            .defineFieldNum(55)
            .defineHeaderLengthPacker(BcdLengthPacker.getInstance(2))
            .defineBodyPacker(HexBodyPacker.getInstance())
            
            .validateStructure();
    }
    
    @Test
    public void unpackAndPack() {
        // unpack
        String hexString = "60010203040210303800000E8002000000000000000031000046741306511212383334363133303034363734313330363534303036323730353532340012910A59218CDAFBBCD2520014";
        byte[] bytes = HexService.hex2byte(hexString);

        FieldBuilder fieldBuilder = defineMessageStructure().jumpToRoot();
        MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
        
        // print result
        String msgFieldStructure = DumpService.getInstance().dumpMsgField(fieldBuilder.getCurrentField());
        String msgValueData = DumpService.getInstance().dumpMsgValue(fieldBuilder.getCurrentField(), msgValue, false);
        logger.info("Example of a message " +
            "with 8 bytes long Bitmap.\nMessage Structure:\n{}\nMessage data:\n{}",
            msgFieldStructure, msgValueData);
        
        // pack again
        MsgField msgField = defineMessageStructure().jumpToRoot().getCurrentField();
        ValueHolder valueHolder = ValueHolder.newInstance(msgValue, msgField);
        byte[] packedBytes = valueHolder.pack();
        String packedHexString = HexService.bytesToHex(packedBytes);
        assertEquals(hexString, packedHexString);
    }

}
