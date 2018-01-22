/**
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2018 Silvio Wangler (silvio.wangler@gmail.com)
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
package ch.silviowangler.gradle.restapi.builder;

import ch.silviowangler.gradle.restapi.*;
import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.Verb;
import com.squareup.javapoet.*;
import org.gradle.api.Project;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static ch.silviowangler.gradle.restapi.AnnotationTypes.JAVAX_GENERATED;
import static javax.lang.model.element.Modifier.*;

public interface ResourceBuilder {

    Project getProject();

    ResourceBuilder withSpecification(File file);

    String getCurrentPackageName();

    ResourceBuilder withCurrentPackageName(String packageName);

    File getSpecification();

    ResourceContract getModel();

    default String resourceName() {
        return GeneratorUtil.createResourceName(getSpecification());
    }

    default TypeName resourceModelName() {
        return GeneratorUtil.getReturnType(getProject(), getSpecification(), "Get", false, getCurrentPackageName());
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

    default boolean containsGetEntity(ResourceContract resourceContract) {
        return fetchVerb(resourceContract, GenerateRestApiTask.GET_ENTITY).isPresent();
    }

    default boolean containsGetCollection(ResourceContract resourceContract) {
        return fetchVerb(resourceContract, GenerateRestApiTask.GET_COLLECTION).isPresent();
    }

    default boolean containsPost(ResourceContract resourceContract) {
        return fetchVerb(resourceContract, GenerateRestApiTask.POST).isPresent();
    }

    default boolean containsPut(ResourceContract resourceContract) {
        return fetchVerb(resourceContract, GenerateRestApiTask.PUT).isPresent();
    }

    default boolean containsDeleteEntity(ResourceContract resourceContract) {
        return fetchVerb(resourceContract, GenerateRestApiTask.DELETE_ENTITY).isPresent();
    }

    default Optional<Verb> fetchVerb(ResourceContract resourceContract, String verbStringValue) {

        Objects.requireNonNull(verbStringValue, "verbStringValue must not be null");
        List<Verb> verbs = resourceContract.getVerbs();
        return verbs.stream().filter(verb -> verbStringValue.equals(verb.getVerb())).findFirst();
    }

    default MethodSpec.Builder createInterfaceMethod(String methodName, TypeName returnType) {
        return createInterfaceMethod(methodName, returnType, new HashMap<>());
    }


    default MethodSpec.Builder createInterfaceMethod(String methodName, TypeName returnType, Map<String, ClassName> params) {
        MethodSpec.Builder methodBuilder = MethodSpec
                .methodBuilder(methodName)
                .returns(returnType)
                .addModifiers(PUBLIC);


        methodBuilder.addAnnotations(getResourceMethodAnnotations(!"getOptions".equals(methodName)));


        if ("getOptions".equals(methodName)) {
            methodBuilder.addModifiers(DEFAULT);
        } else {
            methodBuilder.addModifiers(ABSTRACT);
        }

        params.forEach((key, value) -> {

            ParameterSpec parameter = ParameterSpec.builder(value, key)
                    .addAnnotation(getQueryParamAnnotation(key)).build();

            methodBuilder.addParameter(parameter);

        });
        return methodBuilder;
    }

    AnnotationSpec getQueryParamAnnotation(String paramName);

    Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId);
}
