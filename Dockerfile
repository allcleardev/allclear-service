FROM gradle:jdk13 as build
WORKDIR /app
COPY . /app
RUN gradle build shadowJar

# matches the FROM in gradle:jdk11
FROM adoptopenjdk:13-jdk-hotspot
WORKDIR /app
COPY --from=build /app/platform-server/conf/ /app/conf/
COPY --from=build /app/platform-server/build/libs/platform-server*.jar /app/platform-server.jar
CMD ["java", \
  "-Dfile.encoding=UTF-8", \
  "-Duser.timezone=UTC", \
  "-Dhibernate.dialect.storage_engine=innodb", \
  "-jar", "platform-server.jar", \
  "server", "conf/dev.json"]
