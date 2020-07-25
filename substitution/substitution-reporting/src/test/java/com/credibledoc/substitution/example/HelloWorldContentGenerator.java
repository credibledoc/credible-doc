package com.credibledoc.substitution.example;

import com.credibledoc.substitution.core.content.Content;
import com.credibledoc.substitution.core.content.ContentGenerator;
import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.placeholder.Placeholder;

public class HelloWorldContentGenerator implements ContentGenerator {
    @Override
    public Content generate(Placeholder placeholder, SubstitutionContext substitutionContext) {
        Content content = new Content();
        content.setMarkdownContent(", world!");
        return content;
    }
}
