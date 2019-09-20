package ch.silviowangler.gradle.restapi.validation

import ch.silviowangler.rest.contract.model.v1.ResourceContract
import ch.silviowangler.rest.contract.model.v1.ResourceField
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Silvio Wangler
 */
class MandatoryFieldValidatorSpec extends Specification {

  @Subject
  MandatoryFieldValidator validator = new MandatoryFieldValidator()

  void "Returns an empty set when the resource contract is in its default initial state"() {

    when:
    Set<ConstraintViolation> violations = validator.validate(new ResourceContract())

    then:
    violations.isEmpty()
  }

  void "Validation error when field is mandatory on a verb that the resource does not obtain"() {

    given:
    ResourceContract contract = new ResourceContract(
        fields: [
            new ResourceField(name: 'street', mandatory: ['POST'])
        ]
    )
    when:
    Set<ConstraintViolation> violations = validator.validate(contract)

    then:
    violations.size() == 1

    and:
    violations.first().message == "Field 'street' cannot be mandatory on non existing verb 'POST'"
  }
}
