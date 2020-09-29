package ch.silviowangler.gradle.restapi.validation


import ch.silviowangler.rest.contract.model.v1.ResourceContract
import ch.silviowangler.rest.contract.model.v1.ResourceField
import ch.silviowangler.rest.contract.model.v1.ResourceTypes
import spock.lang.Specification
import spock.lang.Subject

import static ch.silviowangler.gradle.restapi.builder.ResourceBuilder.JavaTypeRegistry.supportedTypeNames

/**
 * @author Silvio Wangler
 */
class FieldTypeIsSupportedValidatorSpec extends Specification {

  @Subject
  Validator validator = new FieldTypeIsSupportedValidator()

  void "No fields result in no violations"() {

    when:
    Set<ConstraintViolation> violations = validator.validate(new ResourceContract())

    then:
    violations.isEmpty()
  }

  void "All supported field types are accepted"() {

    given:
    List<ResourceField> fields = getSupportedTypeNames().collect { typeName -> new ResourceField(name: UUID.randomUUID().toString(), type: typeName) }

    when:
    Set<ConstraintViolation> violations = validator.validate(new ResourceContract(fields: fields))

    then:
    violations.isEmpty()
  }

  void "Unknown field type causes a constraint violation"() {

    when:
    Set<ConstraintViolation> violations = validator.validate(new ResourceContract(fields: [new ResourceField(name: 'YOLO', type: 'blabla')]))

    then:
    violations.size() == 1

    and:
    violations.first().message == "Field 'YOLO' declares an unsupported data type 'blabla'"
  }

  void "Type validation respects custom types"() {

    when:
    Set<ConstraintViolation> violations = validator.validate(
        new ResourceContract(
            fields: [new ResourceField(name: 'YOLO', type: 'blabla')],
            types: [new ResourceTypes(name: 'blabla')]
        )
    )

    then:
    violations.isEmpty()
  }

  void "Type validation respects custom types from previous contract"() {

    when:
    Set<ConstraintViolation> violations = validator.validate(
        new ResourceContract(
            fields: [new ResourceField(name: 'YOLO', type: 'blabla')],
        ),
        [new ResourceTypes(name: 'blabla')]
    )

    then:
    violations.isEmpty()
  }

  void "Ignore enum types"() {

    when:
    Set<ConstraintViolation> violations = validator.validate(
        new ResourceContract(
            fields: [new ResourceField(name: 'YOLO', type: 'enum')]
        )
    )

    then:
    violations.isEmpty()
  }
}
