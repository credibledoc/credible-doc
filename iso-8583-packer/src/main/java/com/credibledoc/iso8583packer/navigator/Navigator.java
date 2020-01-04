package com.credibledoc.iso8583packer.navigator;

import com.credibledoc.iso8583packer.dump.Visualizer;
import com.credibledoc.iso8583packer.message.Msg;
import com.credibledoc.iso8583packer.message.MsgField;
import com.credibledoc.iso8583packer.message.MsgPair;
import com.credibledoc.iso8583packer.message.MsgValue;
import com.credibledoc.iso8583packer.tag.TagPacker;

import java.util.List;

public interface Navigator {
    /**
     * Find <b>first</b> {@link Msg} with the name.
     *
     * @param msgList where for search
     * @param name    what to search
     * @param <T>  the {@link Msg} type, {@link MsgField} or {@link MsgValue}
     * @return 'null' if not found
     */
    <T extends Msg> T findByName(List<? extends Msg> msgList, String name);

    /**
     * Find {@link MsgField} with {@link Msg#getName()}.
     * @param childName {@link Msg#getName()}.
     * @param currentMsgField where to start search.
     * @return The found {@link MsgField} or an exception will be thrown.
     */
    MsgField getChildOrThrowException(String childName, MsgField currentMsgField);

    /**
     * Generate field name, for example 11 or 48(FFEE2E) or just 5F2A.
     * @param current focused node in the object graph
     * @return 'null' if name nor num has been set.
     */
    String generatePath(Msg current);

    /**
     * @param msg the field for path generation
     * @return For example msg.bitmap.PAN_02(2)
     */
    String getPathRecursively(Msg msg);

    /**
     * Find the parent recursively.
     * @param msg source object graph
     * @param <T> the {@link Msg} type, {@link MsgField} or {@link MsgValue}
     * @return The root node of the graph
     */
    <T extends Msg> T findRoot(T msg);

    /**
     * @param msgField where to find.
     * @return The {@link MsgField#getChildrenTagPacker()} value.
     */
    TagPacker getTagPackerFromParent(MsgField msgField);

    /**
     * Find sibling by name.
     * @param siblingName the {@link Msg#getName()} value.
     * @param currentMsgField where to search for a sibling.
     * @return The found sibling or throw a new exception.
     */
    MsgField getSiblingOrThrowException(String siblingName, MsgField currentMsgField);

    /**
     * Jump to msgField root and search for the <b>first</b> child with the same name and tag as the field from argument.
     * 
     * @param msgField where to search
     * @param msgValue what to search
     * @return The found msgField or thrown an exception
     */
    MsgField findByNameAndTagOrThrowException(MsgField msgField, MsgValue msgValue);

    /**
     * Create new {@link MsgValue} and copy {@link MsgField#getName()}, {@link MsgField#getTag()}
     * and {@link MsgField#getFieldNum()} from template.
     * @param msgField the template.
     * @return The created {@link MsgValue}.
     */
    MsgValue newFromNameAndTag(MsgField msgField);

    /**
     * Get {@link MsgPair#getMsgField()} and {@link MsgPair#getMsgValue()}
     * and check theirs {@link Msg#getName()} and {@link Msg#getTag()} equality.
     * @param msgPair the checked container.
     */
    void validateSameNamesAndTags(MsgPair msgPair);

    /**
     * Find <b>first</b> {@link Msg} with the <b>fieldNum</b>.
     *
     * @param msgList where to search
     * @param fieldNum  what to search
     * @param <T>     the {@link Msg} type, {@link MsgField} or {@link MsgValue}
     * @return 'null' if not found
     */
    <T extends Msg> T findByFieldNum(List<? extends Msg> msgList, Integer fieldNum);

    /**
     * Set service.
     *
     * @param visualizer the {@link Visualizer} to set.
     */
    void setVisualizer(Visualizer visualizer);
}
