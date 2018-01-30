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
package ch.silviowangler.gradle.restapi.builder.jaxrs

import ch.silviowangler.gradle.restapi.GeneratorUtil
import ch.silviowangler.gradle.restapi.PluginTypes
import ch.silviowangler.gradle.restapi.builder.AbstractResourceBuilder
import ch.silviowangler.gradle.restapi.builder.ArtifactType
import ch.silviowangler.rest.contract.model.v1.Representation
import ch.silviowangler.rest.contract.model.v1.Verb
import com.squareup.javapoet.*

import java.nio.charset.Charset

import static ch.silviowangler.gradle.restapi.PluginTypes.*

class JaxRsRootResourceFactory extends AbstractResourceBuilder {


    @Override
    protected void createOptionsMethod() {
        Verb verb = new Verb()
        verb.setVerb("OPTIONS")

        setCurrentVerb(verb)
        MethodSpec.Builder optionsMethod = createMethod("getOptions", JAX_RS_RESPONSE.className)

        optionsMethod.addAnnotation(
                createAnnotation(JAX_RS_OPTIONS_VERB)
        )
        optionsMethod.addAnnotation(
                createAnnotation(JAX_RS_PATH, [value: ''])
        )
        optionsMethod.addStatement('return $T.ok(OPTIONS_CONTENT).build()', JAX_RS_RESPONSE.className)

        interfaceBaseInstance().addMethod(optionsMethod.build())
        setCurrentVerb(null)
    }

    @Override
    TypeSpec buildRootResource() {

        reset()
        setArtifactType(ArtifactType.RESOURCE)
        TypeSpec.Builder builder = interfaceBaseInstance()

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

    private AnnotationSpec createProducesAnnotation() {
        createProducesAnnotation('application/json')
    }

    private AnnotationSpec createProducesAnnotation(String mimetype) {

        Charset charset = project.restApi.responseEncoding

        if (charset && mimetype == 'application/json') {
            mimetype = "${mimetype}; charset=${charset.name()}"
        }
        AnnotationSpec.builder(JAX_RS_PRODUCES.className).addMember('value', '{ $S }', mimetype).build()
    }


    @Override
    AnnotationSpec getQueryParamAnnotation(String paramName) {
        return createAnnotation(JAX_RS_QUERY_PARAM, ['value': paramName])
    }

    @Override
    Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId, Representation representation) {

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

        specs << AnnotationSpec.builder(JAX_RS_PRODUCES.className)
                .addMember('value', '{ $S }', 'application/json')
                .build()

        if (applyId) {
            specs << createAnnotation(JAX_RS_PATH, ['value': '{id}'])
        }

        return specs
    }

    @Override
    PluginTypes getPathVariableAnnotationType() {
        return JAX_RS_PATH_PARAM
    }

    @Override
    void generateMethodNotAllowedStatement(MethodSpec.Builder builder) {
        builder.addStatement('return $T.status(405).build()', JAX_RS_RESPONSE.className)
    }

    @Override
    ClassName getMethodNowAllowedReturnType() {
        return JAX_RS_RESPONSE.className
    }

    @Override
    TypeName resourceMethodReturnType(Verb verb, Representation representation) {
        String v = toHttpMethod(verb)
        return GeneratorUtil.getJaxRsReturnType(getResourceContractContainer().getSourceFileName(), v, false, getCurrentPackageName(), representation)
    }
}
