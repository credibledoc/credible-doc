package com.credibledoc.iso8583packer.repeated;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker;
import com.credibledoc.iso8583packer.asciihex.AsciiStringTagPacker;
import com.credibledoc.iso8583packer.bcd.BcdIntBodyPacker;
import com.credibledoc.iso8583packer.body.BodyPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.hex.HexBodyPacker;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

// TODO Kyrylo Semenko - complete the test cases
public class RepeatedFieldsTest {
    private static final Logger logger = LoggerFactory.getLogger(RepeatedFieldsTest.class);

    @Test
    @Ignore
    public void packTagLenVal() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.TAG_LEN_VAL)
            .defineHeaderTag("WW=");
        fieldBuilder.validateStructure();

    }

    @Test
    @Ignore
    public void packTagLenValParentDef() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.TAG_LEN_VAL);
        fieldBuilder.validateStructure();

    }

    /**
     * Used in documentation
     */
    @Test
    public void packTagVal() {
        String root = "Root";
        String product = "Product";
        String productCode = "Code";
        String productAmount = "Amount";
        FieldBuilder msg = FieldBuilder.builder(MsgFieldType.MSG).defineName(root);
        FieldBuilder fieldBuilder = FieldBuilder.from(msg.getCurrentField())
            .createChild(MsgFieldType.TAG_VAL)
            .defineHeaderTag("Item")
            .defineName(product)
            .defineLen(8)
            .defineHeaderTagPacker(AsciiStringTagPacker.getInstance(4))

            .createChild(MsgFieldType.VAL)
            .defineName(productCode)
            .defineLen(6)
            .defineBodyPacker(AsciiBodyPacker.getInstance())

            .cloneToSibling()
            .defineName(productAmount)
            .defineBodyPacker(BcdIntBodyPacker.getInstance(2))
            .defineLen(2)
            .jumpToRoot();
        fieldBuilder.validateStructure();
        String msgFieldDump = DumpService.getInstance().dumpMsgField(fieldBuilder.getCurrentField());
        logger.info("MsgField structure dump: \n{}\nEnd of MsgField dump", msgFieldDump);

        MsgField msgField = fieldBuilder.getCurrentField();
        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder);
        
        // set values
        valueHolder.jumpAbsolute(root, product, productCode).setValue("code01")
            .jumpToSibling(productAmount).setValue(1);
        
        valueHolder.jumpToParent().cloneSibling().jumpToChild(productCode).setValue("code02")
            .jumpToSibling(productAmount).setValue(8);

        // pack to bytes
        byte[] bytes = valueHolder.jumpToRoot().pack();

        // unpack from bytes
        MsgValue unpackedMsgValue = ValueHolder.newInstance(msgField).unpack(bytes);
        List<MsgValue> products = unpackedMsgValue.getChildren();
        assertEquals(2, products.size());
        
        ValueHolder firstProduct = ValueHolder.newInstance(products.get(0), msgField.getChildren().get(0));
        String firstProductCode = firstProduct.jumpToChild(productCode).getValue(String.class);
        assertEquals("code01", firstProductCode);
        int firstProductAmount = firstProduct.jumpToSibling(productAmount).getValue(Integer.class);
        assertEquals(1, firstProductAmount);

        ValueHolder secondProduct = ValueHolder.newInstance(products.get(1), msgField.getChildren().get(0));
        String secondProductCode = secondProduct.jumpToChild(productCode).getValue(String.class);
        assertEquals("code02", secondProductCode);
        int secondProductAmount = secondProduct.jumpToSibling(productAmount).getValue(Integer.class);
        assertEquals(8, secondProductAmount);

        String dump = DumpService.getInstance().dumpMsgValue(msgField, unpackedMsgValue, false);
        logger.info("MsgValue with repeated fields: \n{}\nBytes: {}\nEnd of example", dump, HexService.bytesToHex(bytes));
    }

    @Test
    @Ignore
    public void packTagValParentDef() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.TAG_VAL);
        fieldBuilder.validateStructure();

    }

    @Test
    @Ignore
    public void packLenVal() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.LEN_VAL);
        fieldBuilder.validateStructure();

    }

    @Test
    @Ignore
    public void packLenValParentDef() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.LEN_VAL);
        fieldBuilder.validateStructure();

    }

    @Test
    @Ignore
    public void packLenTagVal() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.LEN_TAG_VAL);
        fieldBuilder.validateStructure();

    }

    @Test
    @Ignore
    public void packLenTagValParentDef() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.LEN_TAG_VAL);
        fieldBuilder.validateStructure();

    }

    @Test
    @Ignore
    public void packVal() {
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.VAL);
        fieldBuilder.validateStructure();
        // TODO Kyrylo Semenko - complete all tests

    }
    
}
