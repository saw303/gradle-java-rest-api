/*
 * MIT License
 *
 * Copyright (c) 2017 - 2017 Silvio Wangler (silvio.wangler@gmail.com)
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
package ch.silviowangler.gradle.restapi

import ch.silviowangler.gradle.restapi.util.SupportedDataTypes
import com.squareup.javapoet.*
import groovy.io.FileType
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import io.github.getify.minify.Minify
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.lang.model.element.Modifier
import java.nio.charset.Charset

import static AnnotationTypes.*
import static com.squareup.javapoet.TypeName.BOOLEAN
import static com.squareup.javapoet.TypeName.INT
import static javax.lang.model.element.Modifier.*

class GenerateRestApiTask extends AbstractTask {


    public static final String GET_COLLECTION = 'GET_COLLECTION'
    public static final String GET_ENTITY = 'GET_ENTITY'
    public static final String POST = 'POST'
    public static final String PUT = 'PUT'
    public static final String DELETE_ENTITY = 'DELETE_ENTITY'
    public static final String DELETE_COLLECTION = 'DELETE_COLLECTION'

    String currentPackageName

    @InputDirectory
    File getOptionsSource() {
        if (project.restApi.optionsSource) {
            return project.restApi.optionsSource
        }
        return new File(GeneratorUtil.generatorInput(project), "spec")
    }


    @OutputDirectory
    File getRootOutputDir() {
        project.restApi.generatorOutput
    }

    @TaskAction
    void exec() {

        logger.lifecycle "Generating REST artifacts ..."
        long start = System.currentTimeMillis()
        int amountOfGeneratedJavaSourceFiles = 0
        final String fileSeparator = '/'

        def jsonSlurper = new JsonSlurper(type: JsonParserType.INDEX_OVERLAY) // use JsonFastParser

        RestApiExtension restApiExtension = project.restApi

        final String basePackageName = restApiExtension.packageName

        List<File> rootOptionsFile = []
        getOptionsSource().eachFile(FileType.FILES, { f -> if (f.name ==~ /root\..*\.json/) rootOptionsFile << f })

        logger.lifecycle("Found ${rootOptionsFile.size()} root option files")


        for (File rootFile in rootOptionsFile) {
            def jsonObject = jsonSlurper.parse rootFile, 'UTF-8'

            currentPackageName = "${basePackageName}.${GeneratorUtil.composePackageName(jsonObject)}".toString()

            for (type in jsonObject.types) {
                logger.lifecycle("Generating type '${type.name}'")
                TypeSpec typeSpec = buildType(type)

                GeneratorUtil.addClassName(type.name, ClassName.get(currentPackageName, GeneratorUtil.createClassname("${type.name}Type")))

                writeToFileSystem(currentPackageName, typeSpec, getRootOutputDir())
                amountOfGeneratedJavaSourceFiles++
            }
            TypeSpec resourceInterface = buildResource(rootFile, jsonObject)
            writeToFileSystem(currentPackageName, resourceInterface, getRootOutputDir())
            amountOfGeneratedJavaSourceFiles++

            def file = new File(restApiExtension.generatorImplOutput, "${currentPackageName.replaceAll('\\.', fileSeparator)}${fileSeparator}${GeneratorUtil.createResourceImplementationName(rootFile)}.java")

            if (!file.exists()) {
                TypeSpec resourceImpl = buildResourceImpl(rootFile, jsonObject)
                writeToFileSystem(currentPackageName, resourceImpl, restApiExtension.generatorImplOutput)
                amountOfGeneratedJavaSourceFiles++
            }

            def representations = jsonObject.verbs.findAll { v ->
                v.representations.find {
                    it.name == 'json'
                }
            }

            for (verb in representations) {

                if ([GET_COLLECTION, DELETE_ENTITY, 'DELETE_COLLECTION'].contains(verb.verb)) continue

                TypeSpec resourceModel = buildResourceModel(rootFile, jsonObject, GeneratorUtil.verb(verb.verb))
                writeToFileSystem(currentPackageName, resourceModel, getRootOutputDir())
                amountOfGeneratedJavaSourceFiles++
            }

        }

        List<File> optionsFiles = []
        getOptionsSource().eachFileRecurse(FileType.FILES, { f -> if (f.name.endsWith('.json') && !f.name.startsWith('root.')) optionsFiles << f })

        if (!optionsFiles) {
            def logMessage = "There are no .json files within directory ${getOptionsSource().absolutePath}"
            logger.warn(logMessage)
        }

        logOptionsFiles(optionsFiles)

        for (File optionsFile in optionsFiles) {

            logger.debug("About to read json file ${optionsFile.name}")

            def jsonObject = jsonSlurper.parse optionsFile, 'UTF-8'

            currentPackageName = "${basePackageName}.${GeneratorUtil.composePackageName(jsonObject)}".toString()

            def representations = jsonObject.verbs.findAll { v ->
                v.representations.find {
                    it.name == 'json'
                }
            }

            for (verb in representations) {

                TypeSpec resourceModel
                // wenn die Ressource nur GET_COLLECTION definiert, dann soll das GET_ENTITY Model trotzdem erstellt werden
                if (representations.find { it.verb == GET_COLLECTION } && !representations.find {
                    it.verb == GET_ENTITY
                }) {
                    resourceModel = buildResourceModel(optionsFile, jsonObject, GeneratorUtil.verb(GET_ENTITY))
                } else {
                    if ([GET_COLLECTION, DELETE_ENTITY, DELETE_COLLECTION].contains(verb.verb)) continue
                    resourceModel = buildResourceModel(optionsFile, jsonObject, GeneratorUtil.verb(verb.verb))
                }
                writeToFileSystem(currentPackageName, resourceModel, getRootOutputDir())
                amountOfGeneratedJavaSourceFiles++
            }

            TypeSpec resourceInterface = buildResource(optionsFile, jsonObject)
            writeToFileSystem(currentPackageName, resourceInterface, getRootOutputDir())
            amountOfGeneratedJavaSourceFiles++

            def file = new File(restApiExtension.generatorImplOutput, "${currentPackageName.replaceAll('\\.', fileSeparator)}${fileSeparator}${GeneratorUtil.createResourceImplementationName(optionsFile)}.java")

            if (!file.exists()) {
                amountOfGeneratedJavaSourceFiles++
                TypeSpec resourceImpl = buildResourceImpl(optionsFile, jsonObject)
                writeToFileSystem(currentPackageName, resourceImpl, restApiExtension.generatorImplOutput)
                logger.lifecycle('Writing implementation {} to {}', file.name, restApiExtension.generatorImplOutput)
            } else {
                logger.lifecycle('Resource implementation {} exists. Skipping this one', file.name)
            }
        }

        logger.lifecycle "Done generating REST artifacts in {} milliseconds. (Processed JSON {} files and generated {} Java source code files)", System.currentTimeMillis() - start, optionsFiles.size(), amountOfGeneratedJavaSourceFiles
    }

    private void writeToFileSystem(String packageName, TypeSpec typeSpec, File outputDir) {
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).skipJavaLangImports(true).build()

        logger.info("Writing {} ...", typeSpec)
        if (getLogger().isDebugEnabled() || System.getProperty('adc.debug', 'false') != 'false') {
            javaFile.writeTo(System.out)
        }
        logger.debug('Writing to {}', outputDir.absolutePath)
        javaFile.writeTo(outputDir)
    }

    private TypeSpec buildFields(Object jsonObject, TypeSpec.Builder typeSpecBuilder) {
        return buildFields(jsonObject, typeSpecBuilder, null, null, null)
    }

    private TypeSpec buildFields(Object jsonObject, TypeSpec.Builder typeSpecBuilder, ClassName ft, Closure closure, String verb) {

        for (f in jsonObject.fields) {

            if (!'get'.equalsIgnoreCase(verb) && f.readonly == Boolean.TRUE) continue
            if ('get'.equalsIgnoreCase(verb) && !f.visible) continue

            def fieldType = ft

            if (!ft) {

                if (!GeneratorUtil.isSupportedDatatype(f.type) && project.restApi.objectResourceModelMapping) {
                    fieldType = project.restApi.objectResourceModelMapping.call(jsonObject, f)
                } else {
                    fieldType = GeneratorUtil.translateToJava(f.type)
                }
            }

            if (f.multiple == true) {
                ClassName list = ClassName.get("java.util", "List")
                fieldType = ParameterizedTypeName.get(list, fieldType)
            }

            FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, f.name, Modifier.PRIVATE)

            if (f.multiple == true) {
                fieldBuilder.initializer("new java.util.ArrayList<>()")
            } else if (f.defaultValue) {

                if (fieldType == SupportedDataTypes.STRING.className) {
                    fieldBuilder.initializer('$S', f.defaultValue)
                } else if (fieldType == SupportedDataTypes.DATE.className) {
                    fieldBuilder.initializer('LocalDate.now()')
                } else if (fieldType == SupportedDataTypes.DATETIME.className) {
                    fieldBuilder.initializer('LocalDateTime.now()')
                } else if (fieldType == SupportedDataTypes.BOOL.className) {
                    fieldBuilder.initializer(String.valueOf(f.defaultValue) == 'true' ? 'Boolean.TRUE' : 'Boolean.FALSE')
                }
            }

            if (f.mandatory.find { v -> v.equalsIgnoreCase(verb) }) {
                fieldBuilder.addAnnotation(JAVAX_VALIDATION_NOT_NULL.className)
            }

            String fieldMethodName = getMethodName(fieldBuilder.name)

            def getterMethodBuilder = MethodSpec.methodBuilder("get${fieldMethodName}")
            def setterMethodBuilder = MethodSpec.methodBuilder("set${fieldMethodName}")

            if (closure) {
                closure.call(f, fieldBuilder, getterMethodBuilder, setterMethodBuilder, verb)
            }

            FieldSpec field = fieldBuilder.build()

            typeSpecBuilder.addField(field)
                    .addMethod(getterMethodBuilder.returns(field.type).addModifiers(PUBLIC).addStatement("return this.${field.name}").build())
                    .addMethod(setterMethodBuilder.returns(void.class).addParameter(field.type, field.name).addModifiers(PUBLIC).addStatement("this.${field.name} = ${field.name}").build())

        }
    }

    String getMethodName(String field) {
        "${field[0].toUpperCase()}${field[1..field.length() - 1]}" as String
    }

    private TypeSpec buildResourceImpl(File optionsFile, Object jsonObject) {

        final String returnStatement = 'throw new RuntimeException("Not yet implemented")'
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(GeneratorUtil.createResourceImplementationName(optionsFile))
                .addModifiers(PUBLIC)

        LinkParser parser = new LinkParser(jsonObject.general.'x-route', jsonObject.general.version.split("\\.")[0])

        classBuilder.addSuperinterface(ClassName.get(currentPackageName, GeneratorUtil.createResourceName(optionsFile)))

        // Collection get
        if (containsGetCollection(jsonObject)) {

            fetchVerb(jsonObject, GET_COLLECTION).representations.each { representation ->

                MethodSpec.Builder collectionGetMethod

                if (representation.name == 'json') {
                    collectionGetMethod = MethodSpec.methodBuilder("getCollection")
                } else {
                    collectionGetMethod = MethodSpec.methodBuilder("getCollection${representation.name[0].toUpperCase()}${representation.name.substring(1)}")
                }

                collectionGetMethod.addModifiers(PUBLIC).returns(GeneratorUtil.getReturnType(project, optionsFile, 'Get', true, currentPackageName))
                collectionGetMethod.addAnnotation(AnnotationSpec.builder(JAVA_OVERRIDE.className).build())

                for (String pathVar in parser.pathVariables) {
                    collectionGetMethod.addParameter(
                            ParameterSpec.builder(String, pathVar).build()
                    )
                }

                // FilterModel
                collectionGetMethod.addParameter(
                        ParameterSpec.builder(RESTAPI_FILTERMODEL.className, 'filter').build()
                )

                // Parameters
                jsonObject.verbs.find { it.verb == GET_COLLECTION }.parameters.each { p ->
                    collectionGetMethod.addParameter(
                            ParameterSpec.builder(GeneratorUtil.translateToJava(p.type), p.name).build()
                    )
                }


                    collectionGetMethod.addStatement(returnStatement)


                classBuilder.addMethod(collectionGetMethod.build())
            }
        }

        // Entity get
        if (containsGetEntity(jsonObject)) {

            fetchVerb(jsonObject, GET_ENTITY).representations.each { representation ->

                MethodSpec.Builder entityGet
                String methodName

                if (representation.name == 'json') {
                    methodName = 'getEntity'
                } else {
                    methodName = "getEntity${representation.name[0].toUpperCase()}${representation.name.substring(1)}".toString()
                }

                entityGet = MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC)

                if (representation.name == 'json') {
                    entityGet.returns(GeneratorUtil.getReturnType(project, optionsFile, 'Get', currentPackageName))
                } else {
                    entityGet.returns(AnnotationTypes.JAX_RS_RESPONSE.className)
                }

                entityGet.addAnnotation(AnnotationSpec.builder(JAVA_OVERRIDE.className).build())


                for (String pathVar in parser.pathVariables) {
                    entityGet.addParameter(
                            ParameterSpec.builder(String, pathVar).build()
                    )
                }

                if (!parser.directEntity) {
                    entityGet.addParameter(
                            ParameterSpec.builder(String, "id").build()
                    )
                }
                entityGet.addStatement(returnStatement)

                classBuilder.addMethod(entityGet.build())
            }
        }

        if (containsPost(jsonObject)) {

            // POST mit Resource Model
            MethodSpec.Builder createEntityResourceModel = createModifyingMethodImpl(parser, JAX_RS_POST_VERB.className, optionsFile, { builder ->
                builder.addParameter(ParameterSpec.builder(ClassName.get(currentPackageName, GeneratorUtil.createResourceModelName(optionsFile, 'Post')), 'model').build())
                builder.addStatement(returnStatement)
            })

            classBuilder.addMethod(createEntityResourceModel.build())
        }

        if (containsPut(jsonObject)) {

            // PUT mit Resource Model
            MethodSpec.Builder updateEntityResourceModel = createModifyingMethodImpl(parser, JAX_RS_PUT_VERB.className, optionsFile, { builder ->
                builder.addParameter(ParameterSpec.builder(ClassName.get(currentPackageName, GeneratorUtil.createResourceModelName(optionsFile, 'Put')), 'model').build())

                if (!parser.directEntity) {
                    builder.addParameter(
                            ParameterSpec.builder(String, "id").build()
                    )
                }
                builder.addStatement(returnStatement)
            })

            classBuilder.addMethod(updateEntityResourceModel.build())
        }

        if (containsDeleteEntity(jsonObject)) {
            // DELETE

            MethodSpec.Builder entityDelete = MethodSpec.methodBuilder('deleteEntity').addModifiers(PUBLIC).returns(GeneratorUtil.getReturnType(project, optionsFile, 'Delete', false, currentPackageName))
            entityDelete.addAnnotation(AnnotationSpec.builder(JAVA_OVERRIDE.className).build())

            for (String pathVar in parser.pathVariables) {
                entityDelete.addParameter(
                        ParameterSpec.builder(String, pathVar).build()
                )
            }

            if (!parser.directEntity) {
                entityDelete.addParameter(
                        ParameterSpec.builder(String, "id").build()
                )

                entityDelete.addStatement(returnStatement)
            }

            classBuilder.addMethod(entityDelete.build())
        }

        return classBuilder.build()
    }

    private TypeSpec buildResource(File optionsFile, Object jsonObject) {
        String resourceName = GeneratorUtil.createResourceName(optionsFile)
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(resourceName)
                .addModifiers(PUBLIC)
                .addAnnotation(createGeneratedAnnotation(optionsFile))

        LinkParser parser = new LinkParser(jsonObject.general.'x-route', jsonObject.general.version.split("\\.")[0])


        interfaceBuilder.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', parser.toBasePath()).build())

        // Collection get

        if (containsGetCollection(jsonObject)) {

            def verb = fetchVerb(jsonObject, GET_COLLECTION)

            for (representation in verb.representations) {

                MethodSpec.Builder collectionGetMethod

                if (representation.name == 'json') {
                    collectionGetMethod = MethodSpec.methodBuilder("getCollection")
                } else {
                    collectionGetMethod = MethodSpec.methodBuilder("getCollection${representation.name[0].toUpperCase()}${representation.name.substring(1)}")
                }

                collectionGetMethod.addModifiers(Modifier.ABSTRACT, PUBLIC).returns(GeneratorUtil.getReturnType(project, optionsFile, 'Get', true, currentPackageName))
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
                jsonObject.verbs.find { it.verb == GET_COLLECTION }.parameters.each { p ->
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
        if (containsGetEntity(jsonObject)) {

            def verb = fetchVerb(jsonObject, GET_ENTITY)
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


        if (containsPost(jsonObject)) {
            // POST mit Resource Model
            MethodSpec.Builder createEntityResourceModel = entityCreateMethod(parser, optionsFile, { builder ->
                builder.addParameter(
                        ParameterSpec.builder(
                                ClassName.get(currentPackageName, GeneratorUtil.createResourceModelName(optionsFile, 'Post')), 'model')
                                .addAnnotation(AnnotationSpec.builder(JAVAX_VALIDATION_VALID.className).build())
                                .build()
                )
                builder.addAnnotation(AnnotationSpec.builder(JAX_RS_PATH.className).addMember('value', '$S', '').build())

                def verb = fetchVerb(jsonObject, POST)
                if (isSecurityEnabled() && verb.permissions?.size() > 0) {
                    builder.addAnnotation(AnnotationSpec.builder(RESTAPI_JWT_ANNOTATION.className).build())
                }
                addCachingAnnotation(builder, verb)
            })

            interfaceBuilder.addMethod(createEntityResourceModel.build())
        } else if (!parser.directEntity) {
            interfaceBuilder.addMethod(buildMethodNotAllowedHandler('createEntity', JAX_RS_POST_VERB.className, parser.directEntity ? '' : '{id}').build())
        }

        if (containsPut(jsonObject)) {

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
                def verb = fetchVerb(jsonObject, PUT)
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
        if (containsDeleteEntity(jsonObject)) {
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

            def verb = fetchVerb(jsonObject, DELETE_ENTITY)
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
                .initializer('$N', "\"${Minify.minify(optionsFile.text).replaceAll('"', '\\\\"')}\"");

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

    void addCachingAnnotation(MethodSpec.Builder builder, def jsonObject) {

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

    private MethodSpec.Builder createModifyingMethod(LinkParser parser, ClassName httpVerb, File optionsFile, Closure closure) {

        String methodName = httpVerb.simpleName() == POST ? 'createEntity' : 'updateEntity'
        String verb = httpVerb.simpleName() == POST ? 'Post' : 'Put'

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

    private MethodSpec.Builder createModifyingMethodImpl(LinkParser parser, ClassName httpVerb, File optionsFile, Closure closure) {

        String methodName = httpVerb.simpleName() == POST ? 'createEntity' : 'updateEntity'
        String verb = httpVerb.simpleName() == POST ? 'Post' : 'Put'

        MethodSpec.Builder createEntityMethodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC).returns(GeneratorUtil.getReturnType(project, optionsFile, verb, currentPackageName))
        createEntityMethodBuilder.addAnnotation(AnnotationSpec.builder(JAVA_OVERRIDE.className).build())

        for (String pathVar in parser.pathVariables) {
            createEntityMethodBuilder.addParameter(
                    ParameterSpec.builder(String, pathVar).build()
            )
        }

        closure.call(createEntityMethodBuilder)
        createEntityMethodBuilder
    }

    private MethodSpec.Builder entityCreateMethod(LinkParser parser, File optionsFile, Closure closure) {
        createModifyingMethod(parser, JAX_RS_POST_VERB.className, optionsFile, closure)
    }

    private MethodSpec.Builder entityUpdateMethod(LinkParser parser, File optionsFile, Closure closure) {
        createModifyingMethod(parser, JAX_RS_PUT_VERB.className, optionsFile, closure)
    }

    private TypeSpec buildType(Object jsonObject) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(GeneratorUtil.createClassname("${jsonObject.name}Type"))
                .addModifiers(PUBLIC)
                .addSuperinterface(Serializable)

        buildFields(jsonObject, classBuilder)
        return classBuilder.build()
    }

    private TypeSpec buildResourceModel(File optionsFile, Object jsonObject, String verb) {

        String resourceModelName = GeneratorUtil.createResourceModelName(optionsFile, verb)
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(resourceModelName)
                .addModifiers(PUBLIC)
                .addAnnotation(createGeneratedAnnotation(optionsFile))
                .addSuperinterface(Serializable)

        def verbGet = fetchVerb(jsonObject, GET_ENTITY)

        if (!verbGet && jsonObject.verbs.size() > 0) {
            verbGet = jsonObject.verbs[0]
        }

        String mimeType = verbGet.representations.find {
            it.name == 'json'
        }.mimetype

        classBuilder.addField(
                FieldSpec.builder(String.class, 'TYPE')
                        .addModifiers(PUBLIC, FINAL, STATIC)
                        .initializer('$S', mimeType)
                        .build()
        )

        List<String> fields = []
        buildFields(jsonObject, classBuilder, null, { field, FieldSpec.Builder fieldBuilder, MethodSpec.Builder getter, MethodSpec.Builder setter, String v ->

            if (!'get'.equalsIgnoreCase(v) && field.min instanceof Number && field.max instanceof Number) {
                fieldBuilder.addAnnotation(
                        AnnotationSpec.builder(JAVAX_VALIDATION_SIZE.className).addMember('min', '$N', field.min.toString()).addMember('max', '$N', field.max.toString()).build()
                )
            }

            fields << field
        }, verb)

        // --> equals Methode überschreiben
        String equalsParamName = 'other'
        String equalsCastVarName = 'that'
        def equalsBuilder = MethodSpec.methodBuilder('equals').addAnnotation(Override).addModifiers(PUBLIC).addParameter(Object, equalsParamName).returns(BOOLEAN)

        equalsBuilder.addStatement('if (this == $L) return true', equalsParamName)
        equalsBuilder.addStatement('if (! ($L instanceof $L)) return false', equalsParamName, resourceModelName)
        equalsBuilder.addStatement('$L $L = ($L) $L', resourceModelName, equalsCastVarName, resourceModelName, equalsParamName)

        def getters = fields.collect { "get${getMethodName(it.name)}()" }
        String code = getters.collect { "\$T.equals(${it}, ${equalsCastVarName}.${it})" }.join(' && ')

        equalsBuilder.addStatement("return ${code}", Collections.nCopies(getters.size(), Objects) as Object[])

        classBuilder.addMethod(equalsBuilder.build())

        // --> hashCode Methode überschreiben
        def hashCodeBuilder = MethodSpec.methodBuilder('hashCode').addAnnotation(Override).addModifiers(PUBLIC).returns(INT)

        code = "\$T.hash(${getters.collect { it }.join(', ')})"

        hashCodeBuilder.addStatement("return ${code}", Objects)

        classBuilder.addMethod(hashCodeBuilder.build())

        return classBuilder.build()
    }

    private AnnotationSpec createGeneratedAnnotation(File optionsFile) {

        def map = [
                'value'   : RestApiPlugin.PLUGIN_ID,
                'comments': "Specification filename: ${optionsFile.name}"
        ]

        if (project.restApi.generateDateAttribute) {
            map.date = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"))
        }

        def annotation = createAnnotation(JAVAX_GENERATED.className, map)

        return annotation
    }

    private AnnotationSpec createAnnotation(ClassName className, Map<String, Object> attributes) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(className)

        attributes.each { key, value ->

            String param = '$S'

            if (value instanceof String && value?.endsWith('.class')) param = '$N'

            builder.addMember(key, param, value)
        }
        return builder.build()
    }

    private void logOptionsFiles(List<File> optionsFiles) {

        logger.info("Found {} options files", optionsFiles ? optionsFiles.size() : 0)

        if (logger.isTraceEnabled() && optionsFiles) {
            for (File f in optionsFiles) {
                logger.trace("Found file {}", f.absolutePath)
            }
        }
    }

    private boolean containsGetEntity(Object jsonObject) {
        return fetchVerb(jsonObject, GET_ENTITY) != null
    }

    private boolean containsGetCollection(Object jsonObject) {
        return fetchVerb(jsonObject, GET_COLLECTION) != null
    }

    private boolean containsPost(Object jsonObject) {
        return fetchVerb(jsonObject, POST) != null
    }

    private boolean containsPut(Object jsonObject) {
        return fetchVerb(jsonObject, PUT) != null
    }

    private boolean containsDeleteEntity(Object jsonObject) {
        return fetchVerb(jsonObject, DELETE_ENTITY) != null
    }

    private boolean containsDeleteCollection(Object jsonObject) {
        return fetchVerb(jsonObject, DELETE_COLLECTION) != null
    }

    Object fetchVerb(Object jsonObject, String verb) {
        return jsonObject.verbs.find { it.verb == verb }
    }

    boolean isSecurityEnabled() {
        return project.restApi.enableSecurity
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
}
