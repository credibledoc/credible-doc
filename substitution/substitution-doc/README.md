# Module substitution-doc
This module generates a documentation for the
[placeholder-substitution](../README.md) module. This document is also generated
by this `substitution-doc` module.

## Usage
Please open a command line and try it yourself.

Clone this project from Github

    git clone https://github.com/credibledoc/plantuml.git
    git clone https://github.com/credibledoc/placeholder-substitution.git
    git clone https://github.com/credibledoc/log-combiner.git

Build projects and install artifacts by Maven
    
    mvn clean install -f plantuml/pom.xml
    mvn clean install -f placeholder-substitution/pom.xml
    mvn clean install -f log-combiner/pom.xml

Change directory
    
    cd placeholder-substitution
    
Launch the application

    java -jar substitution-doc/target/substitution-doc-1.0.4-SNAPSHOT.jar

As a result, the `placeholder-substitution/target/generated/markdown` folder with new folders and files
is generated.

Please inspect these generated files in the `placeholder-substitution/target/generated/markdown` folder.
These generated files is the documentation you are currently reading. You can use
this approach for generation of documentation for your own projects.

## Templates used for generation of this documentation
This documentation is generated from these templates:

* [/template/markdown/](src/main/resources/template/markdown/)
* [/template/markdown/doc/](src/main/resources/template/markdown/doc/)
* [/template/markdown/doc/img/](src/main/resources/template/markdown/doc/img/)
* [/template/markdown/doc/img/diffBetweenTemplateAndGeneratedFiles.png](src/main/resources/template/markdown/doc/img/diffBetweenTemplateAndGeneratedFiles.png)
* [/template/markdown/README.md](src/main/resources/template/markdown/README.md)
* [/template/markdown/doc/diagrams.md](src/main/resources/template/markdown/doc/diagrams.md)


As you can notice, this generated document and its template have common parts,
and this diff shows different parts. These different parts are placeholders and
the content generated from these placeholders.
![Image of differences between template and generated files](doc/img/diffBetweenTemplateAndGeneratedFiles.png)

## Examples of PlantUML diagrams

The [diagrams.md](doc/diagrams.md) page shows some examples of PlantUML diagrams
generated from log files.