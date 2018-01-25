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

import ch.silviowangler.gradle.restapi.AnnotationTypes;
import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.RestApiExtension;
import ch.silviowangler.gradle.restapi.RestApiPlugin;
import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import com.squareup.javapoet.*;
import org.gradle.api.Project;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static ch.silviowangler.gradle.restapi.AnnotationTypes.JAVAX_GENERATED;
import static ch.silviowangler.gradle.restapi.AnnotationTypes.JAVA_OVERRIDE;
import static javax.lang.model.element.Modifier.*;

public interface ResourceBuilder {

    Project getProject();

    ResourceBuilder withSpecification(File file);

    String getCurrentPackageName();

    ResourceBuilder withCurrentPackageName(String packageName);

    File getSpecification();

    ArtifactType getArtifactType();

    ResourceContract getModel();

    default String resourceName() {
        return GeneratorUtil.createResourceName(getSpecification());
    }

    default String resourceImplName() {
        return GeneratorUtil.createResourceImplementationName(getSpecification());
    }


    default TypeName resourceMethodReturnType(String verb) {
        return GeneratorUtil.getReturnType(getSpecification(), verb, false, getCurrentPackageName());
    }

    default TypeName resourceModelName(String verb) {
        return ClassName.get(getCurrentPackageName(), GeneratorUtil.createResourceModelName(getSpecification(), verb));
    }

    default AnnotationSpec createGeneratedAnnotation() {

        Map<String, Object> map = new HashMap<>();

        map.put("value", RestApiPlugin.PLUGIN_ID);
        map.put("comments", "Specification filename: " + getSpecification().getName());

        RestApiExtension restApiExtension = getRestApiExtension();

        if (restApiExtension.isGenerateDateAttribute()) {
            ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
            map.put("date", utc.toString());
        }

        return createAnnotation(JAVAX_GENERATED, map);
    }

    default RestApiExtension getRestApiExtension() {
        return getProject().getExtensions().getByType(RestApiExtension.class);
    }

    default AnnotationSpec createAnnotation(AnnotationTypes className) {
        return createAnnotation(className, new HashMap<>());
    }

    default AnnotationSpec createAnnotation(AnnotationTypes className, Map<String, Object> attributes) {
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
        MethodSpec.Builder builder = createMethod(methodName, getMethodNowAllowedReturnType(), new HashMap<>());

        if (isIdGenerationRequired(methodName)) {
            builder.addParameter(generateIdParam());
        }

        generateMethodNotAllowedStatement(builder);

        return builder;
    }

    default MethodSpec.Builder createMethod(String methodName, TypeName returnType) {
        return createMethod(methodName, returnType, new HashMap<>());
    }

    default MethodSpec.Builder createMethod(String methodName, TypeName returnType, Map<String, ClassName> params) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);

        if ("getCollection".equals(methodName)) {
            methodBuilder.returns(ParameterizedTypeName.get(ClassName.get(Collection.class), returnType));
        } else {
            methodBuilder.returns(returnType);
        }

        methodBuilder.addModifiers(PUBLIC);


        if (ArtifactType.RESOURCE.equals(getArtifactType())) {
            Iterable<AnnotationSpec> annotations = getResourceMethodAnnotations(isIdGenerationRequired(methodName));
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
                methodBuilder.addStatement("throw new RuntimeException(\"Not yet implemented\")");
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

    Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId);

    AnnotationTypes getPathVariableAnnotationType();

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
}
