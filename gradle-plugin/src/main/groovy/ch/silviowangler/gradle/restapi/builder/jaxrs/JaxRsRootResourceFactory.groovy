/*
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2019 Silvio Wangler (silvio.wangler@gmail.com)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ch.silviowangler.gradle.restapi.builder.jaxrs

import ch.silviowangler.gradle.restapi.GeneratorUtil
import ch.silviowangler.gradle.restapi.PluginTypes
import ch.silviowangler.gradle.restapi.builder.AbstractResourceBuilder
import ch.silviowangler.gradle.restapi.builder.ArtifactType
import ch.silviowangler.rest.contract.model.v1.Representation
import ch.silviowangler.rest.contract.model.v1.Verb
import ch.silviowangler.rest.contract.model.v1.VerbParameter
import com.squareup.javapoet.*

import java.nio.charset.Charset

import static ch.silviowangler.gradle.restapi.PluginTypes.*

class JaxRsRootResourceFactory extends AbstractResourceBuilder {

	private final boolean explicitExtensions

	JaxRsRootResourceFactory(boolean explicitExtensions) {
		this.explicitExtensions = explicitExtensions
	}

    @Override
    boolean supportsInterfaces() {
        return true
    }

    @Override
    protected void createOptionsMethod() {
        Verb verb = new Verb()
        verb.setVerb("OPTIONS")

        setCurrentVerb(verb)
        MethodSpec.Builder optionsMethod = createMethod("getOptions", JAX_RS_RESPONSE.typeName)

        optionsMethod.addAnnotation(
                createAnnotation(JAX_RS_OPTIONS_VERB)
        )
        optionsMethod.addAnnotation(
                createAnnotation(JAX_RS_PATH, [value: ''])
        )
        optionsMethod.addStatement('return $T.ok(OPTIONS_CONTENT).build()', JAX_RS_RESPONSE.typeName)

        resourceBaseTypeBuilder().addMethod(optionsMethod.build())
        setCurrentVerb(null)
    }

    @Override
    AnnotationSpec buildRequestBodyAnnotation() {
        throw new UnsupportedOperationException("Not supported for JAX-RS")
    }

    @Override
    TypeSpec buildResource() {

        reset()
        setArtifactType(ArtifactType.RESOURCE)
        TypeSpec.Builder builder = resourceBaseTypeBuilder()

        Map<String, Object> args = ['value': getPath()]

        builder.addAnnotation(createAnnotation(JAX_RS_PATH, args))

        generateResourceMethods()

        return builder.build()
    }

    @Override
    TypeSpec buildResourceImpl() {
        reset()
        setArtifactType(ArtifactType.RESOURCE_IMPL)
        TypeSpec.Builder builder = classBaseInstance()
        builder.addSuperinterface(ClassName.get(getCurrentPackageName(), resourceName()))

        generateResourceMethods()
        return builder.build()
    }

    private AnnotationSpec createProducesAnnotation(Representation representation) {

        Charset charset = getResponseEncoding()
        String mimetype = Objects.requireNonNull(representation.getMimetype(), "mime type must not be null")

        if (charset && representation.isJson()) {
            mimetype = "application/json; charset=${charset.name()}"
        } else if (representation.isJson()) {
            mimetype = "application/json"
        } else if (charset) {
            mimetype = "${mimetype}; charset=${charset.name()}"
        }
        AnnotationSpec.builder(JAX_RS_PRODUCES.typeName).addMember('value', '{ $S }', mimetype).build()
    }


    @Override
    AnnotationSpec getQueryParamAnnotation(VerbParameter paramName) {
        // TODO handle VerbParameter options like required
        return createAnnotation(JAX_RS_QUERY_PARAM, ['value': paramName.getName()])
    }

    @Override
    Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId, Representation representation, String methodName) {

        List<AnnotationSpec> specs = []

        String method = getHttpMethod()

        if (method == 'GET') {
            specs << createAnnotation(JAX_RS_GET_VERB)
        } else if (method == 'POST') {
            specs << createAnnotation(JAX_RS_POST_VERB)
        } else if (method == 'PUT') {
            specs << createAnnotation(JAX_RS_PUT_VERB)
        } else if (method == 'DELETE') {
            specs << createAnnotation(JAX_RS_DELETE_VERB)
        }

        specs << createProducesAnnotation(representation)

        if (applyId) {
            specs << createAnnotation(JAX_RS_PATH, ['value': "{id}${representation.isJson() && !explicitExtensions ? '' : ".${representation.name}"}"])
        }

        return specs
    }

    @Override
    PluginTypes getPathVariableAnnotationType() {
        return JAX_RS_PATH_PARAM
    }

    @Override
    void generateMethodNotAllowedStatement(MethodSpec.Builder builder) {
        builder.addStatement('return $T.status(405).build()', JAX_RS_RESPONSE.typeName)
    }

    @Override
    ClassName getMethodNowAllowedReturnType() {
        return JAX_RS_RESPONSE.className
    }

    @Override
    TypeName resourceMethodReturnType(Verb verb, Representation representation) {
        String v = toHttpMethod(verb)
        return GeneratorUtil.getJaxRsReturnType(getResourceContractContainer().getSourceFileName(), v, verb.getVerb().endsWith("_COLLECTION"), getCurrentPackageName(), representation)
    }
}
