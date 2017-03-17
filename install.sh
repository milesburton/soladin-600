#!/bin/bash

sbt assembly
scp target/scala-2.11/solar-assembly-0.0.1.jar sdr:~
ssh sdr "~/solar.sh"