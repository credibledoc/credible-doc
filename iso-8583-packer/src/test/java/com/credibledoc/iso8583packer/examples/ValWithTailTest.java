package com.credibledoc.iso8583packer.examples;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker;
import com.credibledoc.iso8583packer.asciihex.AsciiLengthPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * Example of a {@link MsgFieldType#VAL} field without {@link MsgField#getLen()} value. The field can be the last
 * child in a list.
 * 
 * @author Kyrylo Semenko
 */
public class ValWithTailTest {
    private static final Logger logger = LoggerFactory.getLogger(ValWithTailTest.class);

    /**
     * Creation of a message structure definition.
     */
    private FieldBuilder defineMessageStructure() {
        return FieldBuilder.builder(MsgFieldType.LEN_VAL)
            .defineName("Parent")
            .defineHeaderLengthPacker(AsciiLengthPacker.getInstance(2))
            
            .createChild(MsgFieldType.VAL)
            .defineName("Child1")
            .defineLen(6)
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .createSibling(MsgFieldType.VAL)
            .defineName("Child2")
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            
            .jumpToRoot()
            .validateStructure();
    }
    
    @Test
    public void packUnpackTest() {
        FieldBuilder fieldBuilder = defineMessageStructure();
        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField());

        valueHolder.jumpToChild("Child1").setValue("child1");
        valueHolder.jumpToSibling("Child2").setValue("child2");
        
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
        
        assertEquals("child1", unpacker.jumpToChild("Child1").getValue(String.class));
        assertEquals("child2", unpacker.jumpToSibling("Child2").getValue(String.class));
    }

}
