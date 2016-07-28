# Stats plugin sample

This plugin allows to compute statistics about the types, fields and methods that are processed
during the compilation.

This plugin introduces the following concepts of Jack:
  * Plugin
  * Schedulables
  * Features
  * Properties
  * Statistics
  * Tracer

## Fetch the code

This plugin is hosted in the Jack development branch in AOSP. You can follow the instructions at
http://source.android.com/source/downloading.html to setup your machine then execute the following
commands

```
mkdir <jack_project_dir>
cd <jack_project_dir>
repo init -u https://android.googlesource.com/platform/manifest -b ub-jack
repo sync
```

where `<jack_project_dir>` is the directory where you want to download the Jack repository.


## Build the plugin

First you need to initialize the plugin's local dependencies with the following commands

```
cd toolchain/jack
ant dist
cd jack-samples
ant
```

Then you can build the plugin using Gradle with the following commands

```
cd jack-stats-plugin
./gradlew
```

The plugin .jar file is located at `build/libs/jack-stats-plugin.jar`.

## Execute the plugin

The plugin's name is `com.android.jack.sample.stats.StatsPlugin`. Here is an example of
command-line to compile source files with this plugin.

```
java -jar dist/jack.jar
 --pluginpath jack-samples/jack-stats-plugin/build/libs/jack-stats-plugin.jar
 --plugin com.android.jack.sample.stats.StatsPlugin
 -D sched.tracer=stat-only
 -D sched.tracer.file=<stats_output_file>
 -D sched.tracer.format=text
 --output-dex=<dex_dir> <source_files>
```

where
* `<stats_output_file>` is the file where all statistics will be written into
* `<dex_dir>` is the directory where the classes.dex file is generated
* `<source_files>` are the source files to be compiled.

By default, the plugin will process types (classes or interfaces), fields and methods. It is
possible to select them individually using the following properties:

* `jack.sample.stats.class`: enables/disables statistics about types
* `jack.sample.stats.field`: enables/disables statistics about fields
* `jack.sample.stats.method`: enables/disables statistics about methods

It is also possible to generate the statistics file in JSON. Simply change the value of the
 property `sched.tracer.format=json` in the previous command-line.

You can take a look at the Jack's command-line usage with

```
java -jar dist/jack.jar --help
```

You can also take a look at the list of Jack properties, including the ones from this plugin, with

```
java -jar dist/jack.jar
 --pluginpath jack-samples/jack-stats-plugin/build/libs/jack-stats-plugin.jar
 --plugin com.android.jack.sample.stats.StatsPlugin
 --help-properties
```

## Tracer

The `com.android.sched.util.log.Tracer` interface is used to update statistics during the
compilation.

At the end of the compilation, it generates an HTML report (`index.html` file) of these statistics
 in the directory provided by the `--tracer-dir <tracer_dir>` option of the Jack command-line.

In this report, you can look for the statistics of the plugin, prefixed by:
 * `jack.sample.stats.class.<...>` for statistics about classes
 * `jack.sample.stats.field.<...>` for statistics about fields
 * `jack.sample.stats.method.<...>` for statistics about methods