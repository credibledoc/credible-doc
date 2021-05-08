package com.credibledoc.iso8583packer.message;

import com.credibledoc.iso8583packer.ValueHolder;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.navigator.Navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The class provides an interface for creating an object from an array of bytes and,
 * conversely, for creating an array of bytes from an object.
 * 
 * @author Kyrylo Semenko
 */
public class IsoMsg {
    
    /**
     * System Trace Audit Number
     */
    public static final int FIELD_STAN_11 = 11;
    
    /**
     * Card Acceptor Terminal ID ('TID')
     */
    public static final int FIELD_TERMINAL_ID_41 = 41;
    
    /**
     * Service for packing an object into bytes and unpacking bytes into the object.
     */
    protected ValueHolder valueHolder;

    /**
     * Call the {@link ValueHolder#newInstance(MsgField, boolean)} method with the second parameter value 'true'.
     * @param msgField used as the first parameter for the above method
     */
    public void setPackager(MsgField msgField) {
        if (msgField.getRoot() == null) {
            msgField.setRoot(msgField);
        }
        valueHolder = ValueHolder.newInstance(msgField, true);
    }

    /**
     * @return The root {@link ValueHolder#getCurrentMsgField()} node.
     */
    public MsgField getPackager() {
        return valueHolder.jumpToRoot().getCurrentMsgField();
    }

    /**
     * Call the {@link ValueHolder#pack()} method.
     * @return See the {@link ValueHolder#pack()} method description.
     */
    public byte[] pack() {
        valueHolder.jumpToRoot();
        return valueHolder.pack();
    }

    /**
     * Call the {@link ValueHolder#unpack(byte[])} method.
     * <p>
     * After the method invocation a {@link ValueHolder#getCurrentMsgValue()} will be instantiated and filled with 
     * objects.
     * Then getXXX methods can be used for the access to its values.
     *
     * @param bytes see the {@link ValueHolder#unpack(byte[])} method description.
     */
    public void unpack(byte[] bytes) {
        valueHolder.unpack(bytes);
    }

    /**
     * Call the {@link ValueHolder#setValue(Object, List)} method.
     * @param bodyValue see the {@link ValueHolder#setValue(Object, List)} method description
     * @param absolutePath see the {@link ValueHolder#setValue(Object, List)} method description
     */
    public void setValue(Object bodyValue, List<String> absolutePath) {
        valueHolder.setValue(bodyValue, absolutePath);
    }

    /**
     * Call the {@link ValueHolder#getValue(List)} method.
     * @param absolutePath see the {@link ValueHolder#getValue(List)} method description
     * @param <T> see the {@link ValueHolder#getValue(List)} method description
     * @return See the {@link ValueHolder#getValue(List)} method description.
     */
    public <T> T get(List<String> absolutePath) {
        return valueHolder.getValue(absolutePath);
    }

    /**
     * Call the {@link ValueHolder#setValue(Object, String...)} method.
     * @param bodyValue see the {@link ValueHolder#setValue(Object, String...)} method description
     * @param absolutePath the {@link ValueHolder#setValue(Object, String...)} method description
     */
    public void set(Object bodyValue, String... absolutePath) {
        valueHolder.setValue(bodyValue, absolutePath);
    }

    /**
     * Call the {@link ValueHolder#getValue(String...)} method.
     * @param absolutePath see the {@link ValueHolder#getValue(String...)} method description.
     * @param <T> see the {@link ValueHolder#getValue(String...)} method description.
     * @return see the {@link ValueHolder#getValue(String...)} method description.
     */
    public <T> T get(String... absolutePath) {
        return valueHolder.getValue(absolutePath);
    }

    /**
     * Find the first BitMap and set the field value.
     * @param fieldNum the BitMap child's {@link MsgField#getFieldNum()}.
     * @param bodyValue the {@link MsgValue#setBodyValue(Object)} property.
     */
    public void set(int fieldNum, Object bodyValue) {
        valueHolder.jumpToRoot();
        List<String> path = findBitSet(valueHolder.getCurrentMsgField());
        valueHolder.jumpAbsolute(path);
        MsgValue bitSet = valueHolder.getCurrentMsgValue();
        if (bitSet == null) {
            throw new PackerRuntimeException("Field with number '" + fieldNum + "' cannot be found, " +
                "because BitMap not found.");
        }
        for (MsgField msgField : valueHolder.getCurrentMsgField().getChildren()) {
            if (msgField.getFieldNum() != null && fieldNum == msgField.getFieldNum()) {
                valueHolder.jumpToChild(msgField.getName());
                valueHolder.setValue(bodyValue);
                return;
            }
        }
        throw new PackerRuntimeException("Field '" + fieldNum + "' cannot be set, " +
            "because BitMap has no field with such number.");
    }

