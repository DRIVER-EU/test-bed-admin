# Admin tool
Single Page Application to help you manage your personalized DRIVER+ test-bed and prepare the trial. It uses Apache Kafka as well as an Embedded Tomcat environment. The frontend is built with Vuejs.

# Introduction
The current version (v 0.0.1) of the Admin tool provides an overview of:
* the solutions connected to the Testbed, including their availability
* the available Kafka topics
* the gateways for message exchange

Furthermore, the Admin tool 
* creates the necessary topics, assigns schemas to topics and handles topic invitations.
* provides the possibility to initialize the testbed
* provides the possibility to start the trial once the testbed was initialized
* provides a section where log entries are displayed

# Installation
Currently no docker image for the Admin tool is available. The Admin tool can be started by invoking the bat file in the executable directory.
To run the Admin tool:
* Pull the executable folder to any destination on your local disk
* start the DB docker container: docker-compose up -d
* ensure that the JAVA_HOME environment variable is set and point to the a JRE 8
* start admin tool by invoking the bat file
* request the admin HMI by calling: http://localhost:8090/#/overview

# Swagger
For testing the Admin tool offers also a swagger interface.
This can be called by:
* http://localhost:8090/swagger-ui.html

# Configuration
The configuration is located in the config folder. Using the default configuration the complete testbed is running on the local machine. Changes for e.g. Kafka connection have to be made in the consumer/producer properties file. Details about the possible
parameters can be found in the adapter description.

## Solution configuration
Every solution that is part of the testbed configuration has to be added to the solutions.json file. The Admin tool loads the
configuration on startup. As soon as a heartbeat (HB) message is received by the admin tool service, the solution will be marked as
available and up. HB is checked every 5 seconds. If no HB is received within 10 seconds the solution is marked as down.

## Topic configuration
All topics used for data exchanged have to be configured in the topics.json file. Each topic that is configured has to have an
unique topic name. The core topics (type=core.topic) should not be modified as the adapters assume they are available. Standard topics
(type=standard.topic) have to define
* which standard (by defining the msgType) 
* in which version (by defining the msgTypeVersion)

they handle. e.g.: 
{
	"id":"standart.topic.cap",
	"type":"standard.topic",
 	"name":"standard_cap",
 	"msgType": "cap",
 	"msgTypeVersion": "1.2",
 	"state": false,
 	"publishSolutionIDs":["all"],
 	"subscribedSolutionIDs": ["all"],
 	"description":"This is the standard CAP topic."
}


## Gateway configuration
Gateways are more or less solutions in the testbed but they have additional configuration properties and therefor they are handled seperately. Like solutions gateways send heartbeat messages and are marked as up or down in the Admin tool.

# Initialize the testbed
After calling the Admin tool, it displays all information configured for the trial
* All involved solutions
* All used topics
* All gateways

## Auto create mode
In the client.properties the parameter init.auto defines if the topic creation is done by invoking (either by clicking on the button, or invoking the Rest Endpoint by e.g.: Trial Manager), or if the Admin tool should create all core topics on startup automatically. Be aware, the Kafka has to be up and running before the Admin Service is started. 


## Create all topics
By pressing the "INITIALIZE TESTBED" button (right top in the header) the Admin tool creates all needed core topics and registers the corresponding schemas on them. The status of the topic creation can be seen in the log and by the indicator in the topic.

## Start the trial
The trial is started by the user clicking the "START TRIAL" button. When the trial is started, all standard topics are going to be created, the schemas are registered and the invite message is sent to all solutions that are allowed to connect to the topics (as publisher or subscriber).

## Solution availability
Each solution has an indication like the topics. As soon as the heartbeat from a solution is received the indication is set to green, indicating it is available.

## Log list
Each log entry that is received by the Admin tool is stored in the DB and is visible on the Admin HMI.



