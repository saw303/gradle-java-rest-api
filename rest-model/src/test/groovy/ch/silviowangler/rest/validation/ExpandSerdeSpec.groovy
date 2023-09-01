package ch.silviowangler.rest.validation

import ch.silviowangler.rest.model.Expand
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate

@MicronautTest
class ExpandSerdeSpec extends Specification {

  @Inject
  ObjectMapper objectMapper

  @Shared
  final Expand expand = new Expand("aaa", [new ActivitiesGetResourceModel(id: 'hh', key: 'hhh', number: 12, dob: LocalDate.of(1978, 11, 1))])

  void "Serialize Expand"() {

    expect:
    objectMapper.writeValueAsString(expand) == '{"name":"aaa","data":[{"@class":"ch.silviowangler.rest.validation.ActivitiesGetResourceModel","key":"hhh","id":"hh","number":12,"dob":"1978-11-01"}]}'
  }


  void "Deserialize Expand"() {

    given:
    final String json = objectMapper.writeValueAsString(expand)

    when:
    Expand result = objectMapper.readValue(json, Expand)

    then:
    result.name == expand.name

    and:
    result.data.size() == 1

    and:
    with(result.data.first()) { ActivitiesGetResourceModel res ->
      res.id == 'hh'
      res.key == 'hhh'
      res.number == 12
      res.dob == LocalDate.of(1978, 11, 1)
    }
  }
}
