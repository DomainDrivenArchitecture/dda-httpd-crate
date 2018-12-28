FROM dda-httpd-crate

RUN apt-get install curl -y
RUN curl -L -o /app/serverspec.jar https://github.com/DomainDrivenArchitecture/dda-serverspec-crate/releases/download/1.3.0/dda-serverspec-standalone.jar
COPY integration/docker/single_static/test/serverspec.edn /app/serverspec.edn

RUN java -jar /app/serverspec.jar --install-dependencies /app/serverspec.edn
RUN service apache2 restart && \
    java -jar /app/serverspec.jar /app/serverspec.edn -v
