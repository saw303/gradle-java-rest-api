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
import ch.silviowangler.gradle.restapi.GenerateRestApiTask
import ch.silviowangler.gradle.restapi.GeneratorUtil
import ch.silviowangler.gradle.restapi.LinkParser
import ch.silviowangler.gradle.restapi.builder.AbstractRootResourceBuilder
import com.squareup.javapoet.*
import io.github.getify.minify.Minify
import org.gradle.api.Project

import javax.lang.model.element.Modifier
import java.nio.charset.Charset

import static ch.silviowangler.gradle.restapi.AnnotationTypes.*
import static javax.lang.model.element.Modifier.*

class JaxRsRootResourceFactory extends AbstractRootResourceBuilder {

    Project project
    String currentPackageName


    JaxRsRootResourceFactory withProject(Project project) {
        this.project = project
        return this
    }

    @Override
    protected void createOptionsMethod() {

    }

    @Override
    TypeSpec buildRootResource(File optionsFile) {

        withSpecification(optionsFile)

        this.currentPackageName = "${project.restApi.packageName}.${GeneratorUtil.composePackageName(model)}".toString()

        TypeSpec.Builder interfaceBuilder = interfaceBaseInstance()

        LinkParser parser = new LinkParser(model.general.'x-route', model.general.version.split("\\.")[0])


        interfaceBuilder.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', parser.toBasePath()).build())

        // Collection get

        if (containsGetCollection(model)) {

            def verb = fetchVerb(model, GenerateRestApiTask.GET_COLLECTION)

            for (representation in verb.representations) {

                MethodSpec.Builder collectionGetMethod

                if (representation.name == 'json') {
                    collectionGetMethod = MethodSpec.methodBuilder("getCollection")
                } else {
                    collectionGetMethod = MethodSpec.methodBuilder("getCollection${representation.name[0].toUpperCase()}${representation.name.substring(1)}")
                }

                collectionGetMethod.addModifiers(ABSTRACT, PUBLIC).returns(GeneratorUtil.getReturnType(project, optionsFile, 'Get', true, currentPackageName))
                collectionGetMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_GET_VERB.className).build())
                collectionGetMethod.addAnnotation(createProducesAnnotation())
                collectionGetMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', '').build())

                addCachingAnnotation(collectionGetMethod, verb)

                if (representation.name != 'json') {
                    collectionGetMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', '*.' + representation.name).build())
                }

                if (isSecurityEnabled() && verb.permissions?.size() > 0) {
                    collectionGetMethod.addAnnotation(AnnotationSpec.builder(RESTAPI_JWT_ANNOTATION.className).build())
                }

                for (String pathVar in parser.pathVariables) {
                    collectionGetMethod.addParameter(
                            ParameterSpec.builder(String, pathVar).addAnnotation(AnnotationSpec.builder(JAX_RS_PATH_PARAM.className).addMember('value', '$S', pathVar).build()).build()
                    )
                }

                // FilterModel
                collectionGetMethod.addParameter(
                        ParameterSpec.builder(RESTAPI_FILTERMODEL.className, 'filter').addAnnotation(AnnotationSpec.builder(JAX_RS_QUERY_PARAM.className).addMember('value', '$S', 'filter').build()).build()
                )

                // Parameters
                model.verbs.find { it.verb == GenerateRestApiTask.GET_COLLECTION }.parameters.each { p ->
                    collectionGetMethod.addParameter(
                            ParameterSpec.builder(GeneratorUtil.translateToJava(p.type), p.name).addAnnotation(AnnotationSpec.builder(JAX_RS_QUERY_PARAM.className).addMember('value', '$S', p.name).build()).build()
                    )
                }

                interfaceBuilder.addMethod(collectionGetMethod.build())
            }
        } else if (!parser.directEntity) {
            interfaceBuilder.addMethod(buildMethodNotAllowedHandler('getCollection', JAX_RS_GET_VERB.className, '').build())
        }

        // Entity get
        if (containsGetEntity(model)) {

            def verb = fetchVerb(model, GenerateRestApiTask.GET_ENTITY)
            for (representation in verb.representations) {

                String mimetype = representation.name == 'json' ? 'application/json' : representation.mimetype

                MethodSpec.Builder entityGetMethod

                String methodName
                String extension

                if (representation.name == 'json') {
                    methodName = 'getEntity'
                    extension = ''
                } else {
                    methodName = "getEntity${representation.name[0].toUpperCase()}${representation.name.substring(1)}".toString()
                    extension = ".${representation.name}"
                }
                entityGetMethod = MethodSpec.methodBuilder(methodName)

                entityGetMethod.addModifiers(Modifier.ABSTRACT, PUBLIC)

                if (representation.name == 'json') {
                    entityGetMethod.returns(GeneratorUtil.getReturnType(project, optionsFile, 'Get', false, currentPackageName))
                } else {
                    entityGetMethod.returns(AnnotationTypes.JAX_RS_RESPONSE.className)
                }

                entityGetMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_GET_VERB.className).build())
                entityGetMethod.addAnnotation(createProducesAnnotation(mimetype))
                entityGetMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', parser.directEntity ? extension : "{id}${extension}").build())
                if (isSecurityEnabled() && verb.permissions?.size() > 0) {
                    entityGetMethod.addAnnotation(AnnotationSpec.builder(RESTAPI_JWT_ANNOTATION.className).build())
                }

                addCachingAnnotation(entityGetMethod, verb)

                for (String pathVar in parser.pathVariables) {
                    entityGetMethod.addParameter(
                            ParameterSpec.builder(String, pathVar).addAnnotation(AnnotationSpec.builder(JAX_RS_PATH_PARAM.className).addMember('value', '$S', pathVar).build()).build()
                    )
                }

                if (!parser.directEntity) {
                    entityGetMethod.addParameter(
                            ParameterSpec.builder(String, "id").addAnnotation(AnnotationSpec.builder(JAX_RS_PATH_PARAM.className).addMember('value', '$S', "id").build()).build()
                    )
                }

                interfaceBuilder.addMethod(entityGetMethod.build())
            }
        } else {
            interfaceBuilder.addMethod(buildMethodNotAllowedHandler('getEntity', JAX_RS_GET_VERB.className, parser.directEntity ? '' : '{id}').build())
        }


        if (containsPost(model)) {
            // POST mit Resource Model
            MethodSpec.Builder createEntityResourceModel = entityCreateMethod(parser, optionsFile, { builder ->
                builder.addParameter(
                        ParameterSpec.builder(
                                ClassName.get(currentPackageName, GeneratorUtil.createResourceModelName(optionsFile, 'Post')), 'model')
                                .addAnnotation(AnnotationSpec.builder(JAVAX_VALIDATION_VALID.className).build())
                                .build()
                )
                builder.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', '').build())

                def verb = fetchVerb(model, GenerateRestApiTask.POST)
                if (isSecurityEnabled() && verb.permissions?.size() > 0) {
                    builder.addAnnotation(AnnotationSpec.builder(RESTAPI_JWT_ANNOTATION.className).build())
                }
                addCachingAnnotation(builder, verb)
            })

            interfaceBuilder.addMethod(createEntityResourceModel.build())
        } else if (!parser.directEntity) {
            interfaceBuilder.addMethod(buildMethodNotAllowedHandler('createEntity', JAX_RS_POST_VERB.className, parser.directEntity ? '' : '{id}').build())
        }

        if (containsPut(model)) {

            // PUT mit Resource Model
            MethodSpec.Builder updateEntityResourceModel = entityUpdateMethod(parser, optionsFile, { builder ->
                builder.addParameter(
                        ParameterSpec.builder(ClassName.get(currentPackageName, GeneratorUtil.createResourceModelName(optionsFile, 'Put')), 'model')
                                .addAnnotation(AnnotationSpec.builder(JAVAX_VALIDATION_VALID.className).build())
                                .build()
                )

                if (!parser.directEntity) {
                    builder.addParameter(
                            ParameterSpec.builder(String, "id").addAnnotation(AnnotationSpec.builder(JAX_RS_PATH_PARAM.className).addMember('value', '$S', "id").build()).build()
                    )
                }
                builder.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', parser.directEntity ? '' : '{id}').build())
                def verb = fetchVerb(model, GenerateRestApiTask.PUT)
                if (isSecurityEnabled() && verb.permissions?.size() > 0) {
                    builder.addAnnotation(AnnotationSpec.builder(RESTAPI_JWT_ANNOTATION.className).build())
                }
                addCachingAnnotation(builder, verb)
            })

            interfaceBuilder.addMethod(updateEntityResourceModel.build())
        } else {
            interfaceBuilder.addMethod(buildMethodNotAllowedHandler('updateEntity', JAX_RS_PUT_VERB.className, parser.directEntity ? '' : '{id}').build())
        }

        // DELETE
        if (containsDeleteEntity(model)) {
            MethodSpec.Builder entityDeleteMethod = MethodSpec.methodBuilder('deleteEntity').addModifiers(Modifier.ABSTRACT, PUBLIC).returns(GeneratorUtil.getReturnType(project, optionsFile, 'Delete', false, currentPackageName))
            entityDeleteMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_DELETE_VERB.className).build())
            entityDeleteMethod.addAnnotation(createProducesAnnotation())
            entityDeleteMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', parser.directEntity ? '' : '{id}').build())

            for (String pathVar in parser.pathVariables) {
                entityDeleteMethod.addParameter(
                        ParameterSpec.builder(String, pathVar).addAnnotation(AnnotationSpec.builder(JAX_RS_PATH_PARAM.className).addMember('value', '$S', pathVar).build()).build()
                )
            }

            if (!parser.directEntity) {
                entityDeleteMethod.addParameter(
                        ParameterSpec.builder(String, "id").addAnnotation(AnnotationSpec.builder(JAX_RS_PATH_PARAM.className).addMember('value', '$S', "id").build()).build()
                )
            }

            def verb = fetchVerb(model, GenerateRestApiTask.DELETE_ENTITY)
            if (isSecurityEnabled() && verb.permissions?.size() > 0) {
                entityDeleteMethod.addAnnotation(AnnotationSpec.builder(RESTAPI_JWT_ANNOTATION.className).build())
            }
            addCachingAnnotation(entityDeleteMethod, verb)

            interfaceBuilder.addMethod(entityDeleteMethod.build())
        } else {
            interfaceBuilder.addMethod(buildMethodNotAllowedHandler('deleteEntity', JAX_RS_DELETE_VERB.className, parser.directEntity ? '' : '{id}').build())
        }

        // Delete collection
        if (!parser.directEntity) {
            interfaceBuilder.addMethod(buildMethodNotAllowedHandler('deleteCollection', JAX_RS_DELETE_VERB.className, '').build())
        }

        // Options call (Java 8)

        FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.bestGuess('String'), "OPTIONS_CONTENT").addModifiers(PUBLIC, STATIC, FINAL)
                .initializer('$N', "\"${Minify.minify(optionsFile.text).replaceAll('"', '\\\\"')}\"")

        interfaceBuilder.addField(fieldBuilder.build())

        MethodSpec.Builder optionsMethod = MethodSpec.methodBuilder('getOptions').addModifiers(PUBLIC, DEFAULT).returns(JAX_RS_RESPONSE.className)

        optionsMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_OPTIONS_VERB.className).build())
        optionsMethod.addAnnotation(createProducesAnnotation())
        optionsMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', '').build())

        def annotationBuilder = AnnotationSpec.builder(RESTAPI_CACHING_ANNOTATION.className)
        annotationBuilder.addMember('noCache', '$L', false)
        annotationBuilder.addMember('isPrivate', '$L', true)
        annotationBuilder.addMember('maxAge', '$L', 86_400L)
        annotationBuilder.addMember('expires', '$L', 86_400L)
        annotationBuilder.addMember('eTag', '$L', false)
        optionsMethod.addAnnotation(annotationBuilder.build())

        optionsMethod.addParameter(
                ParameterSpec.builder(ClassName.get('javax.ws.rs.core', 'UriInfo'), 'uriInfo').addAnnotation(AnnotationSpec.builder(JAX_RS_CONTEXT.className).build()).build()
        ).addParameter(
                ParameterSpec.builder(ClassName.get('javax.servlet.http', 'HttpServletRequest'), 'request').addAnnotation(AnnotationSpec.builder(JAX_RS_CONTEXT.className).build()).build()
        )

        optionsMethod.addStatement("return Response.ok(OPTIONS_CONTENT).build()")
        interfaceBuilder.addMethod(optionsMethod.build())

        return interfaceBuilder.build()
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

    private MethodSpec.Builder buildMethodNotAllowedHandler(String methodName, ClassName verb, String pathValue) {
        MethodSpec.Builder entityDeleteMethod = MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC, DEFAULT).returns(JAX_RS_RESPONSE.className)

        entityDeleteMethod.addAnnotation(AnnotationSpec.builder(verb).build())
        entityDeleteMethod.addAnnotation(createProducesAnnotation())
        entityDeleteMethod.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', pathValue).build())

        entityDeleteMethod.addStatement("return Response.status(405).build()")
        entityDeleteMethod
    }

    private MethodSpec.Builder entityCreateMethod(LinkParser parser, File optionsFile, Closure closure) {
        createModifyingMethod(parser, JAX_RS_POST_VERB.className, optionsFile, closure)
    }

    private MethodSpec.Builder createModifyingMethod(LinkParser parser, ClassName httpVerb, File optionsFile, Closure closure) {

        String methodName = httpVerb.simpleName() == GenerateRestApiTask.POST ? 'createEntity' : 'updateEntity'
        String verb = httpVerb.simpleName() == GenerateRestApiTask.POST ? 'Post' : 'Put'

        MethodSpec.Builder createEntityMethodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.ABSTRACT, PUBLIC).returns(GeneratorUtil.getReturnType(project, optionsFile, verb, currentPackageName))
        createEntityMethodBuilder.addAnnotation(AnnotationSpec.builder(httpVerb).build())
        createEntityMethodBuilder.addAnnotation(createProducesAnnotation())

        for (String pathVar in parser.pathVariables) {
            createEntityMethodBuilder.addParameter(
                    ParameterSpec.builder(String, pathVar).addAnnotation(AnnotationSpec.builder(JAX_RS_PATH_PARAM.className).addMember('value', '$S', pathVar).build()).build()
            )
        }

        closure.call(createEntityMethodBuilder)
        createEntityMethodBuilder
    }

    private MethodSpec.Builder entityUpdateMethod(LinkParser parser, File optionsFile, Closure closure) {
        createModifyingMethod(parser, JAX_RS_PUT_VERB.className, optionsFile, closure)
    }

    @Override
    AnnotationSpec getQueryParamAnnotation(String paramName) {
        throw new RuntimeException("Not available")
    }

    @Override
    Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId) {
        throw new RuntimeException("Not available")
    }
}
