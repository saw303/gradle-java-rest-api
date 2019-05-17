package ch.silviowangler.gradle.restapi.validation

import ch.silviowangler.rest.contract.model.v1.ResourceContract
import ch.silviowangler.rest.contract.model.v1.ResourceField
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * @author Silvio Wangler
 */
class MinMaxValuesMatchTypeValidatorSpec extends Specification {

  @Subject
  Validator validator = new MinMaxValuesMatchTypeValidator()

  void "An integer field can have integers values for min/max constraints"() {

    given:
    ResourceContract contract = new ResourceContract(
        fields: [
            new ResourceField(
                name: "a",
                type: "int",
                min: 1,
                max: 2
            )
        ]
    )

    and:
    Set<ConstraintViolation> violations = validator.validate(contract)

    expect:
    violations.isEmpty()
  }

  @Unroll
  void "Min & max values are not supported for field type #type"() {

    given:
    ResourceContract contract = new ResourceContract(
        fields: [
            new ResourceField(
                name: "a",
                type: type,
                min: 1,
                max: 2
            )
        ]
    )

    and:
    Set<ConstraintViolation> violations = validator.validate(contract)

    expect:
    violations.size() == 1

    and:
    violations.first().message == "Min/Max not supported for field 'a' with type '${type}'"

    where:
    type       | _
    'bool'     | _
    'date'     | _
    'datetime' | _
    'flag'     | _
    'uuid'     | _
    'object'   | _
    'locale'   | _
  }

  @Unroll
  void "The min value #min and max value #max of a field type '#type' must be of the same type as the field type"() {

    given:
    ResourceContract contract = new ResourceContract(
        fields: [
            new ResourceField(
                name: "a",
                type: type,
                min: min,
                max: max
            )
        ]
    )

    and:
    Set<ConstraintViolation> violations = validator.validate(contract)

    expect:
    violations.size() == violationCount

    and:
    if (!violations.isEmpty()) {
      violations.first().message == message
    }

    where:
    type     | min  | max  || violationCount | message
    'int'    | 1    | 2    || 0              | ''
    'long'   | 1L   | 2L   || 0              | ''
    'long'   | 1.0  | 2.0  || 1              | ''
    'double' | 1.0  | 2.0  || 0              | ''
    'double' | 1    | 2    || 1              | ''
    'float'  | 1.0F | 2.0F || 0              | ''
    'float'  | 1    | 2    || 1              | ''
  }
}
