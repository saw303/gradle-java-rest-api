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

import ch.silviowangler.gradle.restapi.AnnotationTypes
import ch.silviowangler.gradle.restapi.builder.AbstractRootResourceBuilder
import ch.silviowangler.gradle.restapi.builder.ArtifactType
import ch.silviowangler.rest.contract.model.v1.Verb
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.Project

import java.nio.charset.Charset

import static ch.silviowangler.gradle.restapi.AnnotationTypes.*

class JaxRsRootResourceFactory extends AbstractRootResourceBuilder {

    Project project

    JaxRsRootResourceFactory withProject(Project project) {
        this.project = project
        return this
    }

    @Override
    Project getProject() {
        this.project
    }

    @Override
    protected void createOptionsMethod() {
        Verb verb = new Verb()
        verb.setVerb("OPTIONS")

        setCurrentVerb(verb)
        MethodSpec.Builder optionsMethod = createMethod("getOptions", JAX_RS_RESPONSE.className)

        optionsMethod.addAnnotation(
                createAnnotation(JAX_RS_OPTIONS_VERB)
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
        return null
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

    private boolean isSecurityEnabled() {
        return project.restApi.enableSecurity
    }

    private void addCachingAnnotation(MethodSpec.Builder builder, def jsonObject) {

        if (jsonObject.caching) {

            def annotationBuilder = AnnotationSpec.builder(RESTAPI_CACHING_ANNOTATION.className)
            if (jsonObject.caching.'no-cache' != null) {
                annotationBuilder.addMember('noCache', '$L', jsonObject.caching.'no-cache' as Boolean)
            }

            if (jsonObject.caching.private != null) {
                annotationBuilder.addMember('isPrivate', '$L', jsonObject.caching.private as Boolean)
            }

            if (jsonObject.caching.'max-age' != null) {
                annotationBuilder.addMember('maxAge', '$L', jsonObject.caching.'max-age' as Long)
            }

            if (jsonObject.caching.Expires != null) {
                annotationBuilder.addMember('expires', '$L', jsonObject.caching.Expires as Long)
            }

            if (jsonObject.caching.ETag != null) {
                annotationBuilder.addMember('eTag', '$L', jsonObject.caching.ETag as Boolean)
            }
            builder.addAnnotation(annotationBuilder.build())
        }
    }

    @Override
    AnnotationSpec getQueryParamAnnotation(String paramName) {
        return createAnnotation(JAX_RS_QUERY_PARAM, ['value': paramName])
    }

    @Override
    Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId) {

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
    protected AnnotationTypes getPathVariableAnnotationType() {
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
}
