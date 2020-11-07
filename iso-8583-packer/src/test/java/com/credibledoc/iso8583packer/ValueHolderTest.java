package com.credibledoc.iso8583packer;

import com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void jumpAbsoluteTest() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("1")
            
            .createChild(MsgFieldType.MSG)
            .defineName("2")
            
            .createChild(MsgFieldType.VAL)
            .defineName("3")
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            .defineLen(1);

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.jumpToRoot().getCurrentField());
        valueHolder.jumpAbsolute(Arrays.asList("1", "2", "3")).setValue("3");

        assertEquals("3", valueHolder.jumpAbsolute(Arrays.asList("1", "2", "3")).getValue());
    }

    @Test
    public void getValueFromAbsolutePathTest() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("1")
            
            .createChild(MsgFieldType.MSG)
            .defineName("2")
            
            .createChild(MsgFieldType.VAL)
            .defineName("3")
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            .defineLen(1);

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.jumpToRoot().getCurrentField());
        
        valueHolder.setValue("3", Arrays.asList("1", "2", "3"));
        assertEquals("1", valueHolder.getCurrentMsgField().getName());

        assertEquals("3", valueHolder.getValue(Arrays.asList("1", "2", "3")));
    }

    @Test
    public void hasValueTest() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("1")

            .createChild(MsgFieldType.MSG)
            .defineName("2")

            .createChild(MsgFieldType.VAL)
            .defineName("3")
            .defineBodyPacker(AsciiBodyPacker.getInstance())
            .defineLen(1);

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.jumpToRoot().getCurrentField());

        valueHolder.jumpAbsolute(Arrays.asList("1", "2", "3")).setValue("3");
        assertTrue(valueHolder.hasValue(Arrays.asList("1", "2", "3")));

        assertEquals("3", valueHolder.getCurrentMsgValue().getName());
        valueHolder.setValue(null);
        assertFalse(valueHolder.hasValue(Arrays.asList("1", "2", "3")));
    }

    @Test(expected = PackerRuntimeException.class)
    public void newInstanceNullTest() {
        MsgField msgField = null;
        ValueHolder.newInstance(msgField);
    }
}
