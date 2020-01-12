package com.credibledoc.iso8583packer.examples;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker;
import com.credibledoc.iso8583packer.asciihex.AsciiLengthPacker;
import com.credibledoc.iso8583packer.binary.BinaryLengthPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.ebcdic.EbcdicBodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * See the
 * <a href="https://stackoverflow.com/questions/7991023/understanding-iso-8583-messaging-log">Understanding ISO 8583 messaging log</a>
 * page.
 * 
 * @author Kyrylo Semenko
 */
public class UnderstandingIso8583MessageLogTest {
    private static final Logger logger = LoggerFactory.getLogger(UnderstandingIso8583MessageLogTest.class);

    /**
     * Used in documentation
     */
    private FieldBuilder defineMessageStructure() {
        return FieldBuilder.builder(MsgFieldType.LEN_VAL)
            .defineName("Root")
            .defineHeaderLengthPacker(BinaryLengthPacker.getInstance(2))
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createChild(MsgFieldType.VAL)
            .defineName("Header")
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            .defineLen(10)
            
            .createSibling(MsgFieldType.VAL)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            .defineName("MTI")
            .defineLen(4)
            
            .createSibling(MsgFieldType.BIT_SET)
            .defineName("Bitmap")
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance(8))
            
            .createChild(MsgFieldType.VAL)
            .defineName("SystemTraceAuditNumber")
            .defineFieldNum(11)
            .defineLen(6)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("LocalTransactionTimeHHMMSS")
            .defineFieldNum(12)
            .defineLen(6)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling()
            .defineName("LocalTransactionDateMMDD")
            .defineFieldNum(13)
            .defineLen(4)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling()
            .defineName("TerminalId")
            .defineFieldNum(41)
            .defineLen(8)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling(MsgFieldType.LEN_VAL)
            .defineName("PrivateData_48")
            .defineFieldNum(48)
            .defineHeaderLengthPacker(AsciiLengthPacker.getInstance(3))
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("NetworkManagementInformationCode")
            .defineFieldNum(70)
            .defineLen(3)
            .defineBodyPacker(AsciiBodyPacker.getInstance());
    }
    
    @Test
    public void unpackAndPack() {
        // unpack
        String hexString = "00 5B 30 31 31 30 30 30 30 30 30 30 30 38 32 30 80 38 00 00 " +
            "00 81 00 00 04 00 00 00 00 00 00 00 33 36 32 39 31 30 31 30 " +
            "32 39 35 37 31 30 33 31 31 30 30 30 30 30 30 35 30 33 31 53 " +
            "55 32 30 31 31 31 30 33 31 31 30 32 39 35 37 32 30 31 31 31 " +
            "30 33 31 31 30 32 39 35 37 33 30 30 31";
        byte[] bytes = HexService.hex2byte(hexString, "\\s+");

        FieldBuilder fieldBuilder = defineMessageStructure().jumpToRoot();
        MsgValue msgValue = ValueHolder.unpack(bytes, 0, fieldBuilder.getCurrentField());
        
        // print result
        String msgFieldStructure = DumpService.getInstance().dumpMsgField(fieldBuilder.getCurrentField());
        String msgValueData = DumpService.getInstance().dumpMsgValue(fieldBuilder.getCurrentField(), msgValue, false);
        logger.info("Example of a message " +
            "with 16 bytes long Bitmap.\nMessage Structure:\n{}\nMessage data:\n{}End of example.",
            msgFieldStructure, msgValueData);
        
        // pack again
        MsgField msgField = defineMessageStructure().jumpToRoot().getCurrentField();
        ValueHolder valueHolder = ValueHolder.newInstance(msgValue, msgField);
        byte[] packedBytes = valueHolder.pack();
        String packedHexString = HexService.bytesToHex(packedBytes, " ");
        assertEquals(hexString, packedHexString);
    }

    @Test(expected = PackerRuntimeException.class)
    public void testLongValue() {
        byte[] bytes = new byte[2];
        byte[] value = HexService.hex2byte("112233");
        EbcdicBodyPacker.getInstance().pack(value, bytes, 0);
    }

    @Test
    public void testPack() {
        String data = "Hello";
        byte[] bytes = new byte[data.length()];
        EbcdicBodyPacker.getInstance().pack(data, bytes, 0);
        assertArrayEquals(HexService.hex2byte("C885939396"), bytes);
    }

    @Test(expected = PackerRuntimeException.class)
    public void testUnpackShort() {
        byte[] ab = HexService.hex2byte("61");
        EbcdicBodyPacker.getInstance().unpack(ab, 0, 2);
    }

    @Test
    public void testMsgFieldStructure() {
        MsgField msgField = defineMessageStructure().getCurrentField();
        DumpService dumpService = DumpService.getInstance();
        String dump = dumpService.dumpMsgField(msgField);
        assertNotNull(dump);
        logger.info("MsgField structure dump: \n{}", dump);
    }

}
