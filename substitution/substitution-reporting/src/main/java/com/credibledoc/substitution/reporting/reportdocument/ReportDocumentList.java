package com.credibledoc.substitution.reporting.reportdocument;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.reporting.report.Report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ordered list with {@link #map} for a better performance.
 * @param <E> The {@link ReportDocument}
 */
public class ReportDocumentList<E> extends ArrayList<E> {
    private final Map<Report, List<ReportDocument>> map = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public boolean add(Object object) {
        if (!(object instanceof ReportDocument)) {
            throw new SubstitutionRuntimeException("Expected " + ReportDocument.class.getCanonicalName() +
                " but found " + object.getClass().getCanonicalName());
        }
        ReportDocument reportDocument = (ReportDocument) object;
        if (reportDocument.getReport() == null) {
            throw new SubstitutionRuntimeException("Report is mandatory for ReportDocument: " + reportDocument);
        }
        List<ReportDocument> list = map.get(reportDocument.getReport());
        if (list == null) {
            list = new ArrayList<>();
            list.add(reportDocument);
            map.put(reportDocument.getReport(), list);
            return super.add((E) reportDocument);
        }
        if (list.contains(reportDocument)) {
            return false;
        } else {
            list.add(reportDocument);
        }
        return super.add((E) reportDocument);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection collection) {
        for (Object object : collection) {
            add(object);
        }
        return super.addAll(collection);
    }

    public List<ReportDocument> get(Report report) {
        return map.get(report);
    }
}
