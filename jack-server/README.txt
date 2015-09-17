###########
Jack server
###########

The first time jack is used, it launches a local Jack compilation server on your computer:
- This server brings an intrinsic speedup because it avoids launching a new JVM, loading Jack code,
  initializing Jack and warming up the JIT at each compilation. This server speeds up the
  compilation of a tree to be really close to the old toolchain, but also provides very good
  compilation times during small compilations (e.g. in incremental mode).
- The server is also a short-term solution to control the number of parallel Jack compilations, and
  so to avoid the overloading of your computer (memory or disk issue), because it limits the number
  of parallel compilations.

The Jack server shutdowns itself after an idle time without any compilation. It uses two TCP ports
on the localhost interface, and so is not available externally. All these parameters (number of
parallel compilations, timeout, ports number, ... can be modified by editing the $HOME/.jack file).



$HOME/.jack file
----------------

The $HOME/.jack file are setting Jack server variables in a full bash syntax. Follows description
with default value:

SERVER=true
  Enable the server feature of jack

SERVER_PORT_SERVICE=8072
  Set the TCP port number of the server for compilation purpose

SERVER_PORT_ADMIN=8073
  Set the TCP port number of the server for admin purpose

SERVER_COUNT=1
  Not used today

SERVER_NB_COMPILE=4
  Number of maximum parallel compilations allowed

SERVER_TIMEOUT=60
  Number of idle seconds the server has to wait without any compilation before shutting down itself

SERVER_LOG=${SERVER_LOG:=$SERVER_DIR/jack-$SERVER_PORT_SERVICE.log}
  File where server logs are written. By default, this variable can be overloaded by environment
  variable

JACK_VM_COMMAND=${JACK_VM_COMMAND:=java}
  The default command used to launch a JVM on the host. By default, this variable can be overloaded
  by environment variable



Log file
--------

The log is at $ANDROID_BUILD_TOP/out/dist/logs/jack-server.log if you ran a make command with a dist
target, or else you can find it in by running "jack-admin server-log".



Troubleshooting
---------------

If your computer becomes unresponsive during compilation or if you experience Jack compilations
failing on “Out of memory error.”

You can improve the situation by reducing the number of jack simultaneous compilations by editing
your $HOME/.jack and changing SERVER_NB_COMPILE to a lower value.


If your compilations are failing on “Cannot launch background server”

The most likely cause is TCP ports are already used on your computer. Try to change it by editing
your $HOME/.jack (SERVER_PORT_SERVICE and SERVER_PORT_ADMIN variables).

If it doesn’t solve the problem, please report and attach you compilation log and the jack server
log (see previous section where to find it). To unblock the situation, disable jack compilation
server by editing your $HOME/.jack and changing SERVER to false. Unfortunately this will
significantly slow down your compilation and may force you to launch make -j with load control
(option "-l" of make). For example we're using make -j -l 60 on a Z620. Feel free to adjust -l
argument according to your machine configuration.


If your compilation gets stuck without any progress

Please report and give us additional information when possible:
- The command line getting stuck.
- The output this command line.
- The result of executing a “jack-admin server-stat”.
- The file $HOME/.jack.
- The content of the server log with the server state dumped:
  - Find the jack background server process: “jack-admin list-server”.
  - Send a “kill -3” to this server to dump his state in the log file.
  - To know where is the server log file, execute “jack-admin server-log”.
- The result of executing “ls -lR $TMPDIR/jack-$USER”.
- The result of running something like “ps j -U $USER”.

You should be able to unblock yourself by killing the Jack background server (use “jack-admin
kill-server”).


