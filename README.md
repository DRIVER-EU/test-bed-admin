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
* ensure that the JAVA_HOME environment variable ist set and point to the a JRE 8
* start admin tool by invoking the bat file
* request the admin HMI by calling: http://localhost:8090/#/overview

# Swagger
For testing the admin tool offers also a swagger interface.
This can be called by:
* http://localhost:8090/swagger-ui.html

# initialize the testbed
After calling the admin tool, it displays all information configured for the Trail
* All involved Solutions
* All used topics
* All gateways

## create all topics
by pressing the initTestbed button (right top in the header) the admin tool creates all needed core topics and registers the corresponding schemas on them. The status ofthe topic creation can be seen in the log and by the indication image in the topic.

## start the trial
When the trail is started, all standard topics are going to be created, the schemas are registered and the invite message is send to all solutions that are allowed to connect (as publisher or subscriber).

## solution availability
Each solotuion has alss an indication like the topics. As soon as the heartbeat from a solution is received the indication is set to green, which means available.

## log list
Each log entry that is received by the admin tool is stored in the DB and is visible on the Admin HMI.



