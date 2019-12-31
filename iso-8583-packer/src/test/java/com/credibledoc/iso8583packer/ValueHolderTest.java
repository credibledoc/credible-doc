package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.message.MsgFieldType;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class ValueHolderTest {

    @Test
    public void getCurrentMsgValueTest() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.VAL)
            .defineName("field");

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField());
        
        assertNotNull(valueHolder);
        assertNotNull(valueHolder.getCurrentMsgValue());
        assertNotNull(valueHolder.getCurrentMsgField());
        assertEquals("field", valueHolder.getCurrentMsgField().getName());
        assertEquals("field", valueHolder.getCurrentMsgValue().getName());
    }

    @Test
    public void setChildrenTest() {
        FieldBuilder parentFieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("parent");

        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.VAL)
            .defineName("field")
            .defineParent(parentFieldBuilder.getCurrentField());

        ValueHolder fieldValueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField());
        ValueHolder parentValueHolder = ValueHolder.newInstance(parentFieldBuilder.getCurrentField());

        parentValueHolder.setChildren(Collections.singletonList(fieldValueHolder.getCurrentMsgValue()));
        
        assertNotNull(parentValueHolder.getCurrentMsgValue().getChildren());
        assertEquals(fieldValueHolder.getCurrentMsgValue(), parentValueHolder.getCurrentMsgValue().getChildren().get(0));
    }

}
