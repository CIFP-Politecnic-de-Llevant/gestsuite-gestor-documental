FROM maven:3-amazoncorretto-17 as develop-stage-gestor-documental
WORKDIR /app

COPY /config/ /resources/

COPY /api/gestsuite-common/ /external/
RUN mvn clean compile install -f /external/pom.xml

COPY /api/gestsuite-gestor-documental .
RUN mvn clean package -f pom.xml
ENTRYPOINT ["mvn","spring-boot:run","-f","pom.xml"]

FROM maven:3-amazoncorretto-17 as build-stage-gestor-documental
WORKDIR /resources

COPY /api/gestsuite-common/ /external/
RUN mvn clean compile install -f /external/pom.xml


COPY /api/gestsuite-gestor-documental .
RUN mvn clean package -f pom.xml

FROM amazoncorretto:17-alpine-jdk as production-stage-gestor-documental
COPY --from=build-stage-gestor-documental /resources/target/gestor-documental-0.0.1-SNAPSHOT.jar gestor-documental.jar
COPY /config/ /resources/
ENTRYPOINT ["java","-jar","/gestor-documental.jar"]