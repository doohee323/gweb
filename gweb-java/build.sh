#!/usr/bin/env bash

set -x

## on mac
#brew install maven
#export M2_HOME=/usr/local/Cellar/maven/3.3.9

# on ubuntu
# wget http://www-eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
export M2_HOME=/home/ubuntu/apache-maven-3.3.9

export M2=$M2_HOME/bin
export PATH=$M2:$PATH

# cd ~/gweb-java

mvn clean compile package

rm -Rf target/client_secrets
rm -Rf target/stored_oauth2
rm -Rf target/logback.xml
rm -Rf target/gweb.conf

cp -Rf src/main/resources/client_secrets target
cp -Rf src/main/resources/stored_oauth2 target

cp src/main/resources/logback.xml target
cp src/main/resources/gweb.conf target

exit 0
