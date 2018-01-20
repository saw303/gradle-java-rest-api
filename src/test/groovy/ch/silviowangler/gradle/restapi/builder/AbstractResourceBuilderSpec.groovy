package ch.silviowangler.gradle.restapi.builder

import ch.silviowangler.gradle.restapi.GenerateRestApiTask
import ch.silviowangler.rest.contract.model.v1.ResourceContract
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Silvio Wangler
 */
class AbstractResourceBuilderSpec extends Specification {

    @Subject
    ResourceBuilder resourceBuilder = new DummyResourceBuilder()


    private static class DummyResourceBuilder extends AbstractResourceBuilder {

        @Override
        Project getProject() {
            throw new RuntimeException("Not available")
        }
    }

    void "A resource file must not be null nor must not exist"() {

        when:
        resourceBuilder.withSpecification(null)

        then:
        thrown(NullPointerException)

        and:

        File file = new File("hasdkjfhdjhfkajhfdjkad")
        when:
        resourceBuilder.withSpecification(file)

        then:
        IllegalArgumentException ex = thrown()

        and:
        ex.message == "File ${file.absolutePath} does not exist"
    }

    void "A resource contract is converted into our Java model"() {

        given:
        URL url = getClass().getResource('/specs/rootSpringBoot/root.v1.json')

        and:
        resourceBuilder.withSpecification(new File(url.toURI()))

        and:
        ResourceContract resourceContract = resourceBuilder.getModel()

        expect:
        resourceContract

        and:
        resourceContract.general.description == 'This is the root resource of API version 1.0.0'
        resourceContract.general.version == '1.0.0'
        resourceContract.general.xRoute == '/api/:version'


        and:
        resourceContract.verbs.size() == 1

        and:
        resourceContract.verbs[0].verb == GenerateRestApiTask.GET_ENTITY
        resourceContract.verbs[0].rel == 'entity'

        and:
        resourceContract.verbs[0].representations.size() == 1

        and:
        resourceContract.verbs[0].responseStates.size() == 2

        and:
        resourceContract.fields.size() == 2

        and:
        resourceContract.fields[0].name == 'id'
        resourceContract.fields[0].type == 'uuid'

        and:
        resourceContract.fields[1].name == 'name'
        resourceContract.fields[1].type == 'string'
    }
}
