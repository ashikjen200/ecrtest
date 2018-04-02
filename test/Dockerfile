# fetch basic image

FROM maven:3.3.9-jdk-8

# application placed into /opt/app

RUN mkdir -p /opt/app

WORKDIR /opt/app

# selectively add the POM file and

COPY pom.xml /opt/app/

# rest of the project

COPY src /opt/app/src
# install dependencies
RUN ["mvn", "install"]
# execute it

CMD ["mvn", "exec:java"]



