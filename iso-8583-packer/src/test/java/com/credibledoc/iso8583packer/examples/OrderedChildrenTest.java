package com.credibledoc.iso8583packer.examples;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.bcd.BcdBodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * The case when the children where created manually and the last child should be placed between the existing siblings.
 * 
 * @author Kyrylo Semenko
 */
public class OrderedChildrenTest {
    private static final Logger logger = LoggerFactory.getLogger(OrderedChildrenTest.class);

    /**
     * The message structure.
     */
    private FieldBuilder defineMessageStructure() {
        return FieldBuilder.builder(MsgFieldType.VAL)
            .defineName("Parent")
            .defineLen(6)
            
            .createChild(MsgFieldType.VAL)
            .defineName("Child1")
            .defineLen(2)
            .defineBodyPacker(BcdBodyPacker.noPadding())
            
            .cloneToSibling()
            .defineName("Child2")
            
            .cloneToSibling()
            .defineName("Child3")
            
            .validateStructure();
    }
    
    @Test
    public void packUnpackTest() {
        FieldBuilder fieldBuilder = defineMessageStructure();
        fieldBuilder.jumpToRoot();
        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField());

        // manual setting
        valueHolder.getCurrentMsgValue().setChildren(new ArrayList<>());
        
        MsgValue child1 = new MsgValue();
        child1.setRoot(valueHolder.getCurrentMsgValue());
        child1.setParent(valueHolder.getCurrentMsgValue());
        child1.setName("Child1");
        child1.setBodyValue("1111");
        child1.setBodyBytes(HexService.hex2byte("1111"));
        
        MsgValue child3 = new MsgValue();
        child3.setRoot(valueHolder.getCurrentMsgValue());
        child3.setParent(valueHolder.getCurrentMsgValue());
        child3.setName("Child3");
        child3.setBodyValue("3333");
        child3.setBodyBytes(HexService.hex2byte("3333"));
        
        valueHolder.getCurrentMsgValue().getChildren().add(child1);
        valueHolder.getCurrentMsgValue().getChildren().add(child3);

        // setting by ValueHolder
        valueHolder.jumpToChild("Child2").setValue("2222");
        
        // print result
        MsgValue msgValue = valueHolder.jumpToRoot().getCurrentMsgValue();
        String msgFieldStructure = DumpService.getInstance().dumpMsgField(fieldBuilder.getCurrentField());
        String msgValueData = DumpService.getInstance().dumpMsgValue(fieldBuilder.getCurrentField(), msgValue, false);
        byte[] bytes = valueHolder.pack();
        logger.info("Example of a message.\nMessage Structure:\n{}\nMessage data:\n{}\nMessage bytes in hex:\n{}",
            msgFieldStructure, msgValueData, HexService.bytesToHex(bytes));
        
        // unpack again
        MsgField msgField = defineMessageStructure().getCurrentField();
        ValueHolder unpacker = ValueHolder.newInstance(msgField, true);
        unpacker.unpack(bytes);

        assertEquals("3333", unpacker.getValue("Parent", "Child3"));
        assertEquals("2222", unpacker.getValue("Parent", "Child2"));
        assertEquals("1111", unpacker.getValue("Parent", "Child1"));
    }

}
