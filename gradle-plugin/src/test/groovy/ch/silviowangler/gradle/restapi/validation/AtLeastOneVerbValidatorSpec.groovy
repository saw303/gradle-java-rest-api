package ch.silviowangler.gradle.restapi.validation

import ch.silviowangler.rest.contract.model.v1.ResourceContract
import ch.silviowangler.rest.contract.model.v1.Verb
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Silvio Wangler
 */
class AtLeastOneVerbValidatorSpec extends Specification {

  @Subject
  Validator validator = new AtLeastOneVerbValidator()

  void "The contract must contain at least one verb"() {

    when:
    Set<ValidationViolation> violations = validator.validate(new ResourceContract())

    then:
    violations.size() == 1

    and:
    violations.first().message == 'The resource must contain at least one verb'

    when:
    violations = validator.validate(new ResourceContract(verbs: [new Verb(verb: 'GET')]))

    then:
    violations.isEmpty()
  }
}
