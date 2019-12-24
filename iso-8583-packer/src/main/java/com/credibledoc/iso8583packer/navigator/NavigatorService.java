package com.credibledoc.iso8583packer.navigator;

import com.credibledoc.iso8583packer.message.*;
import com.credibledoc.iso8583packer.tag.TagPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;

import java.util.List;
import java.util.Objects;

/**
 * This static helper contains methods for navigation (jumping) inside the {@link MsgField}s
 * graphs.
 * 
 * @author Kyrylo Semenko
 */
public class NavigatorService {
    /**
     * Please do not create instances of this static helper.
     */
    private NavigatorService() {
        // empty
    }

    /**
     * Find <b>first</b> {@link Msg} with the name.
     *
     * @param msgList where for search
     * @param name    what to search
     * @param <T>  the {@link Msg} type, {@link MsgField} or {@link MsgValue}
     * @return 'null' if not found
     */
    @SuppressWarnings("unchecked")
    public static <T extends Msg> T findByName(List<? extends Msg> msgList, String name) {
        if (name == null) {
            return null;
        }
        if (msgList == null) {
            return null;
        }
        for (Msg nextMsgField : msgList) {
            if (name.equals(nextMsgField.getName())) {
                return (T) nextMsgField;
            }
        }
        return null;
    }

    public static MsgField getChildOrThrowException(String childName, MsgField currentMsgField) {
        List<MsgField> msgFields = currentMsgField.getChildren();
        MsgField child = findByName(msgFields, childName);
        if (child == null) {
            throw new PackerRuntimeException("Field with name '" + getPathRecursively(currentMsgField) +
                    "' has no child with name : '" + childName + "'. Current field: " + currentMsgField);
        }
        return child;
    }

    /**
     * Generate field name, for example 11 or 48(FFEE2E) or just 5F2A.
     * @param current focused node in the object graph
     * @return 'null' if name nor num has been set.
     */
    public static String generatePath(Msg current) {
        if (current.getTagNum() != null) {
            if (current.getName() != null) {
                return current.getName() + "(" + current.getTagNum() + ")";
            }
            return String.valueOf(current.getTagNum());
        }
        if (current.getName() == null && current instanceof MsgField) {
            MsgField msgField = (MsgField) current;
            return msgField.getType().toString();
        }
        return current.getName();
    }

    /**
     * Call the {@link #generatePath(Msg)} method recursively.
     * @param current focused node in the object graph
     * @param prefix can be 'null'. If exists, a result will contain this prefix.
     * @return 'null' if name nor num has been set.
     */
    private static String generatePathRecursively(Msg current, String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        if (current.getParent() != null) {
            prefix = generatePathRecursively(current.getParent(), prefix);
            prefix = prefix + "." + generatePath(current);
        } else {
            prefix = prefix + generatePath(current);
        }
        return prefix;
    }

    public static String getPathRecursively(Msg msg) {
        if (msg == null) {
            return null;
        }
        return generatePathRecursively(msg, null);
    }

    /**
     * Find the parent recursively.
     * @param msg source object graph
     * @param <T> the {@link Msg} type, {@link MsgField} or {@link MsgValue}
     * @return The root node of the graph
     */
    @SuppressWarnings("unchecked")
    public static <T extends Msg> T findRoot(T msg) {
        if (msg.getParent() == null) {
            return msg;
        }
        return (T)findRoot(msg.getParent());
    }

    public static TagPacker getTagPackerFromParent(MsgField msgField) {
        if (MsgFieldType.isNotTaggedType(msgField)) {
            return null;
        }
        if (msgField.getParent() != null) {
            return msgField.getParent().getChildrenTagPacker();
        }
        throw new PackerRuntimeException("This field '" + getPathRecursively(msgField) +
                "' has no parent. The parent is mandatory for obtaining of the ChildrenTagPacker property. " +
                "Please create a new Field and set it as a parent. Parent is not mandatory for fields which " +
                "contains the bitSet property.");
    }

    public static MsgField getSiblingOrThrowException(String siblingName, MsgField currentMsgField) {
        if (currentMsgField.getParent() == null) {
            throw new PackerRuntimeException("Field '" + getPathRecursively(currentMsgField) +
                    "' has no parent, hence it cannot have a sibling with name '" + siblingName + "'");
        }
        MsgField parentMsgField = currentMsgField.getParent();
        return getChildOrThrowException(siblingName, parentMsgField);
    }

    /**
     * Jump to msgField root and search for the <b>first</b> child with the same name and tagNum as the field from argument.
     * 
     * @param msgField where to search
     * @param msgValue what to search
     * @return The found msgField or thrown an exception
     */
    public static MsgField findByNameAndTagNumOrThrowException(MsgField msgField, MsgValue msgValue) {
        MsgField rootMsgField = findRoot(msgField);
        MsgField result = findInGraphRecurrently(msgValue, rootMsgField);
        if (result == null) {
            throw new PackerRuntimeException("Cannot find msgField for this field: " + getPathRecursively(msgValue));
        }
        return result;
    }

    private static MsgField findInGraphRecurrently(MsgValue msgValue, MsgField msgField) {
        if (isValueFitToField(msgValue, msgField)) {
            return msgField;
        }
        for (MsgField child : msgField.getChildren()) {
            MsgField nextResult = findInGraphRecurrently(msgValue, child);
            if (nextResult != null) {
                return nextResult;
            }
        }
        return null;
    }

    private static boolean isValueFitToField(MsgValue msgValue, MsgField msgField) {
        return Objects.equals(msgValue.getName(), msgField.getName()) &&
                Objects.equals(msgValue.getTagNum(), msgField.getTagNum());
    }

    public static MsgValue newFromNameAndTagNum(MsgField msgField) {
        MsgValue msgValue = new MsgValue();
        msgValue.setName(msgField.getName());
        msgValue.setTagNum(msgField.getTagNum());
        return msgValue;
    }

    public static void validateSameNamesAndTagNum(MsgPair msgPair) {
        MsgField msgField = msgPair.getMsgField();
        MsgValue msgValue = msgPair.getMsgValue();
        boolean namesEqual = Objects.equals(msgValue.getName(), msgField.getName());
        boolean tanNumsEqual = Objects.equals(msgValue.getTagNum(), msgField.getTagNum());
        if (!namesEqual || !tanNumsEqual) {
            String cause;
            if (!namesEqual && !tanNumsEqual) {
                cause = "names and tagNums";
            } else if (!namesEqual) {
                cause = "names";
            } else {
                cause = "tagNums";
            }
            throw new PackerRuntimeException("The MsgField and its msgValue does not fit to each other because " +
                    "they have different " + cause + ". MsgValue: '" + getPathRecursively(msgValue) +
                    "'. MsgField: '" + getPathRecursively(msgField) + "'. " +
                    "Please navigate to correct msgField. For this purpose use FieldBuilder.jump** methods.");
        }
    }
}
