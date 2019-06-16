# Module substitution-doc
This module generates a documentation for the
[substitution-parent](../README.md) module. This document is also generated
by this `substitution-doc` module.

## Usage
Please open a command line and try it yourself.

Clone this project from Github

    git clone https://github.com/credibledoc/credible-doc.git

Build projects and install artifacts by Maven
    
    mvn clean install -f credible-doc/pom.xml

Change directory
    
    cd credible-doc/substitution
    
Launch the application

    java -jar substitution-doc/target/substitution-doc-1.0.7-SNAPSHOT.jar

As a result, the `credible-doc/substitution/target/generated/markdown` folder with new folders and files
is generated.

Please inspect these generated files in the `credible-doc/substitution/target/generated/markdown` folder.
These generated files is the documentation you are currently reading. You can use
this approach for generation of documentation for your own projects.

## Templates used for generation of this documentation
This documentation is generated from these templates:

* [/template/markdown/README.md](src/main/resources/template/markdown/README.md)
* [/template/markdown/substitution-core/README.md](src/main/resources/template/markdown/substitution-core/README.md)
* [/template/markdown/substitution-doc/doc/diagrams.md](src/main/resources/template/markdown/substitution-doc/doc/diagrams.md)
* [/template/markdown/substitution-doc/doc/img/diffBetweenTemplateAndGeneratedFiles.png](src/main/resources/template/markdown/substitution-doc/doc/img/diffBetweenTemplateAndGeneratedFiles.png)
* [/template/markdown/substitution-doc/README.md](src/main/resources/template/markdown/substitution-doc/README.md)
* [/template/markdown/substitution-generators/README.md](src/main/resources/template/markdown/substitution-generators/README.md)
* [/template/markdown/substitution-reporting/README.md](src/main/resources/template/markdown/substitution-reporting/README.md)


As you can notice, this generated document and its template have common parts,
and this diff shows different parts. These different parts are placeholders and
the content generated from these placeholders.
![Image of differences between template and generated files](doc/img/diffBetweenTemplateAndGeneratedFiles.png)

## Examples of PlantUML diagrams

The [diagrams.md](doc/diagrams.md) page shows some examples of PlantUML diagrams
generated from log files.