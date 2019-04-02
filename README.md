# Gradle REST API Plugin

This Gradle plugin helps you to design REST API for Java backends in a unique way. The plugin currently supports

- **Micronaut** - a shiny new star at the Java landscape
- **JAX RS** - well, the specification dude!
- **Spring Boot**, the way who Spring does it.


Use this [link](https://bintray.com/saw303/gradle-plugins/gradle-java-rest-api?source=watch) to watch latest releases. 

## Build & Quality

[![CircleCI](https://circleci.com/gh/saw303/gradle-java-rest-api/tree/master.svg?style=svg)](https://circleci.com/gh/saw303/gradle-java-rest-api/tree/master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/daaa123d3d2c4023908ff8870bdbc7d2)](https://www.codacy.com/app/saw303/gradle-java-rest-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=saw303/gradle-java-rest-api&amp;utm_campaign=Badge_Grade)

## Getting started

The Gradle plugin is hosted at the following Bintray repository https://dl.bintray.com/saw303/gradle-plugins. In order to get this running with Gradle you need to declare that repository and apply the plugin.
If you feel uncomfortable on relying on a private repository feel free to create a mirror for it.

```
buildscript {
    repositories {
        maven { url "https://dl.bintray.com/saw303/gradle-plugins" }
    }
    dependencies {
        classpath 'ch.silviowangler.rest:gradle-java-rest-api:1.5.0'
    }
}

apply plugin: 'ch.silviowangler.restapi'
```

The Gradle plugin will introduce a new build category named `rest api` to your Gradle build. This category groups the following tasks:

- **cleanRestArtifacts** - Removes everything that the plugin has generated (except the stuff under version control)
- **extractSpecs** - Reads and extracts the specs files from the classpath
- **generateDiagrams** - Generates PlantUML diagrams of your REST API
- **generateRestArtifacts** - Generates Java code from your REST specification

## Demo application

This repository contains to very slim demo applications for **Spring Boot** and **Micronaut**.

The Spring Boot repo is located at `./demo-app-springboot`. The Micronaut app is located at `./demo-app-micronaut`.

## Hateoas Functionality

### Micronaut

To enable HATEOAS support you simply need to add the following config to your `application.yml`.

```
restapi:
    hateoas:
        filter:
            enabled: true
            uri: /v1/**
```

`restapi.hateoas.filter.enable` enables the following Micronaut specific HttpFilters.

- `HateoasResponseFilter` - creating the HATEOAS response model
- `ExpandedGetResponseFilter` - supports expanded gets

When HATEOAS is enabled you will get JSON responses containing _SELF_ links such as this example.

```
{
  "data": {
    "id": "CHE",
    "name": "Switzerland",
    "foundationDate": "1291-08-01",
    "surface": 41285
  },
  "links": [
    {
      "rel": "self",
      "method": "GET",
      "href": "/v1/countries/CHE"
    }
  ]
}
```

#### Expanded GETs

For our REST clients I would like to provide a feature I call "Expanded Gets" similar to table joins in SQL.

**Without "Expanded Gets"** a client has to execute at least two HTTP calls to read a person and its addresses.

- `/countries/CHE` - to read a country
- `/countries/CHE/cities/` - read all cities of that country

**With "Expanded Gets"** a client can read the person and its addresses in only one request.

- `/countries/CHE?expands=countries`

```
{
  "data": {
    "id": "CHE",
    "name": "Switzerland",
    "foundationDate": "1291-08-01",
    "surface": 41285
  },
  "links": [
    {
      "rel": "self",
      "method": "GET",
      "href": "/v1/countries/CHE"
    }
  ],
  "expands": [
    {
      "name": "cities",
      "data": [
        {
          "id": "ZH",
          "name": "Zurich",
          "koordinaten": {
            "longitude": 10,
            "latitude": 2147483647
          }
        },
        {
          "id": "BE",
          "name": "Berne",
          "koordinaten": {
            "longitude": 10,
            "latitude": 2147483647
          }
        },
        {
          "id": "GE",
          "name": "Geneva",
          "koordinaten": {
            "longitude": 10,
            "latitude": 2147483647
          }
        },
        {
          "id": "LVA",
          "name": "Lugano",
          "koordinaten": {
            "longitude": 10,
            "latitude": 2147483647
          }
        }
      ]
    }
  ]
}
```

### Spring Boot

Not yet supported

### JAX-RS

Not yet supported