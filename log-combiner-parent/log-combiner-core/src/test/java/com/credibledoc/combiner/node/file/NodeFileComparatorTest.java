package com.credibledoc.combiner.node.file;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class NodeFileComparatorTest {

    @Test
    public void compare() {
        NodeFile older = new NodeFile();
        older.setDate(new Date(1));

        NodeFile younger = new NodeFile();
        younger.setDate(new Date(2));
        
        assertEquals(1, NodeFileComparator.getInstance().compare(younger, older));
    }
}