    /**
     * Find the first BitMap and return the field value.
     * @param fieldNum the BitMap child's {@link MsgField#getFieldNum()}.
     * @param <T> the return type, see the {@link com.credibledoc.iso8583packer.body.BodyPacker} description.
     * @return The {@link MsgValue#getBodyValue()} casted to the type.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(int fieldNum) {
        valueHolder.jumpToRoot();
        List<String> path = findBitSet(valueHolder.getCurrentMsgField());
        valueHolder.jumpAbsolute(path);
        MsgValue bitSet = valueHolder.getCurrentMsgValue();
        if (bitSet == null) {
            return null;
        }
        for (MsgValue msgValue : bitSet.getChildren()) {
            if (msgValue.getFieldNum() != null && fieldNum == msgValue.getFieldNum()) {
                Object bodyValue = msgValue.getBodyValue();
                if (bodyValue == null) {
                    return null;
                }
                return (T) msgValue.getBodyValue();
            }
        }
        return null;
    }

    /**
     * Set the value to BitMap child's {@link MsgValue#setBodyValue(Object)}.
     * @param fieldName the name of the child of the first BitMap
     * @param bodyValue to be set to the BitMap's child
     */
    public void set(String fieldName, Object bodyValue) {
        List<String> path = resolveAbsolutePath(fieldName);
        valueHolder.jumpAbsolute(path);
        valueHolder.setValue(bodyValue);
    }

    /**
     * Get a value of the BitMap child's {@link MsgValue#setBodyValue(Object)}.
     * @param fieldName the name of the child of the first BitMap
     * @param <T> to be used for casting the returned value
     * @return the {@link MsgValue#getBodyValue(Class)} casted to T or 'null'.
     */
    public <T> T get(String fieldName) {
        List<String> path = resolveAbsolutePath(fieldName);
        return valueHolder.getValue(path);
    }

    /**
     * Set the Message Type indicator value.
     * @param value the MTI, for example 0200
     */
    public void setMti(String value) {
        valueHolder.jumpToRoot();
        List<String> path = resolveMtiPath(valueHolder.getCurrentMsgField());
        if (!path.isEmpty()) {
            valueHolder.setValue(value, path);
        } else {
            throw new PackerRuntimeException("Cannot find a field with name MTI");
        }
    }

    /**
     * @return The Message Type indicator value.
     */
    public String getMti() {
        valueHolder.jumpToRoot();
        List<String> path = resolveMtiPath(valueHolder.getCurrentMsgField());
        if (!path.isEmpty()) {
            return valueHolder.getValue(path);
        }
        return null;
    }

    /**
     * Set the {@link #FIELD_STAN_11} value.
     * @param value to be set
     */
    public void setStan(String value) {
        set(FIELD_STAN_11, value);
    }

    /**
     * @return The {@link #FIELD_STAN_11} value.
     */
    public String getStan() {
        return get(FIELD_STAN_11);
    }

    /**
     * Set the {@link #FIELD_TERMINAL_ID_41} value.
     * @param value to be set
     */
    public void setTerminalId(String value) {
        set(FIELD_TERMINAL_ID_41, value);
    }

    /**
     * @return The {@link #FIELD_TERMINAL_ID_41} value.
     */
    public String getTerminalId() {
        return get(FIELD_TERMINAL_ID_41);
    }

    /**
     * @return The {@link #valueHolder} field value.
     */
    public ValueHolder getValueHolder() {
        return valueHolder;
    }

    /**
     * Find the {@link MsgValue} node in the {@link #valueHolder}. If the msgValue not exists yet, create a new one,
     * as defined in the {@link ValueHolder#getCurrentMsgField()}.
     * <p>
     * If the msgField definition has no child with such absolutePath, throw an exception.
     *
     * @param absolutePath absolute path to the {@link MsgField} in the object graph
     * @return The found or newly created {@link MsgValue} from the {@link #valueHolder}.
     */
    public MsgValue getMsgValue(List<String> absolutePath) {
        // store actual position
        MsgPair msgPair = getValueHolder().getCurrentPair();

        getValueHolder().jumpAbsolute(absolutePath);
        MsgValue result = getValueHolder().getCurrentMsgValue();
        
        // restore actual position
        getValueHolder().setCurrent(msgPair.getMsgField(), msgPair.getMsgValue());
        
        return result;
    }

