# Usage of the log-combiner-core module

This module is the source for the `&&beginPlaceholder {
                                          "className": "com.credibledoc.substitution.content.generator.pom.JarNameContentGenerator",
                                          "description": "Latest name and version of log-combiner-core artifact in Maven Central Repository",
                                          "parameters": {
                                              "url": "https://repo1.maven.org/maven2/com/credibledoc/log-combiner-core/maven-metadata.xml",
                                              "nameAndVersionSeparator": "-"
                                          }
                                   } &&endPlaceholder` library.

This library can be loaded from the [Maven Central Repository](https://mvnrepository.com/artifact/com.credibledoc/log-combiner-core).

Example of Maven configuration in a `pom.xml` file

    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
        ... some mandatory tags omitted
    
        <dependencies>
            <dependency>
                <groupId>com.credibledoc</groupId>
                <artifactId>log-combiner-core</artifactId>
                <version>&&beginPlaceholder {
                        "className": "com.credibledoc.substitution.content.generator.pom.JarNameContentGenerator",
                        "description": "Latest version of log-combiner-core artifact in Maven Central Repository",
                        "parameters": {
                            "url": "https://repo1.maven.org/maven2/com/credibledoc/log-combiner-core/maven-metadata.xml",
                            "versionOnly": "true"
                        }
                 } &&endPlaceholder</version>
            </dependency>
        </dependencies>
    
    </project>