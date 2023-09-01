package ch.silviowangler.rest.validation


import ch.silviowangler.rest.contract.model.v1.ResourceContract
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class ResourceContractSerdeSpec extends Specification {

  @Inject
  ObjectMapper objectMapper


  void "Deserialize JSON contract"() {

    given:
    final String json = '''
{
  "general": {
    "name": "project",
    "description": "The project resource",
    "version": "1.0.0",
    "x-route": "/api/v1/organisations/:organisation/projects/:entity",
    "lifecycle": {
      "deprecated": false,
      "info": "This version is still valid"
    }
  },
  "verbs": [
    {
      "verb": "GET_COLLECTION",
      "rel": "collection",
      "responseStates": [
        {
          "code": 200,
          "message": "200 Ok",
          "comment": "content in response body"
        },
        {
          "code": 503,
          "message": "503 Service Unavailable",
          "comment": "Backend server not reachable or too slow"
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
      "headers": []
    }
  ],
  "fields": [
    {
      "name": "id",
      "type": "uuid",
      "options": null,
      "mandatory": [],
      "min": null,
      "max": null,
      "multiple": false,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "the id of the project"
    },
    {
      "name": "name",
      "type": "string",
      "options": null,
      "mandatory": [],
      "min": 1,
      "max": 255,
      "multiple": false,
      "protected": [],
      "visible": true,
      "sortable": false,
      "readonly": false,
      "filterable": false,
      "alias": [],
      "x-comment": "the name of the project"
    }
  ],
  "subresources": [
    {
      "name": "timesheetprojects",
      "type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "rel": "subresource",
      "href": "/api/v1/organisations/{:organisation}/projects/reports/timesheet",
      "method": "GET",
      "expandable": false
    }
  ],
  "pipes": [],
  "types": [],
  "validators": []
}

'''

    when:
    ResourceContract contract = this.objectMapper.readValue(json, ResourceContract.class);

    then:
    noExceptionThrown()

    and:
    contract.general.name == 'project'

    and:
    !contract.fields.isEmpty()
  }
}
