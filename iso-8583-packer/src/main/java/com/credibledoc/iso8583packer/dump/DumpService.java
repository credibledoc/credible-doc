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
 * This static helper creates the String representation of the {@link MsgField}s for logging and debugging purposes.
 * 
 * @author Kyrylo Semenko
 */
public class DumpService {
    private static final Logger logger = LoggerFactory.getLogger(DumpService.class);
    private static final int MAX_LEN_20 = 20;

    /**
     * Please don't instantiate this static helper.
     */
    private DumpService() {
        // empty
    }

    public static String dumpMsgField(MsgField msgField) {
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

    /**
     * Create the {@link MsgValue} documentation.
     *
     * @param msgField        is used for masking. Can be 'null' if the {@link MsgField} has no {@link Masker}s.
     * @param msgValue        contains data for serialization.
     * @param maskPrivateData if 'true', the values will be masked by appropriate {@link Masker}s defined in the
     *                        {@link MsgField} argument.
     * @return The serialized data.
     */
    public static String dumpMsgValue(MsgField msgField, MsgValue msgValue, boolean maskPrivateData) {
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

    /**
     * Print out field to printStream, for example
     * <pre>{@code
     *   <f type="VAL" bodyPacker="BcdBodyPacker" len="2"/>
     * }</pre>
     * @param msgField to be printed out
     * @param printStream to be filled out with field properties
     * @param indent in case of 'null',
     *              it will be set to empty. This indentation will be applied for all lines to formatting.
     * @param indentForChildren in case of 'null' or empty, it will be set to 4 spaces. This indentation will be applied to
     *              children of this field. 
     */
    public static void dumpMsgField(MsgField msgField, PrintStream printStream, String indent, String indentForChildren) {
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

    private static String getChildTagPackerString(MsgField msgField) {
        String childTagPackerString;
        if (msgField.getChildrenTagPacker() != null) {
            childTagPackerString = " childTagPacker=\"" + msgField.getChildrenTagPacker().getClass().getSimpleName() + "\"";
        } else {
            childTagPackerString = "";
        }
        return childTagPackerString;
    }

    private static String getChildTagLenString(MsgField msgField) {
        String childTagLenString;
        if (msgField.getChildTagLength() != null) {
            childTagLenString = " childTagLen=\"" + msgField.getChildTagLength().toString() + "\"";
        } else {
            childTagLenString = "";
        }
        return childTagLenString;
    }

    private static String getLenString(MsgField msgField) {
        String lenString;
        if (msgField.getLen() != null) {
            lenString = " len=\"" + msgField.getLen().toString() + "\"";
        } else {
            lenString = "";
        }
        return lenString;
    }

    private static String getMaxLenString(MsgField msgField) {
        String maxLenString;
        if (msgField.getMaxLen() != null) {
            maxLenString = " maxLen=\"" + msgField.getMaxLen().toString() + "\"";
        } else {
            maxLenString = "";
        }
        return maxLenString;
    }

    private static String getBodyPackerString(MsgField msgField) {
        String result = null;
        if (msgField.getBodyPacker() != null) {
            result = msgField.getBodyPacker().getClass().getSimpleName();
        }
        return result == null ? "" : " bodyPacker=\"" + result + "\"";
    }

    /**
     * Create the {@link MsgValue} documentation.
     * 
     * @param msgField may be 'null' if the maskPrivateData is 'false'.
     * @param msgValue the data for documentation.
     * @param printStream where to write the serialized documentation.
     * @param indent indentation of the current item. May be 'null'.
     * @param indentForChildren the indentation increment for children of the current item.
     * @param maskPrivateData if 'true', the values will be masked by appropriate {@link Masker}s defined in
     *                       the {@link MsgField} argument.
     */
    public static void dumpMsgValue(MsgField msgField, MsgValue msgValue, PrintStream printStream, String indent,
                                    String indentForChildren, boolean maskPrivateData) {
        
        NavigatorService.validateSameNamesAndTagNum(new MsgPair(msgField, msgValue));
        if (indent == null) {
            indent = "";
        }
        if (StringUtils.isEmpty(indentForChildren)) {
            indentForChildren = "    ";
        }

        if (maskPrivateData && msgField == null) {
            throw new PackerRuntimeException("Argument msgField cannot be 'null' when argument maskPrivateData is 'true'." +
                    " It used for masking of sensitive private data from the msgValue");
        }

        Masker masker = null;
        if (msgField != null) {
            masker = msgField.getMasker();
        }
        
        String valueHexString = maskBodyBytes(msgValue, maskPrivateData, masker);

        String valueString;
        if (msgField != null) {
            valueString = getValueString(msgValue, maskPrivateData, masker, msgField.getStringer());
        } else {
            valueString = getValueString(msgValue, false, null, StringStringer.INSTANCE);
        }

        String tagNumString = msgValue.getTagNum() == null ? "" : " tagNum=\"" + msgValue.getTagNum() + "\"";

        String nameString = msgValue.getName() == null ? "" : " name=\"" + msgValue.getName() + "\"";
        
        String bitmapString;
        if (msgField != null && msgField.getHeaderField() != null &&
                msgValue.getHeaderValue() != null && msgValue.getHeaderValue().getBitSet() != null) {
            BitSet bitSet = msgValue.getHeaderValue().getBitSet();
            byte[] bytes = msgField.getHeaderField().getBitMapPacker().pack(bitSet, msgField.getLen());
            bitmapString = " bitmapHex=\"" + HexService.bytesToHex(bytes) + "\"";
        } else {
            bitmapString = "";
        }

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

    private static String getLenHexString(HeaderValue headerValue) {
        String length = null;
        if (headerValue != null && headerValue.getLengthBytes() != null) {
            length = HexService.bytesToHex(headerValue.getLengthBytes());
        }
        return length == null ? "" : (" lenHex=\"" + length + "\"");
    }

    private static String getTagHexString(HeaderValue headerValue) {
        String tag = null;
        if (headerValue != null && headerValue.getTagBytes() != null) {
            tag = HexService.bytesToHex(headerValue.getTagBytes());
        }
        return tag == null ? "" : (" tagHex=\"" + tag + "\"");
    }

    private static String getValueString(MsgValue msgValue, boolean maskPrivateData, Masker masker, Stringer stringer) {
        if (msgValue.getChildren() != null) {
            return "";
        }
        return " val=\"" + maskValue(msgValue, maskPrivateData, masker, stringer) + "\"";
    }

    private static String maskValue(MsgValue msgValue, boolean maskPrivateData, Masker masker, Stringer stringer) {
        Object bodyValue = msgValue.getBodyValue();
        if (maskPrivateData && masker != null) {
            return masker.maskValue(bodyValue);
        }
        return stringer.convert(bodyValue);
    }

    private static String maskBodyBytes(MsgValue msgValue, boolean maskPrivateData, Masker masker) {
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

    private static void printContent(MsgField msgField, MsgValue msgValue, PrintStream printStream, String indent,
                                     String indentForChildren, boolean maskPrivateData, String content) {
        printStream.print(indent + "<f" + content);
        if (msgValue.getChildren() != null) {
            printStream.println(">");
            List<MsgField> list = msgField.getChildren();
            for (MsgValue childMsgValue : msgValue.getChildren()) {
                MsgField childMsgField = NavigatorService.findByName(list, childMsgValue.getName());
                dumpMsgValue(childMsgField, childMsgValue, printStream, indent + indentForChildren, indentForChildren, maskPrivateData);
            }
            printStream.println(indent + "</f>");
        } else {
            printStream.println("/>");
        }
    }
}
