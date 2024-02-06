HashCode
===============

Find usages of the identity hashCode function, on which classes it is called, how often and where.

Identity hash functions are problematic with Project Lilliput, so the question is: Are they even used that often?
This agent tries to answer this question for specific workload by logging every usage of the identity hashCode function.

Build
-----
```sh
# build it
git clone https://github.com/parttimenerd/hashCode-agent
cd hashCode-agent
mvn package
```

Usage
-----
```sh
Usage: java -javaagent:hashCode.jar=options ...          
Options:                                                 
    help: Print this help message                        
    output: file to store the found hashCode usages      
    logEveryUsage: log every usage of hashCode to stderr 
```

Example Usage
-------------
```sh
# download renaissance benchmark
test -e renaissance.jar || wget https://github.com/renaissance-benchmarks/renaissance/releases/download/v0.15.0/renaissance-gpl-0.15.0.jar -O renaissance.jar
# run renaissance with hashCode agent and store the output
java -javaagent:./target/hashCode.jar=output=usages.txt,logEveryUsage=true -jar renaissance.jar all         
```

Idea
----
- (not supported yet) replace every `System.identityHashCode(o)` call with a call to a custom function
- (not supported yet) replace every `.hashCode()` call with a call to a custom function
- add a `hashCode` function to a class that hasn't any
- the custom function logs the usage of hashCode and then calls the original hashCode function


License
-------
MIT, Copyright 2024 SAP SE or an SAP affiliate company, Johannes Bechberger
and hashCode agent contributors


*This project is a prototype of the [SapMachine](https://sapmachine.io) team
at [SAP SE](https://sap.com)*