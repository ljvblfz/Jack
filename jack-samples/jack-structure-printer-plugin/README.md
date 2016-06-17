# Structure printer plugin sample

This plugin allows to print the type and the members (fields and methods) in the  that
exist in the code. This structure is printed either in stdout, stderr or a file according
to a property.

This plugin introduces the following concepts of Jack:
  * Plugin
  * Schedulables
  * Marker
  * Production
  * Properties
  * WriterFiles

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
cd jack-structure-printer-plugin
./gradlew
```

The plugin .jar file is located at `build/libs/jack-structure-printer-plugin.jar`.

## Execute the plugin

The plugin's name is `com.android.jack.sample.structureprinting.StructurePrintingSamplePlugin`. Here is an
example of command-line to compile source files with this plugin.

```
java -jar dist/jack.jar
 --pluginpath jack-samples/jack-structure-printer-plugin/build/libs/jack-structure-printer-plugin.jar
 --plugin com.android.jack.sample.structureprinting.StructurePrintingSamplePlugin
 -D jack.samples.structure.print.file=<output_file>
 --output-dex=<dex_dir> <source_files>
```

where `<output_file>` is the file where the structure will be printed (`-` means stdout and `--` means stderr),
`<dex_dir>` is the directory where the classes.dex file is generated and `<source_files>`
are the source files to be compiled.

If the property `jack.samples.structure.print.file` is not specified, stdout will be used.

You can take a look at the Jack's command-line usage with

```
java -jar dist/jack.jar --help
```



