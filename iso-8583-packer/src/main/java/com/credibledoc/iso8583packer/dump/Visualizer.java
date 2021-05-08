package com.credibledoc.iso8583packer.dump;

import com.credibledoc.iso8583packer.masking.Masker;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.navigator.Navigator;

import java.io.PrintStream;

/**
 * The implemented services help to visualize {@link MsgField} and {@link MsgValue} object graphs.
 * 
 * @author Kyrylo Semenko
 */
public interface Visualizer {
    
    /**
     * Print msgField object graph.
     * @param msgField the value to be visualized.
     * @return String representation of the {@link MsgField}.
     */
    String dumpMsgField(MsgField msgField);

    /**
     * Create the {@link MsgValue} documentation.
     *
     * @param msgField        is used for masking. Can be 'null' if the {@link MsgField} has no {@link Masker}s.
     * @param msgValue        contains data for serialization.
     * @param maskPrivateData if 'true', the values will be masked by appropriate {@link Masker}s defined in the
     *                        {@link MsgField} argument.
     * @return The serialized data.
     */
    String dumpMsgValue(MsgField msgField, MsgValue msgValue, boolean maskPrivateData);

    /**
     * Print the field to the printStream, for example
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
    void dumpMsgField(MsgField msgField, PrintStream printStream, String indent, String indentForChildren);

    /**
     * Create the {@link MsgValue} documentation, for example
     * <pre>{@code
     *   <f val="123" lenHex="0002" valHex="0123"/>
     * }</pre>
     * 
     * @param msgField may be 'null' if the maskPrivateData is 'false'.
     * @param msgValue the data for documentation.
     * @param printStream where to write the serialized documentation.
     * @param indent indentation of the current item. May be 'null', in this case no indentation is applied.
     * @param indentForChildren the indentation increment for children of the current item. May be 'null', in this case
     *                          four spaces is used.
     * @param maskPrivateData if 'true', the values will be masked with appropriate {@link Masker}s defined in
     *                       the {@link MsgField} argument.
     */
    void dumpMsgValue(MsgField msgField, MsgValue msgValue, PrintStream printStream, String indent,
                      String indentForChildren, boolean maskPrivateData);

    /**
     * The method may be used for replacing the default implementation {@link DumpService} of {@link Navigator}.
     * @param navigator the implementation for replacing.
     */
    void setNavigator(Navigator navigator);
}
