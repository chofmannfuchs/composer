#!/bin/bash
./mvnw clean verify -DskipTests
docker build --build-arg="JAR_FILE=/target/composer.jar" --build-arg="CONF_FILE=/composer.conf" --tag="kotlin-workshop/composer-service:latest" .
