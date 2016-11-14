# gt-vista-fhir
HAPI FHIR Project to talk to VistA Ex API
About
--------------------------------------------------------------------------------
This application uses the HAPI FHIR API to expose webservices to talk with the 
VistA Exchange API available at https://ehmp.vaftl.us/resource/docs/vx-api.
Specifically, it exposes calls to the "FHIR" section of the API to return
patient queries for the following:
- Patient 
- MedicationOrder
- Condition
- Observation
- Ecounter
- Procedure
- MedicationAdministration
- AllergyIntolerance

How to build the WAR
--------------------------------------------------------------------------------
This application uses Maven to manage the build process.

The WAR file for the application can be built by running the following command
> mvn clean install

When Maven finishes the build there will be a WAR file for the application in the
`/target` directory.

How to configure
----------------
The application can be configured via the gtvistaex.properties file. This file contains properties for the VistaEx user, site, and endpoint URLS. By default the system is configured to use the user "sc1234" and the and the PANORAMA site, 9E7A.

How to deploy to Tomcat
--------------------------------------------------------------------------------
After running a "mvn clean install" command there will be a WAR file in the 
`/target` directory named "hapiGtVistaEx.war".

Copy that war file to the `$CATALINA_HOME/webapps` directory.

Then start Tomcat, and it will deploy the WAR file.

To get the conformance statement
- http://localhost:8080/hapiGtVistaEx/fhir/metadata

You will be able to hit the web services at:
- http://localhost:8080/gt-vista-fhir/fhir/Patient/3
- http://localhost:8080/gt-vista-fhir/fhir/Patient?id=3&_format=json
- http://localhost:8080/gt-vista-fhir/fhir/Patient?_id=3&_format=json
- http://localhost:8080/gt-vista-fhir/fhir/Patient?identifier=3&_format=json
- http://localhost:8080/gt-vista-fhir/fhir/MedicationOrder?patient=3&_format=json
- http://localhost:8080/gt-vista-fhir/fhir/Condition?patient=3&_format=json
- http://localhost:8080/gt-vista-fhir/fhir/Observation?patient=3&_format=json
- http://localhost:8080/gt-vista-fhir/fhir/Encounter?patient=3&_format=json
- http://localhost:8080/gt-vista-fhir/fhir/MedicationAdministration?patient=3&_format=json
- http://localhost:8080/gt-vista-fhir/fhir/Procedure?patient=3&_format=json
- http://localhost:8080/gt-vista-fhir/fhir/AllergyIntolerance?patient=3&_format=json

How to run locally with Jetty
--------------------------------------------------------------------------------
For testing the application can be run locally using the Maven Jetty plugin. It
allows Maven to create/run a Jetty servlet container that can be hit by a web
browser/client for testing. To start up the application in Jetty, run the following
Maven command:
> mvn jetty:run

To get the conformance statement
- http://localhost:8080/fhir/metadata

You will be able to hit the web services at:
- http://localhost:8080/fhir/Patient/3
- http://localhost:8080/fhir/Patient?id=3&_format=json
- http://localhost:8080/fhir/Patient?_id=3&_format=json
- http://localhost:8080/fhir/Patient?identifier=3&_format=json
- http://localhost:8080/fhir/MedicationOrder?patient=3&_format=json
- http://localhost:8080/fhir/Condition?patient=3&_format=json
- http://localhost:8080/fhir/Observation?patient=3&_format=json
- http://localhost:8080/fhir/Encounter?patient=3&_format=json
- http://localhost:8080/fhir/MedicationAdministration?patient=3&_format=json
- http://localhost:8080/fhir/Procedure?patient=3&_format=json
- http://localhost:8080/fhir/AllergyIntolerance?patient=3&_format=json
