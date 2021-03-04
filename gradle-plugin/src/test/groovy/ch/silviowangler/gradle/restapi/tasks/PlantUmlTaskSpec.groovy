package ch.silviowangler.gradle.restapi.tasks

import ch.silviowangler.gradle.restapi.builder.ResourceContractContainer
import ch.silviowangler.rest.contract.model.v1.GeneralDetails
import ch.silviowangler.rest.contract.model.v1.ResourceContract
import ch.silviowangler.rest.contract.model.v1.SubResource
import spock.lang.Specification
import spock.lang.Unroll

class PlantUmlTaskSpec extends Specification {

  static String resourceName = "resource"

  @Unroll
  void "The common prefix of '#a' and '#b' is '#prefix'"() {

    expect:
    PlantUmlTask.commonPrefix(a, b) == prefix

    where:
    a            | b                 || prefix
    ""           | ""                || ""
    null         | ""                || ""
    null         | null              || ""
    "HEy"        | "Hey"             || "H"
    "Ciao Bella" | "Ciao Bella Ciao" || "Ciao Bella"
  }

  @Unroll
  void "find subresource with same name"() {

    given:
    SubResource subResource = new SubResource(name: resourceName, href: subresource)

    when:
    ResourceContractContainer foundResource = PlantUmlTask.findSubResourceContract(resources, subResource)

    then:
    foundResource.resourceContract.general.description == expectedResource as String

    where:
    resources                                                              | subresource                    || expectedResource
    [c(1, "/v1/some/name/here"), c(2, "/v1/some/other/name")]              | "/v1/some/name"                || 1
    [c(1, "/v1/:pin/name/:name/text"), c(2, "/v1/:pin/name/:name/number")] | "/v1/{:pin}/name/{:NAME}/text" || 1
  }

  static ResourceContractContainer c(int id, String route) {
    new ResourceContractContainer(
        new ResourceContract(general: new GeneralDetails(
            name: resourceName,
            xRoute: route,
            description: id)),
        null,
        null)
  }
}
