Notice: work in progress.

# Module substitution-doc
This module generates a documentation for the
[placeholder-substitution](../README.md) module. This document is also generated
by this `substitution-doc` module.

## Usage
Please open a command line and try it yourself.

Checkout this project from Github

    git clone https://github.com/credibledoc/placeholder-substitution.git

Change directory

    cd placeholder-substitution
    
Build by Maven
    
    mvn clean install

Change directory
    
    cd substitution-doc
    
Launch the application

    java -jar target/substitution-doc-1.0.0-SNAPSHOT.jar

As a result, the `target/generated/markdown` folder with new folders and files
is generated.

Please inspect these generated files in the `substitution-doc/target/generated/markdown` folder.
These generated files is the documentation you are currently reading. You can use
this approach for generation of documentation for your own projects.

## Templates used for generation of this documentation
This documentation is generated from these templates:

* [/template/markdown/doc/diagrams.md](src/main/resources/template/markdown/doc/diagrams.md)
* [/template/markdown/README.md](src/main/resources/template/markdown/README.md)


As you can notice, this generated document and its template have common parts,
and this diff shows different parts. These different parts are placeholders and
the content generated from these placeholders.
![Image of differences between template and generated files](doc/img/diffBetweenTemplateAndGeneratedFiles.png)

## Examples of PlantUML diagrams

The [diagrams.md](doc/diagrams.md) page shows some examples of PlantUML diagrams
generated from log files.