package ch.silviowangler.gradle.restapi.tasks

import spock.lang.Specification
import spock.lang.Unroll

class PlantUmlTaskSpec extends Specification {
  @Unroll
  void "CommonPrefix"() {

    expect:
    PlantUmlTask.commonPrefix(a, b) == prefix

    where:
    a            | b                || prefix
    ""           | ""               || ""
    null         | ""               || ""
    null         | null             || ""
    "HEy"        | "Hey"            || "H"
    "Ciao Bella" | "Ciao Bella Ciao" | "Ciao Bella"

  }


  void "rest regex"() {
    given:
    String pattern = ":[^/]+"
    when:
    String replacement = "/v1/applications/:pin/insurablePersons/:person/diagnosis/:entity".replaceAll(pattern, "TOKEN")

    then:
    replacement == "/v1/applications/TOKEN/insurablePersons/TOKEN/diagnosis/TOKEN"


  }
}
