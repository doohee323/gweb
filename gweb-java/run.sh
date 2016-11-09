#!/usr/bin/env bash

set -x

cd target

java -cp gweb-java-1.0.0-jar-with-dependencies.jar example.estimate.RentCalculatorImpl 

exit 0
