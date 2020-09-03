Start as a Java Agent
====

Usually Arthas dynamic attach the applications on the fly, but from version `3.2.0` onwards, Arthas supports starting directly as a java agent.

For example, download the full arthas zip package, decompress it and start it by specifying `arthas-agent.jar` with the parameter `-javaagent`.

````
java -javaagent:/tmp/test/arthas-agent.jar -jar arthas-demo.jar
````

Set startup parameters:

* Start and connect to channel server

```
java -javaagent:/tmp/test/arthas-agent.jar="telnetPort=-1;httpPort=-1;channelServer=localhost:7700" -jar arthas-demo.jar
```

* Start and connect to tunnel server

```
java -javaagent:/tmp/test/arthas-agent.jar="telnetPort=-1;httpPort=-1;tunnelServer=ws://localhost:7777/ws" -jar arthas-demo.jar
```


The default configuration is in the `arthas.properties` file in the decompression directory.

Reference: https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html