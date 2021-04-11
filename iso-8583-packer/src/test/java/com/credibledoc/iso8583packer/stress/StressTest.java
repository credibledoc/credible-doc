package com.credibledoc.iso8583packer.stress;

import com.credibledoc.iso8583packer.FieldBuilder;
import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.asciihex.AsciiBodyPacker;
import com.credibledoc.iso8583packer.asciihex.AsciiLengthPacker;
import com.credibledoc.iso8583packer.dump.DumpService;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.message.MsgValue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Example of an empty field in a {@link MsgValue} graph. The test can be used for a stress with a load.
 * 
 * @author Kyrylo Semenko
 */
public class StressTest {
    private static final Logger logger = LoggerFactory.getLogger(StressTest.class);
    private static final int NUM_CHILDREN = 30;
    private static final int NUM_SIBLINGS = 5;

    /**
     * The message structure.
     */
    private FieldBuilder defineMessageStructure() {
        DumpService dumpService = DumpService.getInstance();
        dumpService.setMaxDepthForLogging(3);
        
        FieldBuilder fieldBuilder = FieldBuilder.builder(MsgFieldType.LEN_VAL)
            .defineName("Parent")
            .defineHeaderLengthPacker(AsciiLengthPacker.getInstance(7));

        for (int i = 1; i <= NUM_CHILDREN; i++) {
            fieldBuilder.createChild(MsgFieldType.LEN_VAL)
                .defineName("Child_" + i)
                .defineHeaderLengthPacker(AsciiLengthPacker.getInstance(7))
                .defineBodyPacker(AsciiBodyPacker.getInstance());

            for (int k = 1; k <= NUM_SIBLINGS; k++) {
                fieldBuilder.createSibling(MsgFieldType.LEN_VAL)
                    .defineName("Sibling_" + i + "_" + k)
                    .defineHeaderLengthPacker(AsciiLengthPacker.getInstance(7));
                if (k != NUM_SIBLINGS || i == NUM_CHILDREN) {
                    fieldBuilder.defineBodyPacker(AsciiBodyPacker.getInstance());
                }
            }
        }
        
        fieldBuilder.validateStructure();
        return fieldBuilder;
    }
    
    @Test
    public void packUnpackTest() {
        FieldBuilder fieldBuilder = defineMessageStructure();
        ValueHolder valueHolder = ValueHolder.newInstance(fieldBuilder.getCurrentField(), true);

        long startOfSettings = System.currentTimeMillis();
        List<String> path = new ArrayList<>();
        path.add("Parent");
        List<String> firstPath = new ArrayList<>(path);
        List<String> secondPath = new ArrayList<>(path);
        String expectedFirstValue = null;
        for (int i = 1; i <= NUM_CHILDREN; i++) {
            String childName = "Child_" + i;
            path.add(childName);
            valueHolder.jumpAbsolute(path);
            valueHolder.setValue("value_" + i);

            for (int k = 1; k <= NUM_SIBLINGS; k++) {
                String siblingName = "Sibling_" + i + "_" + k;
                if (k != NUM_SIBLINGS) {
                    expectedFirstValue = "value_" + i + "_" + k;
                    valueHolder.jumpToSibling(siblingName).setValue(expectedFirstValue);
                }
                
                if (k == NUM_SIBLINGS) {
                    valueHolder.jumpToSibling(siblingName);
                    path.remove(path.size() - 1);
                    path.add(siblingName);
                    firstPath.add(siblingName);
                    secondPath.add(siblingName);
                }
            }
        }
        String lastName = firstPath.remove(firstPath.size() - 1);
        firstPath.add(previous(lastName));
        logger.info("Duration of setting in millis: {}", System.currentTimeMillis() - startOfSettings);

        // print result
        MsgValue msgValue = valueHolder.jumpToRoot().getCurrentMsgValue();
        fieldBuilder.jumpToRoot();
        long startPacking = System.currentTimeMillis();
        byte[] bytes = valueHolder.pack();
        long durationOfPacking = System.currentTimeMillis() - startPacking;
        logger.info("Duration of packing: {}", durationOfPacking);
        
        if (durationOfPacking > 5) { // for debugging purposes in case of a high load
            String msgFieldStructure = DumpService.getInstance().dumpMsgField(fieldBuilder.getCurrentField());
            String msgValueData = DumpService.getInstance().dumpMsgValue(fieldBuilder.getCurrentField(), msgValue, false);
            logger.info("Example of a message.\nMessage Structure:\n{}\nMessage data:\n{}\nMessage bytes in hex:\n{}",
                msgFieldStructure, msgValueData, HexService.bytesToHex(bytes));
        }
        
        // unpack again
        long startUnpacking = System.currentTimeMillis();
        ValueHolder unpacker = ValueHolder.newInstance(fieldBuilder, true);
        unpacker.unpack(bytes);
        logger.info("Duration of unpacking: {}", System.currentTimeMillis() - startUnpacking);
        
        long startGetting = System.currentTimeMillis();
        Object firstValue = unpacker.getValue(firstPath);
        Object secondValue = unpacker.getValue(secondPath);
        logger.info("Duration of getting: {}", System.currentTimeMillis() - startGetting);
        
        assertEquals(expectedFirstValue, firstValue);
        assertNull(secondValue);
    }

    private String previous(String lastName) {
        String pattern = "_";
        int lastIndex = lastName.lastIndexOf(pattern);
        int trailer = Integer.parseInt(lastName.substring(lastIndex + pattern.length()));
        int previous = trailer - 1;
        return lastName.substring(0, lastIndex) + pattern + previous;
    }

}
