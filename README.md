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

How to deploy to Tomcat
--------------------------------------------------------------------------------
After running a "mvn clean install" command there will be a WAR file in the 
`/target` directory named "hapiGtVistaEx.war".

Copy that war file to the `$CATALINA_HOME/webapps` directory.

Then start Tomcat, and it will deploy the WAR file.

To get the conformance statement
- http://localhost:8080/hapiGtVistaEx/fhir/metadata

You will be able to hit the web services at:
- http://localhost:8080/hapiGtVistaEx/fhir/Patient/9E7A%3B3
- http://localhost:8080/hapiGtVistaEx/fhir/Patient?id=9E7A%3B3&_format=json
- http://localhost:8080/hapiGtVistaEx/fhir/Patient?_id=9E7A%3B3&_format=json
- http://localhost:8080/hapiGtVistaEx/fhir/Patient?identifier=9E7A%3B3&_format=json
- http://localhost:8080/hapiGtVistaEx/fhir/MedicationOrder?patient=9E7A%3B3&_format=json
- http://localhost:8080/hapiGtVistaEx/fhir/Condition?patient=9E7A%3B3&_format=json
- http://localhost:8080/hapiGtVistaEx/fhir/Observation?patient=9E7A%3B3&_format=json
- http://localhost:8080/hapiGtVistaEx/fhir/Encounter?patient=9E7A%3B3&_format=json
- http://localhost:8080/hapiGtVistaEx/fhir/MedicationAdministration?patient=9E7A%3B3&_format=json
- http://localhost:8080/hapiGtVistaEx/fhir/Procedure?patient=9E7A%3B3&_format=json
- http://localhost:8080/hapiGtVistaEx/fhir/AllergyIntolerance?patient=9E7A%3B3&_format=json

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
- http://localhost:8080/fhir/Patient/9E7A%3B3
- http://localhost:8080/fhir/Patient?id=9E7A%3B3&_format=json
- http://localhost:8080/fhir/Patient?_id=9E7A%3B3&_format=json
- http://localhost:8080/fhir/Patient?identifier=9E7A%3B3&_format=json
- http://localhost:8080/fhir/MedicationOrder?patient=9E7A%3B3&_format=json
- http://localhost:8080/fhir/Condition?patient=9E7A%3B3&_format=json
- http://localhost:8080/fhir/Observation?patient=9E7A%3B3&_format=json
- http://localhost:8080/fhir/Encounter?patient=9E7A%3B3&_format=json
- http://localhost:8080/fhir/MedicationAdministration?patient=9E7A%3B3&_format=json
- http://localhost:8080/fhir/Procedure?patient=9E7A%3B3&_format=json
- http://localhost:8080/fhir/AllergyIntolerance?patient=9E7A%3B3&_format=json
