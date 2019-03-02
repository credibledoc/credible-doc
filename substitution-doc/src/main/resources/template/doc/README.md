# Module substitution-doc
This module generates a documentation for the placeholder-substitution module.

## Usage
Checkout from Github

    git clone https://github.com/credibledoc/placeholder-substitution.git

Change directory

    cd placeholder-substitution
    
Build by Maven
    
    mvn clean install

Change directory
    
    cd substitution-doc\target
    
Create a log file. This log will be used as a recording of the tool behaviour.

    java -jar &&beginPlaceholder {
                  "className": "org.credibledoc.substitution.doc.module.substitution.jar.SubstitutionDocJarNameContentGenerator",
                  "description": "Current name of the substitution-doc-X.X.X.jar."
              } &&endPlaceholder

As a result, the `target/generated` folder with a new README.md and SVG files is generated.

## Templates used for generation of this documentation
This documentation is generated from these templates:

&&beginPlaceholder {
    "className": "org.credibledoc.substitution.doc.module.substitution.resource.ResourcesListMarkdownGenerator",
    "description": "List of resources from classpath of the substitution-doc application.",
    "parameters": {"endsWith": ".md"}
} &&endPlaceholder

## Behavior of the application
This diagram shows the main steps the application does after launching.

&&beginPlaceholder {
    "className": "org.credibledoc.substitution.doc.module.substitution.report.LaunchingUmlReportService",
    "description": "Diagram of the application launching."
} &&endPlaceholder