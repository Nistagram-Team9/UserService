FROM maven:3.6.3-ibmjava-8-alpine AS userServiceBuild
ARG STAGE=dev
WORKDIR /usr/src/userService
COPY . .
RUN mvn package -P${STAGE} -DskipTests


FROM openjdk:8-jdk-alpine AS userServiceRuntime
WORKDIR /app
COPY --from=userServiceBuild /usr/src/userService/target/userService.jar ./
EXPOSE 8080
CMD java -jar userService.jar