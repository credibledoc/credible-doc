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

    java -jar substitution-doc-1.0.0-SNAPSHOT.jar

As a result, the `target/generated` folder with a new README.md and SVG files is generated.

## Templates used for generation of this documentation
This documentation is generated from these templates:

    /template/doc/README.md


## Behavior of the application
This diagram shows the main steps the application does after launching.

![Diagram of the application launching.](img/README.md_3.svg?sanitize=true)