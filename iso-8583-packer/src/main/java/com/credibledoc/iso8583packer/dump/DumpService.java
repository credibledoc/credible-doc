package com.credibledoc.iso8583packer.dump;

import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.masking.Masker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgPair;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.navigator.Navigator;
import com.credibledoc.iso8583packer.navigator.NavigatorService;
import com.credibledoc.iso8583packer.string.StringUtils;
import com.credibledoc.iso8583packer.stringer.StringStringer;
import com.credibledoc.iso8583packer.stringer.Stringer;
import com.credibledoc.iso8583packer.tag.TagPacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

/**
 * The service creates the String representation of the {@link MsgField}s and {@link MsgValue}s for logging and debugging purposes.
 * 
 * @author Kyrylo Semenko
 */
public class DumpService implements Visualizer {
    private static final Logger logger = LoggerFactory.getLogger(DumpService.class);
    public static final String FOR_SPACES = "    ";
    private static final String VAL_ATTRIBUTE_PREFIX = " val=\"";
    private static final String VAL_HEX_ATTRIBUTE_PREFIX = " valHex=\"";

    protected static DumpService instance;
    
    protected Navigator navigator;

    /**
     * Please use the {@link #getInstance()} method instead of this constructor.
     */
    public DumpService() {
        // empty
    }

    /**
     * Static factory.
     * @return The single instance of the {@link DumpService}. 
     */
    public static DumpService getInstance() {
        if (instance == null) {
            instance = new DumpService();
            instance.createDefaultServices();
        }
        return instance;
    }

    /**
     * Create instances of services used in the builder. The method may be overridden if needed.
     */
    protected void createDefaultServices() {
        navigator = NavigatorService.getInstance();
    }

