# Admin tool
Single Page Application to help you manage your personalized DRIVER+ test-bed and prepare the trial. It uses Apache Kafka as well as an Embedded Tomcat environment.

# Introduction
The current version (v 0.0.1) of the Admin tool provides an overview of:
* the tools connected to the Testbed, including their availability
* the available Kafka topics
* the gateways for message exchange

Furthermore, the Admin tool 
* creates the necessary topics, assigns schemas to topics and handles topic invitations.
* provides the possibility to initialize the testbed
* provides the possibility to start the trial once the testbed was initialized
* provides a section where log entries are displayed

# Installation
Currently no docker image for the admin tool is available. The Admin tool can be started by invoking the bat file in the executable directory.
To run the Admin tool:
* Pull the executable folder to any destination on your local disk
* start the DB docker container: docker-compose up -d
* ensure that the JAVA_HOME environment variable is set and point to the a JRE 8
* start admin tool by invoking the bat file
* request the admin HMI by calling: http://localhost:8090/#/overview

# Swagger
For testing the admin tool offers also a swagger interface.
This can be called by:
* http://localhost:8090/swagger-ui.html

# configuration
The configuration is located in the config folder. The general configuration is to have e complete Testbed running on the local machine. Changes for e.g. Kafka connection have to be made in the consumer/producer properties file. Details about the possible
parameters can be found in the adapter description.

## solution configuration
Every solution that is part of the testbed configuration has to be added to the solutions.json file. The AdminTool loads the
configuration on startup. As soon as a heartbeat (HB) message is received by the admin tool service, the solution will be marked as
available and up. HB is checked every 5sec. if not HB is received within 10sec. the solution is marked as down.

## topic configuration
All topics used for data exchanged have to be configured in the topics.json file. Each topic that is configured has to have an
unique topic name. The core topics type=core.topic should not be touched as the adapters assume they are available. Standard topics
type=standard.topic have to defined 
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


## gateway configuration
Gateways are more or less solutions in the testbed but they have add. configuration properties and there for they are handled seperately. ALso gateways send heartbeat messages and are marked as up/down in the Admin Tool 

# initialize the testbed
After calling the admin tool, it displays all information configured for the Trail
* All involved Solutions
* All used topics
* All gateways

## auto create mode
In the client.properties the parameter init.auto true/false defines if the topic creation is done by invoking (either by clicking on the Button, or invoking the Rest Endpoint by e.g.: Trail Manager), or if the admin tool should create all core topics on startup automatically. Be aware, the Kafka has to be up, before the Admin Service is started. 


## create all topics
by pressing the initTestbed button (right top in the header) the admin tool creates all needed core topics and registers the corresponding schemas on them. The status ofthe topic creation can be seen in the log and by the indication image in the topic.

## start the trial
When the trail is started, all standard topics are going to be created, the schemas are registered and the invite message is send to all solutions that are allowed to connect (as publisher or subscriber).

## solution availability
Each solotuion has alss an indication like the topics. As soon as the heartbeat from a solution is received the indication is set to green, which means available.

## log list
Each log entry that is received by the admin tool is stored in the DB and is visible on the Admin HMI.



