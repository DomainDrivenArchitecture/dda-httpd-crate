FROM ubuntu

RUN apt-get update && apt-get upgrade -y && apt-get install openjdk-8-jre -y

COPY target/dda-httpd-standalone.jar /app/dda-httpd-standalone.jar
COPY integration/docker/single_proxy/image/httpd.edn /app/

RUN java -jar /app/dda-httpd-standalone.jar /app/httpd.edn
