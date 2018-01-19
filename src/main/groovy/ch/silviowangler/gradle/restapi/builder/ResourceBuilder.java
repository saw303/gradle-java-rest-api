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

import ch.silviowangler.gradle.restapi.GenerateRestApiTask;
import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.RestApiExtension;
import ch.silviowangler.gradle.restapi.RestApiPlugin;
import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.Verb;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import org.gradle.api.Project;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static ch.silviowangler.gradle.restapi.AnnotationTypes.JAVAX_GENERATED;

public interface ResourceBuilder {

    Project getProject();

    ResourceBuilder withSpecification(File file);

    File getSpecification();

    ResourceContract getModel();

    default String resourceName(File specification) {
        return GeneratorUtil.createResourceName(specification);
    }

    default AnnotationSpec createGeneratedAnnotation() {

        Map<String, Object> map = new HashMap<>();

        map.put("value", RestApiPlugin.PLUGIN_ID);
        map.put("comments", "Specification filename: " + getSpecification().getName());

        RestApiExtension restApiExtension = getProject().getExtensions().getByType(RestApiExtension.class);

        if (restApiExtension.getGenerateDateAttribute()) {
            ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
            map.put("date", utc.toString());
        }

        return createAnnotation(JAVAX_GENERATED.getClassName(), map);
    }

    default AnnotationSpec createAnnotation(ClassName className, Map<String, Object> attributes) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(className);

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
}
