# myTrack Project

This project appears to be a Java application built with Gradle. It includes a `Dockerfile` and `docker-compose-arm64.yml`, suggesting it's designed to be containerized. 

## General 
- This project serves as a backend to track GPS position data from multiple trackers owned by multiple users
- The project aims to be used in different domains, for example: camping tracking routes and overnight stays, tracking and securing assets like bicycles or pets
- The project exposes various REST endpoints intended to be used by apps

## Architecture
- It uses the Spring framework
- Database used is POSTGIS
- All documentation shall be in english language
- All rest endpoints have the prefix /api/v1/
- The service layer is using an interface / implementation class approach.
- All REST Endpoints are implemented in REST Controllers residing in the package com.tmv.core.controller

## Project Structure

* **`src/`**: Contains the Java source code.
* **`build.gradle`**: The build script for the Gradle build tool.
* **`Dockerfile`**: Defines the Docker image for the application.
* **`docker-compose-arm64.yml`**: Docker Compose file for ARM64 architecture.
* **`docs/`**: Likely contains project documentation.
* **`docs/api-docs/openapi.yml contains a documentation of REST Endpoints serving the purpose to generate front-end code for an app
* **`docs/API.md contains a human readable documentation of the API

This `agent.md` file is a starting point. Feel free to expand it with more details about the project's architecture, setup, and deployment.
