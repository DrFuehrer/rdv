## Introduction ##
RDV is developed in [Java](http://java.sun.com/) and uses Swing for it's UI. RDV also uses a of a number of free software libraries, including:

  * [Data Turbine](http://dataturbine.org/) for the dynamic data server
  * [JFreeChart](http://www.jfree.org/jfreechart/) for plotting and charting
  * [JLayer](http://www.javazoom.net/javalayer/javalayer.html), [MP3SPI](http://www.javazoom.net/mp3spi/mp3spi.html) , and [Tritonus](http://tritonus.org/) for audio
  * [JAXB](https://jaxb.dev.java.net/) for XML binding
  * and the [Swing Application Framework ](https://appframework.dev.java.net/), Apache Commons [CLI](http://commons.apache.org/cli/) and [Logging](http://commons.apache.org/logging/) for application infrastructure.

These libraries can be found in the `lib` directory.

## Requirements ##
To build RDV, the following programs are needed:

  * [Java SE Development Kit](http://java.sun.com/javase/) version 5 or greater
  * [Apache Ant](http://ant.apache.org/)

Make sure Ant's bin directory is in your path and JAVA\_HOME is set to the JDK path.

## Getting the Source Code ##
Source code for RDV releases can be obtained from the [downloads page](http://code.google.com/p/rdv/downloads/list?q=label:Type-Source). The latest source code can be obtained from the trunk of RDV's [SVN repository](http://code.google.com/p/rdv/source/checkout).

## Compiling ##
To compile RDV run the following command:

> `> ant compile`

This will compile all the java source files in `src` into class files and put them into `build/classes`

## Build JAR's ##
To make a jar of the class files, run:

> `> ant jar`

This will create `build/lib/rdv.jar`. This jar file will have it's `Main-Class` attribute set to RDV's main class. The jar's `Class-Path` attribute will also be set so all of RDV's dependent libraries will be loaded if they are in the same directory.

To create a jar file will all of RDV's dependent libraries included, run:

> `> ant bin`

This will create `build/bin/rdv.jar`. This jar file will be standalone in the sense that it require no additional libraries to run.

## Making a distribution ##
To make a distriution of RDV, run:

> `> ant dist`

This will create a binary and source distribution in `build/dist`. The binary distribution file will be named `rdv-<version>.zip` and will contain the standalone jar along with basic documentation text files. The source distribution file will be named `rdv-<version>-src.zip` and will contain the full source code and dependent libraries.

## Platform Installers ##
RDV can also be installed using the native installation mechanism for Windows, Mac, and Debian/Ubuntu.

### Windows ###
To build the Windows installer, you will need:
  * [Launch4j](http://launch4j.sourceforge.net/)
  * [NSIS](http://nsis.sourceforge.net/)

The LAUNCH4J\_HOME environmental variable needs to be set to the directory where Launch4j is installed and the NSIS installation directory needs to be added to the path.

To make the installer, run:

> `> ant installer`

This will create the installer at `build/exe/rdv-<version>-setup.exe`.

### Mac ###
To build the Mac disk image, you will need:
  * [Apple Developer Tools](http://developer.apple.com/tools/)

To make the disk image, run:

> `> ant mac`

This will create the disk image at `build/mac/rdv-<version>.dmg`.

### Debian/Ubuntu ###
To build the Debian package you will need to have the following packages installed:
  * dpkg-dev
  * fakeroot
  * debhelper
  * cdbs
  * openjdk-6-jdk
  * ant
  * libappframework-java
  * libcommons-cli-java
  * libcommons-logging-java
  * libjcommon-java
  * libjfreechart-java (>= 1.0.7)
  * librbnb-java
  * libswingworker-java
  * libtritonus-java

To create the Debian package, run:

> `> ant deb`

This will create the Debian package at `../rdv-<version>-<revision>.deb`.

## Ant Targets ##
Most of the any targets are described above, but here is a complete list of them.

| **Name**    | **Description**                             |
|:------------|:--------------------------------------------|
| api         | Build documentation                         |
| bin         | Build a standalone jar                      |
| clean       | Remove all generated files                  |
| compile     | Build from source                           |
| deb         | Build debian package                        |
| dist        | Build the binary and source distributions   |
| dist-bin    | Build the binary distribution               |
| dist-src    | Build the source distribution               |
| exe         | Build windows executable                    |
| help        | Show usage                                  |
| installer   | Build windows installer                     |
| jar         | Build jar                                   |
| mac         | Build a mac application bundle              |
| run         | Run application                             |
| test        | Run unit tests                              |