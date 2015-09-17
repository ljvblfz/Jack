########
Overview
########

Jack is a new Android toolchain generating dex files. Jack is available in AOSP, reduces the number
of external tools and provides an incremental compilation support to reduce development compilation
time.

Jack source code is available in AOSP in ub-jack* branches,
Overview, supported features, how to use in Gradle and SDK release notes can be found on our public
tool page http://tools.android.com/tech-docs/jackandjill.

Jack provides:

A compiler from Java programming language source to the Android dex file format
Several features integrated in the same tool
An incremental compilation to speedup the compilation time
Jill is another tool that translates an existing .jar to a .jack library file format.



#####################################
How to use in the Android source tree
#####################################

Below we describe how to use Jack in the Android source tree. Please have a look to Limitations
sections before starting using it.



How to use Jack
---------------

If you're working in a tree supporting Jack and it's not default you can enable Jack with

$ export ANDROID_COMPILE_WITH_JACK=true

Then use your standard makefile commands to compile the tree or your project.

Jack uses a local compilation server on your computer. For more information about the Jack server
and its configuration files see jack-server/README.txt.

Currently the incremental compilation is not enabled by default. Please have a look to 'Using Jack
incremental compilation' section.



Troubleshooting
---------------

If you have issues, or want to provide feedback

To report bugs or request features use our issue tracker, available at http://b.android.com and use
the templates Jack tool bug report
(https://code.google.com/p/android/issues/entry?template=Jack%20bug%20report)
or Jack tool feature request
(https://code.google.com/p/android/issues/entry?template=Jack%20feature%20request).


How to get detailed build logs in case of Jack failure

In case of Jack failures, you can have a more detailed log by setting one variable:

$ export ANDROID_JACK_EXTRA_ARGS="--verbose debug --sanity-checks on -D sched.runner=single-threaded"

Then use your standard makefile commands to compile the tree or your project and attach its standard
output and error.
Attach also the Jack server log: The log is at $ANDROID_BUILD_TOP/out/dist/logs/jack-server.log if
you ran a make command with a dist target, or else you can find it in by running
"jack-admin server-log".


To remove detailed build logs use:

$ unset ANDROID_JACK_EXTRA_ARGS

Your problems may also be related to the Jack server, check the "troubleshooting" section of
jack-server/README.txt.


If you are blocked due to a Jack bug

You can continue to work
on your own tree by temporarily switching to javac/dx toolchain. You must remove all dex files in
the 'out' directory, and disable Jack with the command lines below. Then use your standard makefile
commands to compile the tree or your project.

$ mm clean-dex-files

$ export ANDROID_COMPILE_WITH_JACK=false

If a Jack bug is solved, you can switch back to Jack toolchain in your tree, you must remove all dex
files in the 'out' directory, and enable Jack with the command line below. Then use your standard
makefile commands to compile the tree or your project.

$ mm clean-dex-files

$ export ANDROID_COMPILE_WITH_JACK=true



Using Jack incremental compilation
----------------------------------

Enabling incremental support in a project

Add the following line to the Android.mk file of the project that you want to build incrementally.

LOCAL_JACK_ENABLED := incremental

The first time that you build your project with Jack if some dependencies are not built, use mma to
build them, and after that you can use the standard build command.


If you have issues

If you are experimenting issues using the incremental support, first please file a bug report and
attach the incremental log that is located in the folder jack-incremental/logs into the output
folder of your module to the bug report. Please send also detailed build logs (see previous section
for how to).

Then clean your incremental folder to restart from a full compilation of your module in order to see
 if it resolves your problem by using:

mm clean-jack-incremental

Then if the problem persists, it is recommended to disable incremental support and try again (see
below for how to).


How to disable incremental support

Revert the modification done in your Android.mk and clear your incremental folder by using the
following command lines:

mm clean-jack-incremental



Known limitations
-----------------

- The Jack server is mono-user by default, so can be only used by one user on a computer. If it is
  not the case, please, choose different port numbers for each user and adjust SERVER_NB_COMPILE
  accordingly. You can also disable the Jack server by setting SERVER=false in your $HOME/.jack.
- CTS compilation is very slow because of vm-tests-tf integration.
- The build system is not fully optimized for Jack: we're currently doing extra jobs during the tree
  compilation.
- Bytecode manipulation tools, like JaCoCo, are not supported.