    /**
     * Call the {@link #getMsgValue(List)} method
     * @param absolutePath see the {@link #getMsgValue(List)} method description
     * @return See the {@link #getMsgValue(List)} method description
     */
    public MsgValue getMsgValue(String... absolutePath) {
        return getMsgValue(Arrays.asList(absolutePath));
    }

    private List<String> findBitSet(MsgField msgField) {
        if (msgField.getBitMapPacker() != null) {
            return resolveAbsolutePath(msgField);
        }
        if (msgField.getChildren() != null) {
            for (MsgField child : msgField.getChildren()) {
                List<String> path = findBitSet(child);
                if (!path.isEmpty()) {
                    return path;
                }
            }
        }
        return Collections.emptyList();
    }

    private List<String> resolveAbsolutePath(MsgField msgField) {
        List<String> path = new ArrayList<>();
        path.add(msgField.getName());
        while (msgField.getParent() != null) {
            msgField = msgField.getParent();
            path.add(msgField.getName());
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Find in BitMap only
     * @param fieldName the BitMap child name
     * @return The path from the root field
     */
    private List<String> resolveAbsolutePath(String fieldName) {
        valueHolder.jumpToRoot();
        List<String> path = findBitSet(valueHolder.getCurrentMsgField());
        valueHolder.jumpAbsolute(path);
        MsgValue bitSet = valueHolder.getCurrentMsgValue();
        if (bitSet == null) {
            throw new PackerRuntimeException("Field with name '" + fieldName + "' cannot be found, " +
                "because BitMap not found.");
        }
        path.add(fieldName);
        return path;
    }

    private List<String> resolveMtiPath(MsgField msgField) {
        boolean containsMti = msgField.getName() != null && msgField.getName().toLowerCase().contains("mti");
        if (containsMti) {
            return resolveAbsolutePath(msgField);
        }
        if (msgField.getChildren() != null) {
            for (MsgField child : msgField.getChildren()) {
                List<String> path = resolveMtiPath(child);
                if (!path.isEmpty()) {
                    return path;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Search the first {@link MsgFieldType#BIT_SET} {@link MsgValue} in the {@link #valueHolder}
     * and return the last child's {@link MsgValue#getFieldNum()}.
     * Only children with existing bodyValue or bodyBytes will be searched.
     * @return The fieldNum or -1 if the non-empty child cannot be found or the child has no fieldNum.
     */
    public int getMaxField() {
        Navigator navigator = getValueHolder().getNavigator();
        MsgField root = navigator.findRoot(getValueHolder().getCurrentMsgField());
        
        List<String> firstBitSetPath = findBitSet(root);
        if (firstBitSetPath.isEmpty()) {
            return -1;
        }
        MsgValue firstBitSet = getMsgValue(firstBitSetPath);
        if (firstBitSet.getChildren() == null || firstBitSet.getChildren().isEmpty()) {
            return -1;
        }

        for (int i = firstBitSet.getChildren().size() - 1; i >= 0; i--) {
            MsgValue msgValue = firstBitSet.getChildren().get(i);
            if (msgValue.getBodyValue() != null || msgValue.getBodyBytes() != null) {
                if (msgValue.getFieldNum() == null) {
                    return -1;
                }
                return msgValue.getFieldNum();
            }
        }
        return -1;
    }

    /**
     * Call the {@link com.credibledoc.iso8583packer.dump.Visualizer#dumpMsgValue(MsgField, MsgValue, boolean)} method
     * of the current {@link #valueHolder} visualizer.
     * @param maskPrivateData see the
     * {@link com.credibledoc.iso8583packer.dump.Visualizer#dumpMsgValue(MsgField, MsgValue, boolean)} method description.
     * @return Visualized root {@link MsgValue} of the {@link #valueHolder}. 
     */
    public String dumpMsgValue(boolean maskPrivateData) {
        Navigator navigator = valueHolder.getNavigator();
        MsgField rootMsgField = navigator.findRoot(valueHolder.getCurrentMsgField());
        MsgValue rootMsgValue = navigator.findRoot(valueHolder.getCurrentMsgValue());
        return getValueHolder().getVisualizer().dumpMsgValue(rootMsgField, rootMsgValue, maskPrivateData);
    }

    /**
     * Call the {@link com.credibledoc.iso8583packer.dump.Visualizer#dumpMsgField(MsgField)} method.
     * @return Visualized root {@link MsgField} of the {@link #valueHolder}.
     */
    public String dumpMsgField() {
        Navigator navigator = valueHolder.getNavigator();
        MsgField rootMsgField = navigator.findRoot(valueHolder.getCurrentMsgField());
        return getValueHolder().getVisualizer().dumpMsgField(rootMsgField);
    }
}
