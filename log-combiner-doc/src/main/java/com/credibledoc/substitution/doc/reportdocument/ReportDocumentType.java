package com.credibledoc.substitution.doc.reportdocument;

/**
 * Each generated {@link ReportDocument} has some type, for example
 * {@link #INDEX}, {@link #TRANSACTION_ACTIVITY},
 * {@link #TRANSACTION_COLLECTED} or {@link #UNIDENTIFIED}.
 */
public enum ReportDocumentType {

    /**
     * A part of another document, for example au UML diagram
     * in the launching.md document.
     */
    DOCUMENT_PART_UML,

    /**
     * The main document which contains the basic information of transactions
     * and links to transaction details.
     */
    INDEX,

    /**
     * UML diagram of transaction activity.
     */
    TRANSACTION_ACTIVITY,

    /**
     * Contains log lines filtered for particular transaction.
     */
    TRANSACTION_COLLECTED,

    /**
     * Contains log lines where a transaction can not be identified.
     */
    UNIDENTIFIED
}
