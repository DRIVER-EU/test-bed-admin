FROM java:8-jre-alpine
ADD admin-service/target/admin-service-1.1.0.jar /opt/application/admin-service-1.1.0.jar
ADD run.sh /opt/application/run.sh
ADD dockerconfig /opt/application/config
CMD ["/bin/sh","/opt/application/run.sh"]


