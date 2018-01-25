/*
 * MIT License
 *
 * Copyright (c) 2016 - 2018 Silvio Wangler (silvio.wangler@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ch.silviowangler.gradle.restapi.builder

import ch.silviowangler.gradle.restapi.AnnotationTypes
import ch.silviowangler.gradle.restapi.GenerateRestApiTask
import ch.silviowangler.rest.contract.model.v1.ResourceContract
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
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

        @Override
        AnnotationSpec getQueryParamAnnotation(String paramName) {
            throw new RuntimeException("Not available")
        }

        @Override
        Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId) {
            throw new RuntimeException("Not available")
        }

        @Override
        AnnotationTypes getPathVariableAnnotationType() {
            throw new RuntimeException("Not available")
        }

        @Override
        void generateMethodNotAllowedStatement(MethodSpec.Builder builder) {
            throw new RuntimeException("Not available")
        }

        @Override
        ClassName getMethodNowAllowedReturnType() {
            throw new RuntimeException("Not available")
        }

        @Override
        protected void createOptionsMethod() {
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
