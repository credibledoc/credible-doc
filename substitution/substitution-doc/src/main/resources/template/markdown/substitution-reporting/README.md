Notice: work in progress.

# Module substitution-reporting
This module is a java library. This library helps to generate reports.
Reports are documents describing some application or system structure and behavior.

## Usage
This module is the source for the &&beginPlaceholder {
                                          "className": "com.credibledoc.substitution.content.generator.pom.JarNameContentGenerator",
                                          "description": "Latest name and version of substitution-reporting artifact in Maven Central Repository",
                                          "parameters": {
                                              "url": "https://repo1.maven.org/maven2/com/credibledoc/substitution-reporting/maven-metadata.xml",
                                              "nameAndVersionSeparator": "-"
                                          }
                                   } &&endPlaceholder library.

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
                <version>&&beginPlaceholder {
                        "className": "com.credibledoc.substitution.content.generator.pom.JarNameContentGenerator",
                        "description": "Latest version of substitution-reporting artifact in Maven Central Repository",
                        "parameters": {
                            "url": "https://repo1.maven.org/maven2/com/credibledoc/substitution-reporting/maven-metadata.xml",
                            "versionOnly": "true"
                        }
                 } &&endPlaceholder</version>
            </dependency>
        </dependencies>
    
    </project>

## How the generated reports look like
Example: [diagrams.md](../substitution-doc/doc/diagrams.md)

Parts of documentation for this repository [credible-doc](../README.md),
for example [diagrams.md](../substitution-doc/doc/diagrams.md), are generated with help of this `substitution-reporting` module.