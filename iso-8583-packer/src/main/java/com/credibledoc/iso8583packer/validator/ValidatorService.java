package com.credibledoc.iso8583packer.validator;

import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.navigator.Navigator;
import com.credibledoc.iso8583packer.tag.TagPacker;

import java.util.List;

/**
 * The service is used for validation the {@link com.credibledoc.iso8583packer.message.MsgField} structure.
 * 
 * @author Kyrylo Semenko
 */
public class ValidatorService implements Validator {
    
    protected Navigator navigator;
    protected Visualizer visualizer;

    @Override
    public void validateStructure(MsgField current) {
        validateStructureRecursively(current);
    }

    /**
     * Check restrictions of the Field structure and all its children recursively.
     *
     * @param msgField the {@link MsgField} to be checked.
     */
    protected void validateStructureRecursively(MsgField msgField) {
        if (msgField == null) {
            throw new PackerRuntimeException("Field is null");
        }

        String path = navigator.getPathRecursively(msgField);

        if (msgField.getType() == null) {
            throw new PackerRuntimeException("Please set the " + MsgFieldType.class.getSimpleName() +
                " value to the field with path: '" + path + "'");
        }

        if (msgField.getTag() == null && msgField.getName() == null && msgField.getType() != MsgFieldType.VAL) {
            throw new PackerRuntimeException("At least one 'tag' or 'name' should be set to the field " +
                    "but the both properties are 'null'. Field path: '" + path + "'");
        }

        if (msgField.getBodyPacker() == null && msgField.getChildren() == null &&
                msgField.getType() != MsgFieldType.BIT_SET && msgField.getType() != MsgFieldType.MSG) {
            throw new PackerRuntimeException("Please call the 'defineBodyPacker(...)' method " +
                    "because the 'BodyPacker' value is mandatory. " +
                    "Field path: '" + path + "'.");
        }

        if (MsgFieldType.isTaggedType(msgField)) {
            validateTagAndTagPackerExists(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.TAG_LEN_VAL || msgField.getType() == MsgFieldType.LEN_TAG_VAL) {
            validateHeaderOrParentLengthPackerExists(msgField, path);
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.TAG_VAL) {
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.LEN_VAL) {
            validateHeaderOrParentLengthPackerExists(msgField, path);
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.VAL) {
            validateLenExists(msgField, path);
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.BIT_SET) {
            validateBitSetAndBitMapPackerExists(msgField, path);
            validateChildrenExists(msgField, path);
        }

        if (msgField.getType() == MsgFieldType.VAL || msgField.getType() == MsgFieldType.TAG_VAL) {
            validateLenExists(msgField, path);
        }

        if (msgField.getType() == MsgFieldType.MSG) {
            validateHasNoBitSetAndBitMapPacker(msgField, path);
            validateHasNoLenDefined(msgField, path);
        }
        
        List<MsgField> msgFields = msgField.getChildren();
        if (msgFields != null) {
            for (MsgField nextMsgField : msgFields) {
                validateStructureRecursively(nextMsgField);
            }
        }
    }

    private void validateHasNoLenDefined(MsgField msgField, String path) {
        if (msgField.getLen() != null) {
            throw new PackerRuntimeException("The 'len' property is not allowed for the MsgField with path '" + path +
                "' with the '" + MsgFieldType.class.getSimpleName() + "." + msgField.getType() + "' type " +
                "because the 'len' property is not used in such field type.");
        }
    }

    protected void validateHasNoBitSetAndBitMapPacker(MsgField msgField, String path) {
        if (msgField.getBitMapPacker() != null) {
            throw new PackerRuntimeException("BitMapPacker is not allowed for MsgField '" + path +
                    "' with MsgType '" + msgField.getType() + "' because that doesn't make sense.");
        }
    }

    protected void validateTagAndTagPackerExists(MsgField msgField, String path) {
        boolean parentTagPackerExists = msgField.getParent() != null && msgField.getParent().getChildrenTagPacker() != null;
        if (parentTagPackerExists && msgField.getTagPacker() != null) {
            String parentPath = navigator.getPathRecursively(msgField.getParent());
            throw new PackerRuntimeException("Only one TagPacker definition is allowed " +
                "for the MsgField with path '" + path +
                "', but found its parent childrenTagPacker with path '" + parentPath +
                "'. Please chose only one TagPacker.");
        }
        
        boolean tagPackerExists = parentTagPackerExists || msgField.getTagPacker() != null;
        boolean tagExists = msgField.getTag() != null;

        if (!tagPackerExists) {
            String parentPath = navigator.getPathRecursively(msgField.getParent());
            String parentString = parentPath == null ? "" : " or its parent with path '" + parentPath + "'";
            throw new PackerRuntimeException("Please define the '" + TagPacker.class.getSimpleName() +
                "' value to the MsgField with path '" + path + "'" +
                parentString + ", " +
                "because it is mandatory for MsgFieldType '" + msgField.getType() + "'. Please call the " +
                "fieldBuilder.defineChildrenTagPacker(..) method for its parent " +
                "or fieldBuilder.defineHeaderTagPacker(..) method for the MsgField itself.");
        }

        if (!tagExists) {
            throw new PackerRuntimeException("Please define the tag value to the field '" + path +
                    "', it is mandatory for MsgFieldType '" + msgField.getType() + "'.");
        }
    }

    protected void validateBitSetAndBitMapPackerExists(MsgField msgField, String path) {
        if (msgField.getBitMapPacker() == null) {
            throw new PackerRuntimeException("The bitMapPacker value is mandatory for '" + msgField.getType() +
                    "' field type. Please call the defineHeaderBitmapPacker(...) method. Field path: " + path + ".");
        }
    }

    protected void validateHeaderOrParentLengthPackerExists(MsgField msgField, String path) {
        boolean parentHasLengthPacker = msgField.getParent() != null && msgField.getParent().getChildrenLengthPacker() != null;
        boolean fieldHasLengthPacker = msgField.getLengthPacker() != null;
        if (!parentHasLengthPacker && !fieldHasLengthPacker) {
            throw new PackerRuntimeException("The '" + LengthPacker.class.getSimpleName() +
                    "' value is mandatory for the field '" + path +
                    "' or its parent because it has the '" + msgField.getType() +
                    "' type. Please call the defineHeaderLengthPacker() method on the field " +
                    "or defineChildrenLengthPacker() method on the field parent.");
        }
    }

    protected void validateLenExists(MsgField msgField, String path) {
        if (msgField.getLen() == null) {
            throw new PackerRuntimeException("The field with path '" + path +
                    "' has '" + msgField.getType() +
                    "' type, so please define its length by calling the defineLen() method.");
        }
    }

    protected void validateChildrenExists(MsgField msgField, String path) {
        if (msgField.getChildren() == null || msgField.getChildren().isEmpty()) {
            throw new PackerRuntimeException("The field '" + path +
                    "' has no children but has the '" + msgField.getType() +
                    "' type. Please define at least one child with the .createChild(...) method.");
        }
    }

    /**
     * @param navigator see the {@link #navigator} field description.
     */
    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    /**
     * @param visualizer see the {@link #visualizer} field description.
     */
    @Override
    public void setVisualizer(Visualizer visualizer) {
        this.visualizer = visualizer;
    }
}
