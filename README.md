# Gradle REST API Plugin

This Gradle plugin helps you to design REST API for Java backends in a unique way. The plugin currently supports

- **Micronaut** - a shiny new star at the Java landscape (Supported versions are 1.2.6 at greater)
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
}

plugins {
  id 'ch.silviowangler.restapi' version '2.2.12'
}
```

or with the old way:

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

## Set up your Gradle build

To setup your Gradle build you need to apply the REST Generator plugin.

```groovy

// register the plugin
buildscript {
    repositories {
        jcenter()
        maven { url "https://dl.bintray.com/saw303/gradle-plugins" }
    }
    ext {
        restApiPluginVersion = '2.2.12'
    }
}

// apply it to your build
plugins {
  id 'ch.silviowangler.restapi' version "${restApiPluginVersion}"
}

// configure it

restApi {
    // Generate the resource classes to the following package
    packageName = 'my.package.name'
    
    // I would like to use Micronaut
    targetFramework = TargetFramework.MICRONAUT
    
    // the specification file are located at 
    optionsSource = file('src/main/specs')
    
    // Generation mode ALL (default), API or IMPLEMENTATION
    generationMode = GenerationMode.ALL 
}
```

## Example project

The following example will create contain the following three resources:

- root `/v1`
- countries `/v1/countries`
- cities `/v1/countries/{countryId}/cities`

## Defining resources

In the example above we told the REST Generator that our resource specification files are located
at `src/main/specs`. Make sure you have created that folder.

Now lets create a first resource called `root` resource. Therefore we create a file `src/main/specs/root.v1.json`.

```json
{
  "general": {
    "name": "root",
    "description": "This is the root resource in version 1.0.0",
    "version": "1.0.0",
    "x-route": "/v1"
  },
  "verbs": [],
  "fields": [],
  "subresources": [
    {
      "name": "countries",
      "type": "application/json",
      "rel": "Länder Dokumentation",
      "href": "/v1/countries",
      "method": "OPTIONS",
      "expandable": false
    }
  ],
  "pipes": [],
  "types": [
    {
      "name": "coordinates",
      "fields": [
        {
          "name": "longitude",
          "type": "decimal",
          "options": null,
          "min": 0.0,
          "max": null,
          "multiple": false,
          "defaultValue": null
        },
        {
          "name": "latitude",
          "type": "int",
          "options": null,
          "min": 0.0,
          "max": null,
          "multiple": false,
          "defaultValue": null
        }
      ]
    }
  ],
  "validators": []
}
```

The `countries` resource.

```json
{
  "general": {
    "name": "countries",
    "description": "countries",
    "version": "1.0.0",
    "lifecycle": {
      "deprecated": false,
      "info": "Version is valid"
    },
    "searchable": true,
    "countable": false,
    "x-route": "/v1/countries/:entity"
  },
  "verbs": [
    {
      "verb": "GET_ENTITY",
      "rel": "Read a country",
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "parameters": [],
      "permissions": [],
      "caching": {
        "no-cache": true,
        "private": false,
        "max-age": -2,
        "Expires": -1,
        "ETag": true
      }
    },
    {
      "verb": "GET_COLLECTION",
      "rel": "Read all countries",
      "collectionLimit": 19,
      "maxCollectionLimit": 101,
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "permissions": []
    },
    {
      "verb": "HEAD_ENTITY",
      "rel": "Verify if a country exists",
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "parameters": [],
      "permissions": []
    },
    {
      "verb": "HEAD_COLLECTION",
      "rel": "Verify if countries exist",
      "collectionLimit": 19,
      "maxCollectionLimit": 101,
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "permissions": []
    },
    {
      "verb": "PUT",
      "rel": "Modiy a country",
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "parameters": [],
      "permissions": []
    },
    {
      "verb": "POST",
      "rel": "Add a country",
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "parameters": [],
      "permissions": []
    }
  ],
  "fields": [
    {
      "name": "id",
      "type": "uuid",
      "options": null,
      "mandatory": [
        "PUT"
      ],
      "min": null,
      "max": null,
      "multiple": false,
      "defaultValue": null,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "Unique id of a country"
    },
    {
      "name": "name",
      "type": "string",
      "options": null,
      "mandatory": [
        "PUT", "POST"
      ],
      "min": 0,
      "max": 100,
      "multiple": false,
      "defaultValue": null,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "Name of the country"
    },
    {
      "name": "foundationDate",
      "type": "date",
      "options": null,
      "mandatory": [
        "PUT", "POST"
      ],
      "min": null,
      "max": null,
      "multiple": false,
      "defaultValue": null,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "Foundation date of a country"
    },
    {
      "name": "surface",
      "type": "int",
      "options": null,
      "mandatory": [
        "PUT", "POST"
      ],
      "min": null,
      "max": null,
      "multiple": false,
      "defaultValue": null,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "Surface in square kilometers"
    },
    {
      "name": "coordinates",
      "type": "coordinates",
      "options": null,
      "mandatory": [
        "PUT", "POST"
      ],
      "min": null,
      "max": null,
      "multiple": false,
      "defaultValue": null,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "Coordinates"
    }
  ],
  "subresources": [
    {
      "name": "cities",
      "type": "application/json",
      "rel": "orte",
      "href": "/v1/countries/{:entity}/cities",
      "method": "OPTIONS",
      "expandable": true
    }
  ]
}
```

The `cities` resource

```json
{
  "general": {
    "name": "cities",
    "description": "City resource",
    "version": "1.0.0",
    "lifecycle": {
      "deprecated": false,
      "info": "This version is valid"
    },
    "searchable": true,
    "countable": false,
    "x-route": "/v1/countries/:country/cities/:entity"
  },
  "verbs": [
    {
      "verb": "GET_ENTITY",
      "rel": "Read city",
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "options": [],
      "permissions": []
    },
    {
      "verb": "GET_COLLECTION",
      "rel": "Read all cities",
      "collectionLimit": 19,
      "maxCollectionLimit": 101,
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "options": [],
      "permissions": []
    },
    {
      "verb": "PUT",
      "rel": "Modify a city",
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "options": [],
      "permissions": []
    },
    {
      "verb": "POST",
      "rel": "Create a city",
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "options": [],
      "permissions": []
    },
    {
      "verb": "DELETE_ENTITY",
      "rel": "Delete a city",
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server eventually not reachable or to slow"
        }
      ],
      "representations": [
        {
          "name": "json",
          "comment": "",
          "responseExample": "{...}",
          "isDefault": true,
          "mimetype": "application/json"
        }
      ],
      "options": [],
      "permissions": []
    }
  ],
  "fields": [
    {
      "name": "id",
      "type": "uuid",
      "options": null,
      "mandatory": [
        "PUT"
      ],
      "min": null,
      "max": null,
      "multiple": false,
      "defaultValue": null,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "City id"
    },
    {
      "name": "name",
      "type": "string",
      "options": null,
      "mandatory": [
        "PUT", "POST"
      ],
      "min": null,
      "max": 20,
      "multiple": false,
      "defaultValue": null,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "City name"
    },
    {
      "name": "coordinates",
      "type": "coordinates",
      "options": null,
      "mandatory": [
        "PUT", "POST"
      ],
      "min": null,
      "max": null,
      "multiple": false,
      "defaultValue": null,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "Coordinates of the city"
    }
  ],
  "parameters": [],
  "subresources": []
}
```

## Declaring fields

Currently the following field types are supported:

- `date` => `java.time.LocalDate`
- `datetime` => `java.time.Instant`
- `decimal` => `java.math.BigDecimal`
- `int` => `java.lang.Integer`
- `long` => `java.lang.Long`
- `double` => `java.lang.Double`
- `float` => `java.lang.Double`
- `bool` => `java.lang.Boolean`
- `flag` => `java.lang.Boolean`
- `string` => `java.lang.String`
- `email` => `java.lang.String`
- `uuid` => `java.lang.String`
- `money` => `javax.money.MonetaryAmount` [JSR-354](https://javamoney.github.io/apidocs/javax/money/MonetaryAmount.html)
- `locale` => `java.util.Locale` (use this one to specify a language or even )
- `enum` => will create a enum type
- `object` => `java.lang.Object` (I recommend not using this type)
- `phoneNumber` => `java.lang.String` with validation on POST & PUT requests.

### creating an enum type

```json
{
  "name": "gender",
  "type": "enum",
  "options": ["MALE", "FEMALE"],
  "multiple": false,
  "defaultValue": null,
  "x-comment": "Gender of a person"
}
```

will create the following Java output

```java
public enum GenderType { MALE, FEMALE }
```

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
          "coordinates": {
            "longitude": 10,
            "latitude": 2147483647
          }
        },
        {
          "id": "BE",
          "name": "Berne",
          "coordinates": {
            "longitude": 10,
            "latitude": 2147483647
          }
        },
        {
          "id": "GE",
          "name": "Geneva",
          "coordinates": {
            "longitude": 10,
            "latitude": 2147483647
          }
        },
        {
          "id": "LVA",
          "name": "Lugano",
          "coordinates": {
            "longitude": 10,
            "latitude": 2147483647
          }
        }
      ]
    }
  ]
}
```

If there are several expandable sub resources a client can either name them all or use an Asterix `*` to fetch them all at once. 

`GET /countries/CHE?expands=*` 

### Spring Boot

Not yet supported

### JAX-RS

Not yet supported

## Additional features

### Raw return types (Micronaut only)

By defining a representation of an HTTP verb as `raw` 

```json
"representations": [
    {
      "name": "json",
      "comment": "",
      "responseExample": "{...}",
      "isDefault": true,
      "mimetype": "application/json",
      "raw": true
    }
  ]
```

the plugin generates a controller method with an HttpResponse insted of the specific model. This gives you more freedom to control the http headers.
For example if you would like to answer a POST call with an `HTTP 303 - See other` you might want to set the JSON represention of the POST verb to `raw=true`. 
