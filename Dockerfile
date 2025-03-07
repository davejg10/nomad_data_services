# This Dockerfile is used to build the scrapers such as one2goasia etc
# This is slightly complicated due to our multi-module approach and therefore this Dockerfile is at the root
# Passed as argument is the name of the module e.g one2goasia that you want to copy in.
FROM curlimages/curl:latest AS newrelic-downloader

# Install unzip
RUN apk add --no-cache unzip

# Download and extract New Relic agent
RUN curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip \
    && unzip newrelic-java.zip

# Image recommended on playwright website
FROM mcr.microsoft.com/playwright/java:v1.49.0-noble AS scraper-image

ARG MODULE_TO_BUILD

WORKDIR /app

RUN mkdir -p /usr/local/newrelic
COPY --from=newrelic-downloader /newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
COPY ./newrelic.yml /usr/local/newrelic/newrelic.yml

COPY  /${MODULE_TO_BUILD}/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-javaagent:/usr/local/newrelic/newrelic.jar", "-jar", "/app/app.jar"]