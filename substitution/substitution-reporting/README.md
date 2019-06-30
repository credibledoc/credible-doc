Notice: work in progress.

# Module `substitution-reporting`
This module is a java library. This library helps to generate reports.
Reports are parts of some application documentation.

## Usage
This module is the source for the `substitution-reporting-1.0.8` library.

This library can be loaded from the [Maven Central Repository](https://mvnrepository.com/artifact/com.credibledoc/substitution-reporting).

Example of Maven configuration in a `pom.xml` file

    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
        ... some mandatory tags omitted
    
        <dependencies>
            <dependency>
                <groupId>com.credibledoc</groupId>
                <artifactId>substitution-reporting</artifactId>
                <version>1.0.8</version>
            </dependency>
        </dependencies>
    
    </project>

## How the generated reports look like
Example: [diagrams.md](../substitution-doc/doc/diagrams.md)

Parts of documentation for this repository [credible-doc](../README.md),
for example [diagrams.md](../substitution-doc/doc/diagrams.md), are generated with help of this `substitution-reporting` module.

## Dependencies of this `substiturion-reporting` module on other modules of the `credible-doc` repository
![UML diagram of dependencies `substiturion-reporting` module on classes from other modules of the `credible-doc` repository.](img/README.md_3.svg?sanitize=true)

_UML diagram of dependencies `substiturion-reporting` module on classes from other modules of the `credible-doc` repository._
