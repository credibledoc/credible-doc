package com.credibledoc.iso8583packer.tv;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.ebcdic.EbcdicBodyPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalLengthPacker;
import com.credibledoc.iso8583packer.ebcdic.EbcdicDecimalTagPacker;
import com.credibledoc.iso8583packer.hex.HexBodyPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link MsgFieldType#TAG_VAL} packing and unpacking
 */
public class TvTest {

    private static final Logger logger = LoggerFactory.getLogger(TvTest.class);

    @Test
    public void tvTest() {
        // definition
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("msg")
            .defineChildrenTagPacker(EbcdicDecimalTagPacker.getInstance(2))
            .defineChildrenBodyPacker(EbcdicBodyPacker.getInstance())
            .defineChildrenBodyLen(2)

            .createChild(MsgFieldType.TAG_VAL)
            .defineHeaderTag(1)
            .defineName("tag-1")

            .createSibling(MsgFieldType.TAG_VAL)
            .defineHeaderTag(3)
            .defineName("tag-3");

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder, true);
        
        valueHolder.setValue("11", "msg", "tag-1");
        valueHolder.setValue("33", "msg", "tag-3");
        
        byte[] bytes = valueHolder.pack();
        String hex = HexService.bytesToHex(bytes);
        String expectedHex = "F0F1F1F1F0F3F3F3";
        assertEquals(expectedHex, hex);
        
        // Unpacking undefined tags between tag-1 and tag-3 and after tag-3
        String incomingHex = "F0F1F1F1 F0F2F2F2 F0F3F3F3 F0F4F4F4";
        byte[] incomingBytes = HexService.hex2byte(incomingHex);
        ValueHolder incomingValueHolder = ValueHolder.newInstance(fieldBuilder, true);
        incomingValueHolder.unpack(incomingBytes);
        
        assertEquals("11", incomingValueHolder.getValue("msg", "tag-1"));
        assertEquals("33", incomingValueHolder.getValue("msg", "tag-3"));

        // Unpacking undefined tags in a different order, tag-3, undefined, undefined, tag-1
        String incomingHexUnordered = "F0F3F3F3 F0F2F2F2 F0F4F4F4 F0F1F1F1";
        byte[] incomingBytesUnordered = HexService.hex2byte(incomingHexUnordered);
        ValueHolder incomingValueHolderUnordered = ValueHolder.newInstance(fieldBuilder, true);
        incomingValueHolderUnordered.unpack(incomingBytesUnordered);
        MsgValue rootMsgValue = incomingValueHolderUnordered.getCurrentMsgValue();
        assertEquals(2, rootMsgValue.getUndefinedChildrenMap().size());

        assertEquals("11", incomingValueHolderUnordered.getValue("msg", "tag-1"));
        assertEquals("33", incomingValueHolderUnordered.getValue("msg", "tag-3"));
        MsgValue root = incomingValueHolderUnordered.jumpToRoot().getCurrentMsgValue();
        assertEquals(2, root.getUndefinedChildrenMap().size());
        assertEquals("22", root.getUndefinedChildrenMap().get("tag-3-clone-1").getBodyValue());
        assertEquals(2, root.getUndefinedChildrenMap().get("tag-3-clone-1").getTag());

        MsgField rootMsgField = incomingValueHolderUnordered.getCurrentMsgField();
        logger.info("MsgField:\n{}", DumpService.getInstance().dumpMsgField(rootMsgField));
        logger.info("MsgValue:\n{}", DumpService.getInstance().dumpMsgValue(rootMsgField, rootMsgValue, false));
    }

    @Test
    public void tvRepeatedTest() {
        // definition
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.MSG)
            .defineName("msg")
            .defineChildrenTagPacker(EbcdicDecimalTagPacker.getInstance(2))
            .defineChildrenBodyPacker(EbcdicBodyPacker.getInstance())
            .defineChildrenBodyLen(2)

            .createChild(MsgFieldType.TAG_VAL)
            .defineHeaderTag(1)
            .defineName("tag-1");

        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder, true);

        valueHolder.jumpAbsolute("msg", "tag-1");
        valueHolder.setValue("11");
        valueHolder.cloneSibling();
        valueHolder.setValue("33");
        
        valueHolder.jumpToRoot();
        byte[] bytes = valueHolder.pack();
        String hex = HexService.bytesToHex(bytes);
        String expectedHex = "F0F1F1F1 F0F1F3F3";
        assertEquals(expectedHex.replace(" ", ""), hex);

        ValueHolder unpackedValueHolder = ValueHolder.newInstance(fieldBuilder, true);
        unpackedValueHolder.unpack(HexService.hex2byte(hex));
        assertEquals(2, valueHolder.getCurrentMsgValue().getChildren().size());
        List<MsgValue> children = unpackedValueHolder.jumpAbsolute("msg").getCurrentMsgValue().getChildren();
        assertEquals("11", children.get(0).getBodyValue(String.class));
        assertEquals("tag-1", children.get(0).getName());
        assertEquals("33", children.get(1).getBodyValue(String.class));
        assertEquals("tag-1", children.get(1).getName());
    }
    
}
