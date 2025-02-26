# This Dockerfile is used to build the scrapers such as one2goasia etc
# This is slightly complicated due to our multi-module approach and therefore this Dockerfile is at the root
# Passed as argument is the name of the module e.g one2goasia that you want to copy in.

# Image recommended on playwright website
FROM mcr.microsoft.com/playwright/java:v1.49.0-noble

ARG MODULE_TO_BUILD

WORKDIR /app

# Pre-install browsers during image build
RUN npx playwright install

COPY  /${MODULE_TO_BUILD}/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]