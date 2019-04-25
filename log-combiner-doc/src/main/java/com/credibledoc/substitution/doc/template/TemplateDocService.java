package com.credibledoc.substitution.doc.template;

import com.credibledoc.substitution.core.template.TemplateService;
import org.springframework.stereotype.Service;

/**
 * Provides templates of documents.
 *
 * @author Kyrylo Semenko
 */
@Service
public class TemplateDocService {

    /**
     * Return a template as a String
     * @param template see {@link Template}
     */
    public String getTemplate(Template template) {
        String templateRelativePath = template.getTemplateRelativePath();
        return TemplateService.getInstance().getTemplateContent(templateRelativePath);
    }

}
