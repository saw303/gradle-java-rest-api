# Gradle REST API Plugin

This Gradle plugin helps you to design REST API for Java backends in a unique way. The plugin currently supports

- **Micronaut** - a shiny new star at the Java landscape
- **JAX RS** - well, the specification dude!
- **Spring Boot**, the way who Spring does it.

## Build & Quality

[![CircleCI](https://circleci.com/gh/saw303/gradle-java-rest-api/tree/master.svg?style=svg)](https://circleci.com/gh/saw303/gradle-java-rest-api/tree/master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/daaa123d3d2c4023908ff8870bdbc7d2)](https://www.codacy.com/app/saw303/gradle-java-rest-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=saw303/gradle-java-rest-api&amp;utm_campaign=Badge_Grade)

## Getting started

The Gradle plugin is hosted my private Bintray repository https://dl.bintray.com/saw303/gradle-plugins. In order to get this running with Gradle you need to declare that repository and apply the plugin.

```
buildscript {
    repositories {
        maven { url "https://dl.bintray.com/saw303/gradle-plugins" }
    }
    dependencies {
        classpath 'ch.silviowangler.rest:gradle-java-rest-api:1.4.2'
    }
}

apply plugin: 'ch.silviowangler.restapi'
```

The Gradle plugin will introduce a new build category named `rest api` to your Gradle build. This category groups the following tasks:

- **cleanRestArtifacts** - Removes everything that the plugin has generated (except the stuff under version control)
- **extractSpecs** - Read the specs from the classpath
- **generateDiagrams** - Generate PlantUML diagrams of your REST API
- **generateRestArtifacts** - Generate Java code from your REST specification