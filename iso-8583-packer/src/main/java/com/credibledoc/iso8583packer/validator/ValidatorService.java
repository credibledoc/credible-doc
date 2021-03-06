package com.credibledoc.iso8583packer.validator;

import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgFieldType;
import com.credibledoc.iso8583packer.navigator.Navigator;
import com.credibledoc.iso8583packer.tag.TagPacker;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
            validateHasNoBitSetAndBitMapPacker(msgField, path);
        }
        
        if (msgField.getType() == MsgFieldType.BIT_SET) {
            validateBitSetAndBitMapPackerExists(msgField, path);
            validateChildrenExists(msgField, path);
        }

        validateMsgType(msgField, path);

        validateFixedLenSubfields(msgField, path);
        
        validateFixedLengthType(msgField, path);

        validateChildren(msgField, path);
    }

    private void validateMsgType(MsgField msgField, String path) {
        if (msgField.getType() == MsgFieldType.MSG) {
            validateHasNoBitSetAndBitMapPacker(msgField, path);
            validateHasNoLenDefined(msgField, path);
        }
    }

    private void validateChildren(MsgField msgField, String path) {
        List<MsgField> msgFields = msgField.getChildren();

        if (msgFields != null) {
            validateHasNoBodyPacker(msgField, path);
        }

        if (msgFields != null) {
            for (int i = 0; i < msgFields.size(); i++) {
                MsgField nextMsgField = msgFields.get(i);
                validateStructureRecursively(nextMsgField);
                if (i < msgFields.size() - 1) {
                    // all except the last child
                    validateLenExists(nextMsgField, navigator.getPathRecursively(nextMsgField));
                }
            }
            for (MsgField nextMsgField : msgFields) {
                validateStructureRecursively(nextMsgField);
            }
        }
    }

    void validateFixedLengthType(MsgField msgField, String path) {
        MsgFieldType type = msgField.getType();
        if (msgField.getLen() != null && MsgFieldType.isLengthType(msgField)) {
            throw new PackerRuntimeException("The current MsgField with type " + type + " and path '" + path +
                "' cannot have the 'fieldLen'" +
                " value because the type belongs to the following types: " +
                Arrays.toString(MsgFieldType.getLengthTypes().toArray()));
        }
    }

    private void validateHasNoBodyPacker(MsgField msgField, String path) {
        if (msgField.getBodyPacker() != null) {
            throw new PackerRuntimeException("The MsgField with path '" + path +
                "' has defined bodyPacker '" + msgField.getBodyPacker().getClass().getSimpleName() +
                "', but it is redundant because the MsgField contains the children.");
        }
    }

    protected void validateFixedLenSubfields(MsgField msgField, String path) {
        boolean isLenType =
            msgField.getLen() != null &&
            msgField.getChildren() != null &&
            allChildrenAreLenType(msgField.getChildren());
        
        if (isLenType) {
            int childrenLen = calculateChildrenLen(msgField.getChildren());
            if (msgField.getLen() != childrenLen) {
                throw new PackerRuntimeException("The msgField 'len' value '" + msgField.getLen() +
                    "' differs from its children 'len' values sum '" + childrenLen + "'. " +
                    "MsgField path: '" + path + "'");
            }
        }
    }

    protected int calculateChildrenLen(List<MsgField> children) {
        int result = 0;
        for (MsgField msgField : children) {
            result += msgField.getLen();
        }
        return result;
    }

    protected boolean allChildrenAreLenType(List<MsgField> children) {
        for (MsgField msgField : children) {
            if (msgField.getLen() == null) {
                return false;
            }
        }
        return true;
    }

    protected void validateHasNoLenDefined(MsgField msgField, String path) {
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
        Object tag = msgField.getTag();
        boolean tagExists = tag != null;

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

        TagPacker tagPacker = msgField.getTagPacker();
        if (tagPacker == null) {
            tagPacker = msgField.getParent().getChildrenTagPacker();
        }
        byte[] packedTag = tagPacker.pack(tag);
        Object unpackedTag = tagPacker.unpack(packedTag, 0);
        if (!Objects.equals(tag, unpackedTag)) {
            throw new PackerRuntimeException("MsgField with path '" + path + "' contains tag '" + tag + "', " +
                "the tag is packed as bytes '" + HexService.bytesToHex(packedTag) + "'. " +
                "But unpacked value '" + unpackedTag + "' of the tag " +
                "not equals with the original value '" + tag + "'." +
                " The tag is packed and unpacked with the '" + tagPacker.getClass().getSimpleName() + "' TagPacker." +
                " Please define other TagPacker.");
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
        if (msgField.getLen() == null &&
            (msgField.getType() == MsgFieldType.VAL || msgField.getType() == MsgFieldType.TAG_VAL)) {
            
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
