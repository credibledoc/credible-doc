package com.credibledoc.iso8583packer.validator;

import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.navigator.Navigator;

/**
 * The interface is used for validation the {@link com.credibledoc.iso8583packer.message.MsgField} structure.
 * <p>
 * The interface created for ability to override its implementation in the {@link ValidatorService} class.
 *
 * @author Kyrylo Semenko
 */
public interface Validator {

    /**
     * Check restrictions of the Field structure and all its children recursively.
     *
     * @param current the {@link MsgField} to be checked.
     */
    void validateStructure(MsgField current);

    /**
     * Set service.
     * 
     * @param navigator the {@link Navigator} to set.
     */
    void setNavigator(Navigator navigator);

    /**
     * Set service.
     *
     * @param visualizer the {@link Visualizer} to set.
     */
    void setVisualizer(Visualizer visualizer);
}
