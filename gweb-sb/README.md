GWeb sample web app with Spring boot.
=========================================================

You can run like this,

mvn package

java -jar target/gweb-sb-1.0-SNAPSHOT.jar --spring.profiles.active=local 

It includes some features,

- gweb-sb (Google Script API library)
- restful apis with http request / response for json format
- h2
- spring security for restful api
- logback

# change library file path
pom.xml
<systemPath>/vagrant/gweb-sb/libs/gweb-1.0.0.jar</systemPath>

# in eclipse debug
- Project: gweb-sb
- Main class: example.Application
- Program arguments: --spring.profiles.active=local

# in eclipse
mvn spring-boot:run

mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8880"

# in console

mvn clean compile package

# run prod mode

mvn spring-boot:run
or
java -jar target/gweb-sb-1.0-SNAPSHOT.jar --spring.profiles.active=local

cf. --spring.config.location=classpath:file:/vagrant/gweb-sb/src/main/resources/application-local.properties


http://localhost:8088/app/index.html

   

