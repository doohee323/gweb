# Web app library based on Google Spreadsheets API

What's GWeb?

Libraries and samples to make web apps using Spreadsheet(Excel) as database and business logic server.

http://gweb.topzone.biz/

- Library
```
	1. Server-side Library for Google Script Api
		Google API Server-side library communicating gweb client library to support Google Script APIs.
		- /gweb-js/app/scripts/gweb_s.js: gweb server library
	2. Client-side Library
		Javascript library communicating gweb server library through Google OAuth2. Handling GWeb's parameters and result set.
		- /gweb-js/app/scripts/gweb_c.js: gweb client library
	3. Client-side Java Library
		Java library communicating gweb server library through Google OAuth2. Handling GWeb's parameters and result set.
		- /gweb-java/target/gweb-java-1.0.0.jar: gweb java client library
```
- Usecase
```
	1. Reuse excel file
	Can use MS Excel and it's functions as a database and business logic engine.
	2. Make a web app without application server
```
Can make a web app without application server or other server-side programming language, using Spreadsheet's function and UI javascripts.

- Examples
```
	Type 1. web app without any application server.
		- run with a static web server (google authentication required)
	Type 2. web app with spring boot.
		- run with spring boot (google authentication not required)
```


 

