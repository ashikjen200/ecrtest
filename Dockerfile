# fetch basic image
FROM maven:3.3.9-jdk-8

# application placed into /opt/app
RUN mkdir -p /opt/app
WORKDIR /opt/app

# selectively add the POM file and
# install dependencies
COPY pom.xml /opt/app/
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]
# rest of the project
COPY src /opt/app/src

RUN ["mvn", "compile"]

# execute it
CMD ["mvn",  "exec:java"]
