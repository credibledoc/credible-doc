package com.credibledoc.iso8583packer.examples;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * Example of {@link MsgFieldType#LEN_VAL} fields with a {@link MsgFieldType#VAL} parent.
 * 
 * @author Kyrylo Semenko
 */
public class LenValWithoutBitmapTest {
    private static final Logger logger = LoggerFactory.getLogger(LenValWithoutBitmapTest.class);

    /**
     * Creation of a message structure definition.
     */
    private FieldBuilder defineMessageStructure() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineName("Parent")
            .defineLen(17)
            
            .createChild(MsgFieldType.LEN_VAL)
            .defineName("Child1")
            .defineHeaderLengthPacker(EbcdicDecimalLengthPacker.getInstance(2))
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .cloneToSibling()
            .defineName("Child2")
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .jumpToRoot()
            .validateStructure();
    }
    
    @Test
    public void packUnpackTest() {
        FieldBuilder fieldBuilder = defineMessageStructure();
        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField());

        valueHolder.jumpToChild("Child1").setValue("111111111111");
        valueHolder.jumpToSibling("Child2").setValue("22222222222222");
        
        // print result
        MsgValue msgValue = valueHolder.jumpToRoot().getCurrentMsgValue();
        String msgFieldStructure = DumpService.getInstance().dumpMsgField(fieldBuilder.getCurrentField());
        String msgValueData = DumpService.getInstance().dumpMsgValue(fieldBuilder.getCurrentField(), msgValue, false);
        byte[] bytes = valueHolder.pack();
        logger.info("Example of a message.\nMessage Structure:\n{}\nMessage data:\n{}\nMessage bytes in hex:\n{}",
            msgFieldStructure, msgValueData, HexService.bytesToHex(bytes));
        
        // unpack again
        MsgField msgField = defineMessageStructure().getCurrentField();
        ValueHolder unpacker = ValueHolder.newInstance(msgField);
        unpacker.unpack(bytes);
        
        assertEquals("111111111111", unpacker.jumpToChild("Child1").getValue(String.class));
        assertEquals("22222222222222", unpacker.jumpToSibling("Child2").getValue(String.class));
    }

}
