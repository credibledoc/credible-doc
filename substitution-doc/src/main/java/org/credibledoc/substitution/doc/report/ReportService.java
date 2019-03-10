package org.credibledoc.substitution.doc.report;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.template.TemplateService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.credibledoc.substitution.doc.reportdocument.ReportDocument;
import org.credibledoc.substitution.doc.reportdocument.ReportDocumentService;
import org.credibledoc.substitution.doc.reportdocument.ReportDocumentType;
import org.credibledoc.substitution.doc.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Provides access to the {@link ReportRepository} and serves {@link Report}s.
 *
 * @author Kyrylo Semenko
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private static final String PUBLIC = "public";

    @NonNull
    private final ReportRepository reportRepository;

    @NonNull
    private final ApplicationContext applicationContext;

    @NonNull
    private ReportDocumentService reportDocumentService;

    /**
     * Create empty reports
     */
    public void prepareReports() {
        List<Report> reports = reportRepository.getReports();
        for (Report report : reports) {
            File publicFolder = new File(report.getDirectory(), PUBLIC);
            createCssFile(publicFolder);
            createJsFiles(publicFolder);

            Map<String, ReportDocumentCreator> reportDocumentCreatorMap =
                applicationContext.getBeansOfType(ReportDocumentCreator.class);

            for (ReportDocumentCreator reportDocumentCreator : reportDocumentCreatorMap.values()) {
                ReportDocumentType reportDocumentType = reportDocumentCreator.getReportDocumentType();
                if (reportDocumentType == ReportDocumentType.INDEX ||
                    reportDocumentType == ReportDocumentType.UNIDENTIFIED) {

                    ReportDocument reportDocument = reportDocumentCreator.prepareReportDocument();
                    reportDocumentService.getReportDocuments().add(reportDocument);
                }
            }
        }
    }

    /**
     * Call the {@link ReportRepository#getReports()} method.
     * @return the global application state.
     */
    public List<Report> getReports() {
        return reportRepository.getReports();
    }

    /**
     * Create javaScript folder and files.
     * @param publicFolder where js folder will be created
     */
    private void createJsFiles(File publicFolder) {
        File jsFolder = new File(publicFolder, "js");
        boolean cssFolderCreated = jsFolder.mkdirs();
        if (!cssFolderCreated) {
            throw new SubstitutionRuntimeException("Cannot create folder: " + jsFolder.getAbsolutePath());
        }
        
        String jqueryFileAbsolutePath = jsFolder.getAbsolutePath() + File.separator + "jquery-1.11.0.min.js";
        TemplateService templateService = TemplateService.getInstance();
        File jsJqueryFile = templateService
                .exportResource(Template.JQUERY.getTemplateRelativePath(), jqueryFileAbsolutePath);
        logger.info("JavaScript library created: {}", jsJqueryFile);
        
        String scriptFileAbsolutePath = jsFolder.getAbsolutePath() + File.separator + "script.js";
        File jsScriptFile = templateService
                .exportResource(Template.JAVASCRIPT.getTemplateRelativePath(), scriptFileAbsolutePath);
        logger.info("JavaScript created: {}", jsScriptFile);
    }

    private void createCssFile(File publicFolder) {
        File cssFolder = new File(publicFolder, "css");
        boolean isCreated = cssFolder.mkdirs();
        if (!isCreated) {
            throw new SubstitutionRuntimeException("Cannot create folder: " + cssFolder.getAbsolutePath());
        }
        String cssFileAbsolutePath = cssFolder.getAbsolutePath() + File.separator + "css.css";
        TemplateService templateService = TemplateService.getInstance();
        File cssFile = templateService.exportResource(Template.CSS.getTemplateRelativePath(), cssFileAbsolutePath);
        logger.info("File created: {}", cssFile);
    }

    /**
     * Add all reports to the {@link #reportRepository}.
     * @param reports for appending
     */
    public void addReports(List<Report> reports) {
        reportRepository.addReports(reports);
    }
}
