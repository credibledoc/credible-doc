package com.credibledoc.iso8583packer.message;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.hex.HexBodyPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.ifb.IfbBitmapPacker;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for the {@link IsoMsg} methods.
 * 
 * @author Kyrylo Semenko
 */
public class IsoMsgTest {
    
    @Test
    public void emptyMtiTest() {
        MsgField msgField = new MsgField();
        IsoMsg isoMsg = new IsoMsg();
        isoMsg.setPackager(msgField);
        assertNull(isoMsg.getMti());
    }
    
    @Test
    public void setPackagerTest() {
        MsgField msgField = new MsgField();
        IsoMsg isoMsg = new IsoMsg();
        isoMsg.setPackager(msgField);
        assertNotNull(isoMsg.getPackager());
    }
    
    @Test
    public void isoMsgSetGetTest() {
        MsgField msgField = definePackager();

        IsoMsg isoMsg = new IsoMsg();
        isoMsg.setPackager(msgField);
        isoMsg.setMti("0200");
        isoMsg.set(2, "2222");
        isoMsg.setValue("4444", Arrays.asList("MSG", "BIT_SET", "Amount"));
        isoMsg.set("ReasonCode", "2525");
        isoMsg.setStan("1111");
        isoMsg.setTerminalId("4141");
        isoMsg.set("5555", "MSG", "BIT_SET", "AmountReco");
        byte[] bytes = isoMsg.pack();
        String expectedHex = "02005820008000800000222244445555111125254141";
        assertEquals(expectedHex, HexService.bytesToHex(bytes));
        
        assertEquals("2222", isoMsg.get(2));
        assertNull(isoMsg.get(10));
        assertEquals("2525", isoMsg.get("ReasonCode"));
        assertEquals("1111", isoMsg.getStan());
        assertEquals("4141", isoMsg.getTerminalId());
        assertNotNull(isoMsg.getValueHolder());
    }

    private MsgField definePackager() {
        return FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("MSG")

            .createChild(MsgFieldType.VAL)
            .defineName("MTI")
            .defineBodyPacker(HexBodyPacker.getInstance())
            .defineLen(2)
            
            .createSibling(MsgFieldType.BIT_SET)
            .defineName("BIT_SET")
            .defineHeaderBitmapPacker(IfbBitmapPacker.getInstance(8))

            .createChild(MsgFieldType.VAL)
            .defineName("PAN")
            .defineFieldNum(2)
            .defineBodyPacker(HexBodyPacker.getInstance())
            .defineLen(2)

            .createSibling(MsgFieldType.VAL)
            .defineName("Amount")
            .defineFieldNum(4)
            .defineBodyPacker(HexBodyPacker.getInstance())
            .defineLen(2)

            .createSibling(MsgFieldType.VAL)
            .defineName("AmountReco")
            .defineFieldNum(5)
            .defineBodyPacker(HexBodyPacker.getInstance())
            .defineLen(2)

            .createSibling(MsgFieldType.VAL)
            .defineName("STAN")
            .defineFieldNum(11)
            .defineBodyPacker(HexBodyPacker.getInstance())
            .defineLen(2)

            .createSibling(MsgFieldType.VAL)
            .defineName("ReasonCode")
            .defineFieldNum(25)
            .defineBodyPacker(HexBodyPacker.getInstance())
            .defineLen(2)

            .createSibling(MsgFieldType.VAL)
            .defineName("TerminalId")
            .defineFieldNum(41)
            .defineBodyPacker(HexBodyPacker.getInstance())
            .defineLen(2)
            
            .getCurrentField();
    }

    @Test
    public void unpackTest() {
        MsgField msgField = definePackager();

        IsoMsg isoMsg = new IsoMsg();
        isoMsg.setPackager(msgField);
        String hex = "0200502000800080000022224444111125254141";
        isoMsg.unpack(HexService.hex2byte(hex));
        String expectedMti = "0200";
        assertEquals(expectedMti, isoMsg.getMti());
        String expectedPan = "2222";
        assertEquals(expectedPan, isoMsg.get("MSG", "BIT_SET", "PAN"));
        assertEquals(expectedPan, isoMsg.get(Arrays.asList("MSG", "BIT_SET", "PAN")));
    }

    @Test
    public void getMsgValueTest() {
        IsoMsg isoMsg = new IsoMsg();
        isoMsg.setPackager(definePackager());
        MsgValue msgValue = isoMsg.getMsgValue("MSG", "MTI");
        assertNotNull(msgValue);
        assertNull(msgValue.getBodyValue());

        String panValue = "1234";
        isoMsg.set(2, panValue);
        
        MsgValue msgValuePan = isoMsg.getMsgValue("MSG", "BIT_SET", "PAN");
        assertEquals(panValue, msgValuePan.getBodyValue(String.class));
    }
}
