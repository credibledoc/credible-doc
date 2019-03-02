# Module substitution-doc
This module generates a documentation for the
[placeholder-substitution](../README.md) module. This document is also generated
by the `substitution-doc` module.

## Usage
Checkout from Github

    git clone https://github.com/credibledoc/placeholder-substitution.git

Change directory

    cd placeholder-substitution
    
Build by Maven
    
    mvn clean install

Change directory
    
    cd substitution-doc
    
Launch the application

    java -jar target/&&beginPlaceholder {
                  "className": "org.credibledoc.substitution.doc.module.substitution.jar.SubstitutionDocJarNameContentGenerator",
                  "description": "Current name of the substitution-doc-X.X.X.jar."
              } &&endPlaceholder

As a result, the `target/generated/doc` folder with the new README.md file
and `target/generated/doc/img` with SVG file generated.

Please inspect these generated files in the `target/generated/doc` folder.

## Templates used for generation of this documentation
This documentation is generated from these templates:

&&beginPlaceholder {
    "className": "org.credibledoc.substitution.doc.module.substitution.resource.ResourcesListMarkdownGenerator",
    "description": "List of resources from classpath of the substitution-doc application.",
    "parameters": {"endsWith": ".md"}
} &&endPlaceholder

## Behavior of the application
After launching the application generates a log file in the `log` directory.
This log will be used as a recording of the tool behaviour. Application uses its
own log for generation of UML diagrams like this one, see below.

This diagram shows the main steps the application does after launching.

&&beginPlaceholder {
    "className": "org.credibledoc.substitution.doc.module.substitution.report.LaunchingUmlReportService",
    "description": "Diagram of the application launching."
} &&endPlaceholder