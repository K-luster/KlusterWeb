FROM openjdk:11-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN curl -L https://github.com/kubernetes/kompose/releases/download/v1.31.0/kompose-linux-amd64 -o kompose
RUN chmod +x kompose
ENTRYPOINT ["java","-jar","/app.jar"]