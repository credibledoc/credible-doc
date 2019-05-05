# Usage of the log-combiner-core module

This module is the source for the &&beginPlaceholder {
          "className": "com.credibledoc.substitution.content.generator.jar.LocalJarNameContentGenerator",
          "description": "Current name of the log-combiner-core-X.X.X.jar.",
                  "parameters": {
                      "targetDirectoryRelativePath": "log-combiner-core/target",
                      "jarNamePrefix": "log-combiner-core-"
                  }
      } &&endPlaceholder library.

This library can be loaded by the Maven tool from the Maven central repository.

Example of Maven configuration in a `pom.xml` file

    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <dependencies>
            <dependency>
                <groupId>com.credibledoc</groupId>
                <artifactId>log-combiner-core</artifactId>
                <version>&&beginPlaceholder {
                   "className": "com.credibledoc.substitution.content.generator.jar.LocalVersionContentGenerator",
                   "description": "Current name of the log-combiner-core-X.X.X.jar.",
                   "parameters": {
                       "targetDirectoryRelativePath": "log-combiner-core/target",
                       "jarNamePrefix": "log-combiner-core-"
                   }
    } &&endPlaceholder</version>
            </dependency>
    ...