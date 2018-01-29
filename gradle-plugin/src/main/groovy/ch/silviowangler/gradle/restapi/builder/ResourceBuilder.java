/**
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
package ch.silviowangler.gradle.restapi.builder;

import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.PluginTypes;
import ch.silviowangler.gradle.restapi.RestApiPlugin;
import ch.silviowangler.rest.contract.model.v1.Representation;
import ch.silviowangler.rest.contract.model.v1.Verb;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_GENERATED;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVA_OVERRIDE;
import static javax.lang.model.element.Modifier.*;

public interface ResourceBuilder {

    String GET_COLLECTION = "GET_COLLECTION";
    String GET_ENTITY = "GET_ENTITY";
    String POST = "POST";
    String PUT = "PUT";
    String DELETE_ENTITY = "DELETE_ENTITY";
    String DELETE_COLLECTION = "DELETE_COLLECTION";

    String getCurrentPackageName();

    ResourceBuilder withCurrentPackageName(String packageName);

    ArtifactType getArtifactType();

    ResourceContractContainer getResourceContractContainer();

    TypeSpec buildRootResource();

    TypeSpec buildResourceImpl();

    default String resourceName() {
        return GeneratorUtil.createResourceName(getResourceContractContainer().getSourceFileName());
    }

    default String resourceImplName() {
        return GeneratorUtil.createResourceImplementationName(getResourceContractContainer().getSourceFileName());
    }


    TypeName resourceMethodReturnType(Verb verb, Representation representation);

    default String toHttpMethod(Verb verb) {
        String v;

        if (GET_ENTITY.equals(verb.getVerb()) || GET_COLLECTION.equals(verb.getVerb())) {
            v = "Get";
        } else if (DELETE_ENTITY.equals(verb.getVerb()) || DELETE_COLLECTION.equals(verb.getVerb())) {
            v = "Delete";
        } else if (PUT.equals(verb.getVerb())) {
            v = "Put";
        } else if (POST.equals(verb.getVerb())) {
            v = "Post";
        } else {
            throw new IllegalArgumentException("Unknown verb " + verb.getVerb());
        }
        return v;
    }

    default ClassName resourceModelName(Verb verb) {
        return ClassName.get(getCurrentPackageName(), GeneratorUtil.createResourceModelName(getResourceContractContainer().getSourceFileName(), toHttpMethod(verb)));
    }

    default AnnotationSpec createGeneratedAnnotation(boolean printTimestamp) {

        Map<String, Object> map = new HashMap<>();

        map.put("value", RestApiPlugin.PLUGIN_ID);
        map.put("comments", "Specification filename: " + getResourceContractContainer().getSourceFileName());

        if (printTimestamp) {
            ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
            map.put("date", utc.toString());
        }

        return createAnnotation(JAVAX_GENERATED, map);
    }

    default AnnotationSpec createAnnotation(PluginTypes className) {
        return createAnnotation(className, new HashMap<>());
    }

    default AnnotationSpec createAnnotation(PluginTypes className, Map<String, Object> attributes) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(className.getClassName());

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {

            String param = "$S";

            if (entry.getValue() instanceof String && ((String) entry.getValue()).endsWith(".class")) {
                param = "$N";
            }
            builder.addMember(entry.getKey(), param, entry.getValue());
        }
        return builder.build();
    }

    void generateResourceMethods();

    void generateMethodNotAllowedStatement(MethodSpec.Builder builder);

    ClassName getMethodNowAllowedReturnType();

    default MethodSpec.Builder createMethodNotAllowedHandler(String methodName) {
        Representation representation = new Representation();
        representation.setName("json");
        MethodSpec.Builder builder = createMethod(methodName, getMethodNowAllowedReturnType(), new HashMap<>(), representation);

        if (isIdGenerationRequired(methodName)) {
            builder.addParameter(generateIdParam());
        }
        generateMethodNotAllowedStatement(builder);

        return builder;
    }

    default MethodSpec.Builder createMethod(String methodName, TypeName returnType) {
        Representation representation = new Representation();
        representation.setName("json");
        return createMethod(methodName, returnType, new HashMap<>(), representation);
    }

    default MethodSpec.Builder createMethod(String methodName, TypeName returnType, Map<String, ClassName> params, Representation representation) {


        if (!representation.getName().equals("json")) {
            methodName += CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, representation.getName());
        }

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);

        if ("getCollection".equals(methodName)) {
            methodBuilder.returns(ParameterizedTypeName.get(ClassName.get(Collection.class), returnType));
        } else {
            methodBuilder.returns(returnType);
        }

        methodBuilder.addModifiers(PUBLIC);


        if (ArtifactType.RESOURCE.equals(getArtifactType())) {
            Iterable<AnnotationSpec> annotations = getResourceMethodAnnotations(isIdGenerationRequired(methodName), representation);
            methodBuilder.addAnnotations(annotations);
        } else if (ArtifactType.RESOURCE_IMPL.equals(getArtifactType())) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(JAVA_OVERRIDE.getClassName()).build());
        }

        if ("getOptions".equals(methodName) || isDefaultMethodNotAllowed(methodName)) {
            methodBuilder.addModifiers(DEFAULT);
        } else {
            if (ArtifactType.RESOURCE.equals(getArtifactType())) {
                methodBuilder.addModifiers(ABSTRACT);
            } else {
                methodBuilder.addModifiers(PUBLIC);

                methodBuilder.addStatement("throw new $T()", PluginTypes.PLUGIN_NOT_YET_IMPLEMENTED_EXCEPTION.getClassName());
            }
        }

        params.forEach((key, value) -> {

            ParameterSpec.Builder builder = ParameterSpec.builder(value, key);

            if (ArtifactType.RESOURCE.equals(getArtifactType())) {
                builder.addAnnotation(getQueryParamAnnotation(key));
            }
            ParameterSpec parameter = builder.build();

            methodBuilder.addParameter(parameter);

        });
        return methodBuilder;
    }

    default boolean isIdGenerationRequired(String methodName) {
        List<String> noId = Arrays.asList("getOptions", "createEntity", "getCollection", "deleteCollection");

        boolean result = true;

        for (String s : noId) {
            if (methodName.startsWith(s)) {
                return false;
            }
        }
        return true;
    }

    default boolean isDefaultMethodNotAllowed(String methodName) {
        return methodName.endsWith("AutoAnswer") && ArtifactType.RESOURCE.equals(getArtifactType());
    }

    AnnotationSpec getQueryParamAnnotation(String paramName);

    Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId, Representation representation);

    PluginTypes getPathVariableAnnotationType();

    default ParameterSpec generateIdParam() {
        ParameterSpec.Builder param = ParameterSpec.builder(ClassName.get(String.class), "id");

        if (getArtifactType().equals(ArtifactType.RESOURCE)) {

            Map<String, Object> attrs = new HashMap<>();
            attrs.put("value", "id");

            param.addAnnotation(
                    createAnnotation(getPathVariableAnnotationType(), attrs)
            ).build();
        }
        return param.build();
    }

    ResourceBuilder withResourceContractContainer(ResourceContractContainer resourceContract);

    ResourceBuilder withTimestampInGeneratedAnnotation(boolean val);

    Set<TypeSpec> buildResourceTypes(Set<ClassName> types);

    Set<TypeSpec> buildResourceModels(Set<ClassName> types);
}
