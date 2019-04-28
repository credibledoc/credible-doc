# Repository log-combiner
Allows to combine (merge) log files with different format of lines timestamps
to a single file or InputStream with lines sorted by timestamps.

This repository contains the following modules
* [combiner-core](combiner-core/README.md) - contains classes with business logic.
This module is a source for a `jar` library file.
* [log-combiner](log-combiner/README.md) - contains the 'log-combiner' command-line tool.
This tool can be used for merging log files with different format of lines timestamps to a single file or InputStream.
* [log-combiner-doc](log-combiner-doc/README.md) - contains classes for generation of this repository documentation.