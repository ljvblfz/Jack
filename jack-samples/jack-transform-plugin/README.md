# Transform plugin sample

This plugin demonstrates how a schedulable can transform the Jack IR. In this example, the
transformation consists in inserting a 'System.out.println' statement at the begininning of every
method coming from the source files.

This plugin introduces the following concepts of Jack:
  * Plugin
  * Schedulables
  * Feature
  * Transformation

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
cd jack-transform-plugin
./gradlew
```

The plugin .jar file is located at `build/libs/jack-transform-plugin.jar`.

## Execute the plugin

The plugin's name is `com.android.jack.sample.instrumentation.InstrumentationSamplePlugin`.
Here is an example of command-line to compile source files with this plugin.

```
java -jar dist/jack.jar
 --pluginpath jack-samples/jack-transform-plugin/build/libs/jack-transform-plugin.jar
 --plugin com.android.jack.sample.instrumentation.InstrumentationSamplePlugin
 --output-dex=<dex_dir> <source_files>
```

where `<dex_dir>` is the directory where the classes.dex file is generated and `<source_files>`
are the source files to be compiled.

You can take a look at the Jack's command-line usage with

```
java -jar dist/jack.jar --help
```
