# Module log-combiner
The module creates a simple command-line tool for merging of log files
with a different format of lines timestamps to a single file or readable source
with lines sorted by timestamps.

## Download
This tool can be [downloaded from the Maven Central Repository](https://mvnrepository.com/artifact/com.credibledoc/log-combiner),
see the image

1. Select the latest version

![Link to the Maven Central Repository](doc/img/mvnRepository.png)


2. Select the link next to the ``Files`` word in the page, see the image

![Link to the jar file](doc/img/linkToJar.png)


## Usage
    java -jar log-combiner-1.0.31.jar <folderAbsolutePath> [configAbsolutePath]

## Examples
Combine all the files in the `/var/log/temp` folder recursively.

    java -jar log-combiner-1.0.31.jar /var/log/temp
                             
In case of the `log-combiner.properties` configuration file in place the log files will be
merged with the configuration parameters. Otherwise the default configuration parameters
will be used and the files will be joined by the files with the final time modification.

    java -jar log-combiner-1.0.31.jar /var/log/temp /var/log/combiner/two-apps.properties
In this case the log files from the `/var/log/temp` folder will be merged. And the
`/var/log/combiner/two-apps.properties` configuration file will be used. We assumed
the `two-apps.properties` file in the `/var/log/combiner/` folder exists.

## Arguments description
* `log-combiner-1.0.31.jar` (mandatory) is an executable jar file. Latest release is located on the Maven Central Repository.
It can be [downloaded from the Maven Central Repository](https://mvnrepository.com/artifact/com.credibledoc/log-combiner),
see the link next to the 'Files' in the page, see images above.
* `folderAbsolutePath` (mandatory) is the path to a folder with log files for merging.
The files will be parsed in this folder and sub-folders recursively.
* `configAbsolutePath` (optional) is a configuration file path. The example of the file see below. If the `configAbsolutePath`
is not defined in the command line, the default value is `log-combiner.properties` located
in the same folder, next to the log-combiner-1.0.31.jar file.
If the configuration file not found, all the files will be merged by the final time modification.

## Configuration file log-combiner.properties

The example of the `log-combiner.properties` file contains the configuration of two
different formats of log files - `app0` and `app1`.

    # File log-combiner.properties
    
    insertLineSeparatorBetweenFiles = false
    
    printNodeName = true
    
    targetFileName = joined.log.txt
    
    # Example of timestamp: 22.04.2019 07:59:27.910
    regex[0] = \\d\\d\\.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d
    maxIndexEndOfTime[0] = 24
    simpleDateFormat[0] = dd.MM.yyyy HH:mm:ss.SSS
    applicationName[0] = app0
    
    # Example of timestamp: 2019-04-22T07:59:27.920+0100
    regex[1] = \\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d[+-]\\d\\d\\d\\d
    maxIndexEndOfTime[1] = 40
    simpleDateFormat[1] = yyyy-MM-dd'T'HH:mm:ss.SSSZ
    applicationName[1] = app1
    
    #regex[2] = ...
    #maxIndexEndOfTime[2] = ...
    #simpleDateFormat[2] = ...
    #applicationName[2] = ...


### Parameters description
* `insertLineSeparatorBetweenFiles` (optional, default false, allowed value `true`) if defined as `true`,
System.lineSeparator() will be inserted after each file except the last one
* `printNodeName` (optional, default true, allowed value `false`) if defined as `true`, log lines from sub-folders will be prefixed
with the sub-folder name. The option is useful in case when the same application is installed on multiple nodes and each node generates
its own logs. In this case each node files should be placed in the sub-folders. See example below.
* `regex` (mandatory) the datetime pattern provides searching for dates in the log files
* `maxIndexEndOfTime` (optional) if defined, the first part of a line will be checked for a datetime pattern by a matcher.
If not set, the whole line will be checked by the matcher. For example if the whole line is 100 characters length,
and `maxIndexEndOfTime` is set fot `20`, the datetime will be checked
in a substring from `0` to `20` characters of the line `exclusive`.
* `simpleDateFormat` (mandatory) a pattern for parsing datetime string to a `java.util.Date` object
* `applicationName` (optional) if defined, each line in a merged file will be prefixed with this value.
It is useful for better readability of merged files, where logs from different applications and nodes are
combined into a single file. In this case each line can be distinguished which application it belongs to.
For example:


     node1 app1 INFO 2019-04-22T07:59:27.000+0200 [main] Application app1 started.
     
     node0 app0 22.04.2019 07:59:27.910 [main] INFO Application app0 started.
     
     node1 app0 22.04.2019 07:59:27.910 [thread2] INFO Application app0 started.

The example of a folder structure with the log files with multiple nodes (`node0` and `node1`)
and multiple applications (`app0` and `app1`) :

    folder
        node0
            app0.log
        node1
            app0.log
            app1.log

* `targetFileName` (optional, default value "combined.txt") file name where all source log files will be combined.
        
[This page](doc/usage/programmatically.md) describes how to use this library programmatically.
