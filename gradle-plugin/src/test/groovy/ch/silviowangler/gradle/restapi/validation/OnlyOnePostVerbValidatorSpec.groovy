package ch.silviowangler.gradle.restapi.validation

import ch.silviowangler.gradle.restapi.builder.ResourceBuilder
import ch.silviowangler.rest.contract.model.v1.ResourceContract
import ch.silviowangler.rest.contract.model.v1.Verb
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static ch.silviowangler.gradle.restapi.builder.ResourceBuilder.POST
import static ch.silviowangler.gradle.restapi.builder.ResourceBuilder.POST_COLLECTION
import static ch.silviowangler.gradle.restapi.builder.ResourceBuilder.POST_ENTITY

/**
 * @author Silvio Wangler
 */
class OnlyOnePostVerbValidatorSpec extends Specification {

  @Subject
  OnlyOnePostVerbValidator validator = new OnlyOnePostVerbValidator()

  void "No verbs, no stress"() {

    given:
    ResourceContract contract = new ResourceContract()

    when:
    Set<ConstraintViolation> violations = validator.validate(contract)

    then:
    violations.isEmpty()
  }

  @Unroll
  void "The verb #verb by itself is allowed"() {

    given:
    ResourceContract contract = new ResourceContract(
        verbs: [new Verb(verb: verb)]
    )

    when:
    Set<ConstraintViolation> violations = validator.validate(contract)

    then:
    violations.isEmpty()

    where:
    verb << ResourceBuilder.POST_METHODS
  }

  def "Name"() {
  }

  @Unroll
  void "This combination #comb is not allowed"() {

    given:
    ResourceContract contract = new ResourceContract(
        verbs: comb.collect { new Verb(verb: it) }
    )

    when:
    Set<ConstraintViolation> violations = validator.validate(contract)

    then:
    violations.size() == 1

    and:
    violations.first().message == "Choose either POST, POST_ENTITY or POST_COLLECTION. Only one of them is allowed since they map all to the same URI path. You chose ${comb.join(", ")}"

    where:
    comb                           | _
    [POST, POST_ENTITY]            | _
    [POST, POST_COLLECTION]        | _
    [POST_ENTITY, POST_COLLECTION] | _
  }
}
