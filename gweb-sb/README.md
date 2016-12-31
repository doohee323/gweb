GWeb sample web app with Spring boot.
=========================================================

You can run like this,

# Feature
```
	It includes some features,
	
	- gweb-sb (Google Script API library)
	- restful apis with http request / response for json format
	- h2
	- spring security for restful api
	- logback

	// change library file path
		pom.xml
		<systemPath>/vagrant/gweb-sb/libs/gweb-1.0.0.jar</systemPath>
```

# Run
```
	1. in eclipse debug
		- Project: gweb-sb
		- Main class: example.Application
		- Program arguments: --spring.profiles.active=local
	
	2. in eclipse
		mvn spring-boot:run
		mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8888"
	
	3. in console
		mvn clean compile package
	
	4. run prod mode
		mvn spring-boot:run
		or
		java -jar /usr/local/etc/gweb-sb/gweb-sb-1.0.0.jar --spring.profiles.active=local
		
		cf. --spring.config.location=classpath:file:/vagrant/gweb-sb/src/main/resources/application-local.properties
		
		http://localhost:8880/app/estimate.html
		http://localhost:8880/app/checklist.html
		http://localhost:8880/app/public.html
```
   
# build / install debian
```
	cd /vagrant/gweb/gweb-sb/src
	bash build_deb.sh
```

# install / remove, start/stop service
```
	sudo dpkg -i gweb-sb.deb
	sudo dpkg -r gweb-sb
	sudo dpkg -P gweb-sb
	
	sv start gweb-sb
	sv stop gweb-sb
	sv force-stop gweb-sb
	sv status gweb-sb
```

