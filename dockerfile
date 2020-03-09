FROM openjdk:8u212-jdk
ENV VERSION 2.0.4
ADD admin-service/target/admin-service-${VERSION}.jar /opt/application/admin-service-${VERSION}.jar
ADD run.sh /opt/application/run.sh
ADD dockerconfig /opt/application/config
CMD ["/bin/sh","/opt/application/run.sh"]
