package ch.silviowangler.rest.validation

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import javax.validation.ConstraintValidatorContext

/**
 * @author Silvio Wangler
 */
class PhoneNumberValidatorSpec extends Specification {

  @Subject
  PhoneNumberValidator validator = new PhoneNumberValidator()

  ConstraintValidatorContext context = Mock()

  @Unroll
  void "Phone number #phone is #text"() {

    given:
    PhoneNumber constraint = Mock()

    and:
    constraint.country() >> 'CH'

    and:
    validator.initialize(constraint)

    expect:
    validator.isValid(phone, context) == result

    where:
    phone              | result | text
    '044 444 44 44'    | true   | 'valid'
    '043 444 44 44'    | true   | 'valid'
    '1 444 44 44'      | false  | 'not valid'
    '+41 44 444 44 44' | true   | 'valid'
    '+49 89 7654321'   | true   | 'valid'
    '+49 172 7654321'  | true   | 'valid'
  }

  void "Null values are acceptable"() {

    expect:
    validator.isValid(null, context)
  }
}
