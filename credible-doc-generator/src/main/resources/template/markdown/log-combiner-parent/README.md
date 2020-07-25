# Module log-combiner-parent
It enables to combine (merge) log files with a different format of lines timestamps
to a single file or readable source with lines sorted by timestamps.

The module contains the following modules
* [log-combiner-core](log-combiner-core/README.md) - contains classes with business logic.
The module is the source for the `jar` library file.

* [log-combiner](log-combiner/README.md) - contains the `log-combiner` command-line tool.
The tool can be used for merging log files with various formats of line timestamps to a single file or readable source.

* [log-labelizer](log-labelizer/README.md) - the module contains tools for parsing, reading and analyzing of log files.
