FROM java:8-jre-alpine
ENV VERSION 1.1.10
ADD admin-service/target/admin-service-${VERSION}.jar /opt/application/admin-service-${VERSION}.jar
ADD run.sh /opt/application/run.sh
ADD dockerconfig /opt/application/config
CMD ["/bin/sh","/opt/application/run.sh"]