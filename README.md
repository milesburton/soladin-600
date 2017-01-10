# Soladin 600 Adapter


## Supporting libraries
* Scala 2.11
* Akka
* RXTX
* Spray

## Getting started

Assuming you have installed Scala

sbt assembly

This will produce target/scala-2.11/solar-assembly-0.0.1.jar

As a fat JAR you can copy this to your target device or simply run it with java target/scala-2.11/solar-assembly-0.0.1.jar and you will be provided with the options.

### Raspberry Pi?
* Install Java: http://www.rpiblog.com/2014/03/installing-oracle-jdk-8-on-raspberry-pi.html
* sudo apt-get install librxtx-java

## All
 java -Djava.library.path=/usr/lib/jni/ -jar solar-assembly-0.0.1.jar  --serialPort /dev/ttyUSB0  --sampleTimeInSeconds 60 -k 435711868351581993789e2031283637c3c5745a
