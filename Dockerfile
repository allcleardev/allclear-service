FROM gradle:jdk13 as build
WORKDIR /app
COPY . /app
RUN gradle build shadowJar

FROM sonarsource/sonar-scanner-cli as scan
WORKDIR /app
ARG GIT_BRANCH
ARG SONAR_TOKEN
COPY --from=build /app/* /app/
COPY ./build.gradle /app/
USER root
RUN ./scan.bash

# matches the FROM in gradle:jdk11
FROM adoptopenjdk:13-jdk-hotspot
WORKDIR /app
COPY --from=build /app/platform-server/conf/ /app/conf/
COPY --from=build /app/platform-server/build/libs/platform-server*.jar /app/platform-server.jar

ENV APP_ENV dev
EXPOSE 8080
CMD ["java", \
  "-Dfile.encoding=UTF-8", \
  "-Duser.timezone=UTC", \
  "-Dhibernate.dialect.storage_engine=innodb", \
  "-jar", "platform-server.jar", \
  "server", "conf/${APP_ENV}.json"]
