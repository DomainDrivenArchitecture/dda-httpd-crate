FROM domaindrivenarchitecture/dda-httpd-crate

RUN curl -L -o /app/serverspec.jar https://github.com/DomainDrivenArchitecture/dda-serverspec-crate/releases/download/1.2.1/dda-serverspec-crate-1.2.1-standalone.jar
COPY integration/docker/test/serverspec.edn /app/serverspec.edn

RUN java -jar /app/serverspec.jar /app/serverspec.edn
