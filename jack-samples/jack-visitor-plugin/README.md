# Visitor plugin sample

This plugin allows to count the number of postfix operations (either `<expr>++` or `<expr>--`) that
exist in the code by visiting the internal representation of every method. That number is then
reported to the user.

This plugin introduces the following concepts of Jack:
  * Plugin
  * Schedulables
  * Marker
  * Feature
  * Visitor
  * User message reporting

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
cd jack-visitor-plugin
./gradlew
```

The plugin .jar file is located at `build/libs/jack-visitor-plugin.jar`.

## Execute the plugin

The plugin's name is `com.android.jack.sample.countervisitor.CounterVisitorPlugin`. Here is an
example of command-line to compile source files with this plugin.

```
java -jar dist/jack.jar
 --pluginpath jack-samples/jack-visitor-plugin/build/libs/jack-visitor-plugin.jar
 --plugin com.android.jack.sample.countervisitor.CounterVisitorPlugin
 --verbose info --output-dex=<dex_dir> <source_files>
```

where `<dex_dir>` is the directory where the classes.dex file is generated and `<source_files>`
are the source files to be compiled.

You can take a look at the Jack's command-line usage with

```
java -jar dist/jack.jar --help
```


