# Module log-combiner
This module is a simple command-line tool for merging log files
with different format of lines timestamps to a single file or InputStream
with lines sorted by timestamps.

# Usage
    java -jar log-combiner.jar <folderAbsolutePath> [configAbsolutePath]

## Examples
Combine all files in the `/var/log/temp` folder recursively.

    java -jar log-combiner.jar /var/log/temp
In case of existing `log-combiner.properties` configuration file log files will be
merged with configuration parameters. Else default configuration parameters
will be used and files will be joined by last modification time.

    java -jar log-combiner.jar /var/log/temp /var/log/combiner/three-app.properties
In this case log files from the `/var/log/temp` folder will be merged. And the
`/var/log/combiner/three-app.properties` configuration file will be used. In this case you should create
the `three-app.properties` file in the `/var/log/combiner/` folder.

## Arguments description
* `log-combiner.jar` (mandatory) is the latest release TODO Kyrylo Semenko link of the log-combiner tool
* `folderAbsolutePath` (mandatory) is a path to a folder with log files for merging. Files will be parsed in this folder and sub-folders recursively.
* `configAbsolutePath` (optional) is a configuration file path. Example of the file see below. If the `configAbsolutePath`
is not defined, default is `log-combiner.properties` placed next to the jar file.
If the file not found all files will be merged by last modification time.

## Configuration file log-combiner.properties
    # File log-combiner.properties
    
    insertLineSeparatorBetweenFiles = false
    
    printNodeName = true
    
    regex[0] = \d\d\.\d\d\.\d\d\d\d\s\d\d:\d\d:\d\d\.\d\d\d
    maxIndexEndOfTime[0] = 24
    simpleDateFormat[0] = dd.MM.yyyy HH:mm:ss.SSS
    applicationName[0] = app0
    
    regex[1] = \d\d\.\d\d\.\d\d\d\d;\d\d:\d\d:\d\d\.\d\d\d
    maxIndexEndOfTime[1] = 40
    simpleDateFormat[1] = dd.MM.yyyy;HH:mm:ss.SSS
    applicationName[1] = app1
    
    regex[2] = ...
    maxIndexEndOfTime[2] = ...
    simpleDateFormat[2] = ...
    applicationName[2] = ...

### Parameters description
* `insertLineSeparatorBetweenFiles` (optional, default false, allowed value `true`) if defined as `true`,
System.lineSeparator() will be inserted after each file except the last one
* `printNodeName` (optional, default true, allowed value `false`) if defined as `true`, log lines from sub-folders will be prefixed
by sub-folder name. It is useful in case when the same application is installed on multiple nodes and each node generates
its own logs. In this case each node files should be places in sub-folders. See example below.
* `regex` (mandatory) the pattern of datetime searching in a log line
* `maxIndexEndOfTime` (optional) if defined, the first part of a line will be searched by matcher for datetime pattern.
If not set, the whole line will be searched by matcher. For example if the whole line is 100 characters length,
and `maxIndexEndOfTime` is set to `20`, the datetime will be searching
in a substring from `0` to `20` characters of the line `exclusive`.
* `simpleDateFormat` (mandatory) a pattern for parsing datetime string to `java.util.Date` object
* `applicationName` (optional) if defined, each line in a merged file will be prefixed by this value.
It is useful for better readability of merged files, where logs from different applications and nodes
combined into a single file. In this case each line can be distinguished which application it belongs to.
For example:


    node1 app1 INFO 2019-04-22T07:59:27.000+0200 [main] Application app1 started.
    node0 app0 22.04.2019 07:59:27.910 [main] INFO Application app0 started.
    node1 app0 22.04.2019 07:59:27.910 [thread2] INFO Application app0 started.

Example of folder structure with log files from multiple nodes:

    folder
        node0
            app0.log
        node1
            app0.log
            app1.log
        
# How to use this tool programmatically
TODO