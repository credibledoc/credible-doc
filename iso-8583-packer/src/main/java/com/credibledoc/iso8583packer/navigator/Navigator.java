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
    @SuppressWarnings("unchecked")
    <T extends Msg> T findByName(List<? extends Msg> msgList, String name);

    MsgField getChildOrThrowException(String childName, MsgField currentMsgField);

    /**
     * Generate field name, for example 11 or 48(FFEE2E) or just 5F2A.
     * @param current focused node in the object graph
     * @return 'null' if name nor num has been set.
     */
    String generatePath(Msg current);

    String getPathRecursively(Msg msg);

    /**
     * Find the parent recursively.
     * @param msg source object graph
     * @param <T> the {@link Msg} type, {@link MsgField} or {@link MsgValue}
     * @return The root node of the graph
     */
    @SuppressWarnings("unchecked")
    <T extends Msg> T findRoot(T msg);

    TagPacker getTagPackerFromParent(MsgField msgField);

    MsgField getSiblingOrThrowException(String siblingName, MsgField currentMsgField);

    /**
     * Jump to msgField root and search for the <b>first</b> child with the same name and tag as the field from argument.
     * 
     * @param msgField where to search
     * @param msgValue what to search
     * @return The found msgField or thrown an exception
     */
    MsgField findByNameAndTagOrThrowException(MsgField msgField, MsgValue msgValue);

    MsgValue newFromNameAndTag(MsgField msgField);

    // TODO Kyrylo Semenko - javaDoc for all methods
    void validateSameNamesAndTag(MsgPair msgPair);

    /**
     * Find <b>first</b> {@link Msg} with the <b>tag</b>.
     *
     * @param msgList where to search
     * @param tag  what to search, see the {@link Msg#getTag()} description
     * @param <T>     the {@link Msg} type, {@link MsgField} or {@link MsgValue}
     * @return 'null' if not found
     */
    @SuppressWarnings("unchecked")
    <T extends Msg> T findByTag(List<? extends Msg> msgList, Object tag);

    /**
     * Find <b>first</b> {@link Msg} with the <b>fieldNum</b>.
     *
     * @param msgList where to search
     * @param fieldNum  what to search
     * @param <T>     the {@link Msg} type, {@link MsgField} or {@link MsgValue}
     * @return 'null' if not found
     */
    @SuppressWarnings("unchecked")
    <T extends Msg> T findByFieldNum(List<? extends Msg> msgList, Integer fieldNum);

    /**
     * Set service.
     *
     * @param visualizer the {@link Visualizer} to set.
     */
    void setVisualizer(Visualizer visualizer);
}
