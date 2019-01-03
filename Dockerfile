FROM openjdk:8-alpine

# Required for starting application up.
RUN apk update && apk add bash

RUN mkdir -p /opt/app
ENV PROJECT_HOME /opt/app

COPY ./target/member-0.0.1-SNAPSHOT.jar $PROJECT_HOME/member-0.0.1-SNAPSHOT.jar

WORKDIR $PROJECT_HOME

CMD ["java", "-Dspring.data.mongodb.uri=mongodb://springboot-mongo:27017/members","-Djava.security.egd=file:/dev/./urandom","-jar","./member-0.0.1-SNAPSHOT.jar"]