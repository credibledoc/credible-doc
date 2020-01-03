package com.credibledoc.iso8583packer.dump;

import com.credibledoc.iso8583packer.bitmap.BitmapPacker;
import com.credibledoc.iso8583packer.exception.PackerRuntimeException;
import com.credibledoc.iso8583packer.header.HeaderField;
import com.credibledoc.iso8583packer.header.HeaderValue;
import com.credibledoc.iso8583packer.hex.HexService;
import com.credibledoc.iso8583packer.length.LengthPacker;
import com.credibledoc.iso8583packer.masking.Masker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgPair;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.navigator.Navigator;
import com.credibledoc.iso8583packer.navigator.NavigatorService;
import com.credibledoc.iso8583packer.string.StringUtils;
import com.credibledoc.iso8583packer.stringer.StringStringer;
import com.credibledoc.iso8583packer.stringer.Stringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.List;

/**
 * The service creates the String representation of the {@link MsgField}s and {@link MsgValue}s for logging and debugging purposes.
 * 
 * @author Kyrylo Semenko
 */
public class DumpService implements Visualizer {
    private static final Logger logger = LoggerFactory.getLogger(DumpService.class);
    private static final int MAX_LEN_20 = 20;
    public static final String FOR_SPACES = "    ";

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

        String tagNumString = msgField.getTagNum() == null ? "" : " tagNum=\"" + msgField.getTagNum() + "\"";
        
        String nameString = msgField.getName() == null ? "" : " name=\"" + msgField.getName() + "\"";
        
        LengthPacker lengthPacker = null;
        BitmapPacker bitmapPacker = null;
        final HeaderField headerField = msgField.getHeaderField();
        if (headerField != null) {
            lengthPacker = headerField.getLengthPacker();
            bitmapPacker = headerField.getBitMapPacker();
        }

        String lengthPackerString = lengthPacker == null ? "" : (" lengthPacker=\"" + lengthPacker.getClass().getSimpleName() + "\"");
        String isoBitMapPackerString = bitmapPacker == null ? "" : (" bitMapPacker=\"" + bitmapPacker.getClass().getSimpleName() + "\"");

        String interpreterString = getBodyPackerString(msgField);

        String maxLenString = getMaxLenString(msgField);

        String lenString = getLenString(msgField);

        String childTagLenString = getChildTagLenString(msgField);

        String childTagPackerString = getChildTagPackerString(msgField);

        String typeString = " type=\"" + msgField.getType() + "\"";
        
        printStream.print(indent + "<f" + typeString + tagNumString + nameString + lengthPackerString +
                isoBitMapPackerString + interpreterString +
                maxLenString + lenString + childTagLenString + childTagPackerString);
        
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
        if (msgField.getChildrenTagPacker() != null) {
            childTagPackerString = " childTagPacker=\"" + msgField.getChildrenTagPacker().getClass().getSimpleName() + "\"";
        } else {
            childTagPackerString = "";
        }
        return childTagPackerString;
    }

    protected String getChildTagLenString(MsgField msgField) {
        String childTagLenString;
        if (msgField.getChildTagLength() != null) {
            childTagLenString = " childTagLen=\"" + msgField.getChildTagLength().toString() + "\"";
        } else {
            childTagLenString = "";
        }
        return childTagLenString;
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

        navigator.validateSameNamesAndTagNum(new MsgPair(msgField, msgValue));
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

        String tagNumString = msgValue.getTagNum() == null ? "" : " tagNum=\"" + msgValue.getTagNum() + "\"";

        String nameString = msgValue.getName() == null ? "" : " name=\"" + msgValue.getName() + "\"";

        String bitmapString = createBitmapString(msgField, msgValue);

        final HeaderValue headerValue = msgValue.getHeaderValue();
        
        String tagHexString = getTagHexString(headerValue);

        String lenHexString = getLenHexString(headerValue);

        String content;

        String numNameValue = nameString + tagNumString + valueString + bitmapString;
        
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
        if (msgField != null && msgField.getHeaderField() != null &&
                msgValue.getHeaderValue() != null && msgValue.getHeaderValue().getBitSet() != null) {
            BitSet bitSet = msgValue.getHeaderValue().getBitSet();
            byte[] bytes = msgField.getHeaderField().getBitMapPacker().pack(bitSet, msgField.getLen());
            bitmapString = " bitmapHex=\"" + HexService.bytesToHex(bytes) + "\"";
        } else {
            bitmapString = "";
        }
        return bitmapString;
    }

    protected String getLenHexString(HeaderValue headerValue) {
        String length = null;
        if (headerValue != null && headerValue.getLengthBytes() != null) {
            length = HexService.bytesToHex(headerValue.getLengthBytes());
        }
        return length == null ? "" : (" lenHex=\"" + length + "\"");
    }

    protected String getTagHexString(HeaderValue headerValue) {
        String tag = null;
        if (headerValue != null && headerValue.getTagBytes() != null) {
            tag = HexService.bytesToHex(headerValue.getTagBytes());
        }
        return tag == null ? "" : (" tagHex=\"" + tag + "\"");
    }

    protected String getValueString(MsgValue msgValue, boolean maskPrivateData, Masker masker, Stringer stringer) {
        if (msgValue.getChildren() != null) {
            return "";
        }
        return " val=\"" + maskValue(msgValue, maskPrivateData, masker, stringer) + "\"";
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
            if (hex.length() > MAX_LEN_20) {
                valueHex = hex.substring(0, 8) + "..." + hex.substring(hex.length() - 8);
            } else {
                valueHex = hex;
            }
        }
        return valueHex == null ? "" : (" valHex=\"" + valueHex + "\"");
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
