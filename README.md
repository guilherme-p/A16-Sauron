# Sauron

Distributed Systems 2019-2020, 2nd semester project


## Getting Started

The overall system is composed of multiple modules.
The main server is the _silo_.
The clients are the _eye_ and _spotter_.

See the [project statement](https://github.com/tecnico-distsys/Sauron/blob/master/README.md) for a full description of the domain and the system.

See the [project report](https://github.com/guilherme-p/A16-Sauron/blob/master/report/README.md) for a full description of our implementation.

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

To confirm that you have them installed, open a terminal and type:

```
javac -version

mvn -version
```

### Installing

To compile and install all modules:

```
mvn clean install -DskipTests
```

The integration tests are skipped because they require the servers to be running.


### Running

#### Server

To run the server:

```
cd silo-server
mvn exec:java
```

#### Eye

To run the eye:

```
cd eye/target/appassembler/bin/
./eye host port name latitude longitude
```

#### Spotter

To run the spotter:

```
cd spotter/target/appassembler/bin/
./spotter host port
```

## Built With

* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework


## Versioning

We use [SemVer](http://semver.org/) for versioning. 
