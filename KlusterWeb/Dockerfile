FROM openjdk:11-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN apt-get update && apt-get install -y curl
RUN curl -L https://github.com/kubernetes/kompose/releases/download/v1.31.0/kompose-linux-amd64 -o kompose
RUN chmod +x kompose
RUN mv kompose /usr/local/bin
ENTRYPOINT ["java","-jar","/app.jar"]
