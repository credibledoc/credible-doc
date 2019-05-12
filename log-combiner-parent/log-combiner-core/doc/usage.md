# Usage of the log-combiner-core module

This module is the source for the log-combiner-core-1.0.5-SNAPSHOT.jar library.

This library can be loaded by the Maven tool from the Maven central repository.

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
                <version>1.0.4</version>
            </dependency>
        </dependencies>
    
    </project>