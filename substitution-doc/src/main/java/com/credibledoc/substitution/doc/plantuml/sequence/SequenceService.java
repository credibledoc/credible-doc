package com.credibledoc.substitution.doc.plantuml.sequence;

import org.springframework.stereotype.Service;

@Service
public class SequenceService {

    public static final String DEACTIVATE_LIFELINE = "deactivate";
    public static final String ACTIVATE_LIFELINE = "activate";
    public static final String ACTOR = "actor";
    public static final String PARTICIPANT = "participant";
    public static final String DATABASE = "database";

    private SequenceService() {
        // empty
    }
}