    @Override
    public String dumpMsgField(MsgField msgField) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream printStream = new PrintStream(baos, true, "UTF-8")) {
                dumpMsgField(msgField, printStream, null, "    ");
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            String message = "Error in dump method.";
            logger.error(message, e);
            // Do nothing, it is logging only.
            return message;
        }
    }

    @Override
    public String dumpMsgValue(MsgField msgField, MsgValue msgValue, boolean maskPrivateData) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream printStream = new PrintStream(baos, true, "UTF-8")) {
                dumpMsgValue(msgField, msgValue, printStream, null, "    ", maskPrivateData);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            String message = "Error in dump method.";
            logger.error(message, e);
            // Do nothing, it is logging only.
            return message;
        }
    }

    @Override
    public void dumpMsgField(MsgField msgField, PrintStream printStream, String indent, String indentForChildren) {
        if (indent == null) {
            indent = "";
        }
        if (StringUtils.isEmpty(indentForChildren)) {
            indentForChildren = "    ";
        }

        String fieldNumString = getAttributeString(msgField.getFieldNum(), " fieldNum=\"");

        String tagString = getAttributeString(msgField.getTag(), " tag=\"");

        String nameString = getAttributeString(msgField.getName(), " name=\"");
        
        String lengthPackerString = msgField.getLengthPacker() == null ?
            "" : (" lengthPacker=\"" + msgField.getLengthPacker().getClass().getSimpleName() + "\"");

        String isoBitMapPackerString = msgField.getBitMapPacker() == null ?
            "" : (" bitMapPacker=\"" + msgField.getBitMapPacker().getClass().getSimpleName() + "\"");

        String interpreterString = getBodyPackerString(msgField);

        String maxLenString = getMaxLenString(msgField);

        String lenString = getLenString(msgField);

        String childTagPackerString = getChildTagPackerString(msgField);
        
        String tagPackerString = msgField.getTagPacker() == null ? "" :
            " tagPacker=\"" + msgField.getTagPacker().getClass().getSimpleName() + "\"";

        String typeString = " type=\"" + msgField.getType() + "\"";
        
        printStream.print(indent + "<f" + typeString + fieldNumString + tagString + nameString + lengthPackerString +
                isoBitMapPackerString + interpreterString +
                maxLenString + lenString + childTagPackerString + tagPackerString);
        
        if (msgField.getChildren() != null) {
            printStream.println(">");
            for (MsgField child : msgField.getChildren()) {
                dumpMsgField(child, printStream, indent + indentForChildren, indentForChildren);
            }
            printStream.println(indent + "</f>");
        } else {
            printStream.println("/>");
        }
    }

    protected String getChildTagPackerString(MsgField msgField) {
        String childTagPackerString;
        TagPacker tagPacker = msgField.getChildrenTagPacker();
        if (tagPacker != null) {
            childTagPackerString = " childTagPacker=\"" + tagPacker.getClass().getSimpleName() +
                "(" + tagPacker.getPackedLength() + ")\"";
        } else {
            childTagPackerString = "";
        }
        return childTagPackerString;
    }

    protected String getLenString(MsgField msgField) {
        String lenString;
        if (msgField.getLen() != null) {
            lenString = " len=\"" + msgField.getLen().toString() + "\"";
        } else {
            lenString = "";
        }
        return lenString;
    }

    protected String getMaxLenString(MsgField msgField) {
        String maxLenString;
        if (msgField.getMaxLen() != null) {
            maxLenString = " maxLen=\"" + msgField.getMaxLen().toString() + "\"";
        } else {
            maxLenString = "";
        }
        return maxLenString;
    }

    protected String getBodyPackerString(MsgField msgField) {
        String result = null;
        if (msgField.getBodyPacker() != null) {
            result = msgField.getBodyPacker().getClass().getSimpleName();
        }
        return result == null ? "" : " bodyPacker=\"" + result + "\"";
    }

    @Override
    public void dumpMsgValue(MsgField msgField, MsgValue msgValue, PrintStream printStream, String indent,
                             String indentForChildren, boolean maskPrivateData) {
        if (msgField != null && !Objects.equals(msgField.getName(), msgValue.getName())) {
            msgValue = navigator.synchronizeMessageValue(msgField, msgValue);
        }
        navigator.validateSameNamesAndTags(new MsgPair(msgField, msgValue));
        if (indent == null) {
            indent = "";
        }
        if (StringUtils.isEmpty(indentForChildren)) {
            indentForChildren = FOR_SPACES;
        }

        if (maskPrivateData && msgField == null) {
            throw new PackerRuntimeException("Argument msgField cannot be 'null' when argument maskPrivateData is 'true'." +
                    " It used for masking of sensitive private data from the msgValue");
        }

        Masker masker = null;
        if (msgField != null) {
            masker = msgField.getMasker();
        }

        String valueHexString = getHexValueString(msgValue, maskPrivateData, masker);

        String valueString = getValueString(msgField, msgValue, maskPrivateData, masker);

        if (valueString.length() > 0 && valueHexString.length() > 0 &&
            valueString.substring(VAL_ATTRIBUTE_PREFIX.length()).equals(valueHexString.substring(VAL_HEX_ATTRIBUTE_PREFIX.length()))) {
            // do not print the same values of val and valHex.
            valueHexString = "";
        }

        String fieldNumString = getAttributeString(msgValue.getFieldNum(), " fieldNum=\"");

        String tagString = getAttributeString(msgValue.getTag(), " tag=\"");

        String nameString = getAttributeString(msgValue.getName(), " name=\"");

        String bitmapString = createBitmapString(msgField, msgValue);

        String bitSetString = createBitSetString(msgField, msgValue);

        String tagHexString = getTagHexString(msgValue);

        String lenHexString = getLenHexString(msgValue);

        String content;

        String numNameValue = nameString + fieldNumString + tagString + valueString + bitmapString + bitSetString;
        
        if (msgField != null) {
            switch (msgField.getType()) {
                
                case LEN_TAG_VAL:
                case LEN_VAL:
                case BIT_SET:
                    content = numNameValue + lenHexString + tagHexString + valueHexString;
                    break;
                    
                default: // TAG_LEN_VAL and others
                    content = numNameValue + tagHexString + lenHexString + valueHexString;
                    break;
            }
        } else {
            content = numNameValue + tagHexString + lenHexString + valueHexString;
        }

        printContent(msgField, msgValue, printStream, indent, indentForChildren, maskPrivateData, content);
    }

    protected String getAttributeString(Object object, String attribute) {
        return object == null ? "" : attribute + object + "\"";
    }

    protected String getValueString(MsgField msgField, MsgValue msgValue, boolean maskPrivateData, Masker masker) {
        String valueString;
        if (msgField != null) {
            valueString = getValueString(msgValue, maskPrivateData, masker, msgField.getStringer());
        } else {
            valueString = getValueString(msgValue, false, null, StringStringer.getInstance());
        }
        return valueString;
    }

    protected String getHexValueString(MsgValue msgValue, boolean maskPrivateData, Masker masker) {
        String valueHexString;
        if (msgValue.getChildren() == null || msgValue.getChildren().isEmpty()) {
            valueHexString = maskBodyBytes(msgValue, maskPrivateData, masker);
        } else {
            valueHexString = "";
        }
        return valueHexString;
    }

    protected String createBitmapString(MsgField msgField, MsgValue msgValue) {
        String bitmapString;
        if (msgField != null && msgValue.getBitSet() != null) {
            byte[] bytes = msgValue.getBodyBytes();
            bitmapString = " bitmapHex=\"" + HexService.bytesToHex(bytes) + "\"";
        } else {
            bitmapString = "";
        }
        return bitmapString;
    }

    protected String createBitSetString(MsgField msgField, MsgValue msgValue) {
        String bitSetString;
        if (msgField != null && msgValue.getBitSet() != null) {
            BitSet bitSet = msgValue.getBitSet();
            bitSetString = " bitSet=\"" + bitSet + "\"";
        } else {
            bitSetString = "";
        }
        return bitSetString;
    }

    protected String getLenHexString(MsgValue msgValue) {
        String length = null;
        if (msgValue != null && msgValue.getLengthBytes() != null) {
            length = HexService.bytesToHex(msgValue.getLengthBytes());
        }
        return length == null ? "" : (" lenHex=\"" + length + "\"");
    }

    protected String getTagHexString(MsgValue msgValue) {
        String tag = null;
        if (msgValue != null && msgValue.getTagBytes() != null) {
            tag = HexService.bytesToHex(msgValue.getTagBytes());
        }
        return tag == null ? "" : (" tagHex=\"" + tag + "\"");
    }

    protected String getValueString(MsgValue msgValue, boolean maskPrivateData, Masker masker, Stringer stringer) {
        if (msgValue.getChildren() != null || msgValue.getBodyValue() == null) {
            return "";
        }
        return VAL_ATTRIBUTE_PREFIX + maskValue(msgValue, maskPrivateData, masker, stringer) + "\"";
    }

    protected String maskValue(MsgValue msgValue, boolean maskPrivateData, Masker masker, Stringer stringer) {
        Object bodyValue = msgValue.getBodyValue();
        if (maskPrivateData && masker != null) {
            return masker.maskValue(bodyValue);
        }
        return stringer.convert(bodyValue);
    }

    protected String maskBodyBytes(MsgValue msgValue, boolean maskPrivateData, Masker masker) {
        String valueHex = null;
        if (msgValue.getBodyBytes() != null) {
            String hex = HexService.bytesToHex(msgValue.getBodyBytes());
            if (maskPrivateData && masker != null) {
                hex = masker.maskHex(hex);
            }
            valueHex = hex;
        }
        return valueHex == null ? "" : (VAL_HEX_ATTRIBUTE_PREFIX + valueHex + "\"");
    }

    protected void printContent(MsgField msgField, MsgValue msgValue, PrintStream printStream, String indent,
                                     String indentForChildren, boolean maskPrivateData, String content) {
        printStream.print(indent + "<f" + content);
        if (msgValue.getChildren() != null) {
            printStream.println(">");
            List<MsgField> list = msgField.getChildren();
            for (MsgValue childMsgValue : msgValue.getChildren()) {
                MsgField childMsgField = navigator.findByName(list, childMsgValue.getName());
                dumpMsgValue(childMsgField, childMsgValue, printStream, indent + indentForChildren, indentForChildren, maskPrivateData);
            }
            printStream.println(indent + "</f>");
        } else {
            printStream.println("/>");
        }
    }

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }
}
