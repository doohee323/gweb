#!/usr/bin/env bash

set -x

cd target

java -cp gweb-java-1.0-SNAPSHOT-jar-with-dependencies.jar kr.getcha.estimatecalculator.RentCalculatorImpl 

exit 0
