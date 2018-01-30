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
package ch.silviowangler.gradle.restapi.builder.spring;

import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.PluginTypes;
import ch.silviowangler.gradle.restapi.builder.AbstractResourceBuilder;
import ch.silviowangler.gradle.restapi.builder.ArtifactType;
import ch.silviowangler.rest.contract.model.v1.Representation;
import ch.silviowangler.rest.contract.model.v1.Verb;
import com.squareup.javapoet.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.silviowangler.gradle.restapi.PluginTypes.*;

public class SpringRootResourceFactory extends AbstractResourceBuilder {

    private static final ClassName STRING_CLASS = ClassName.get(String.class);

    private TypeSpec.Builder resourceBuilder;

    @Override
    public TypeSpec buildRootResource() {
        reset();
        setArtifactType(ArtifactType.RESOURCE);
        resourceBuilder = interfaceBaseInstance();

        Map<String, Object> args = new HashMap<>();
        args.put("value", getPath());

        resourceBuilder.addAnnotation(createAnnotation(SPRING_REQUEST_MAPPING, args));

        generateResourceMethods();
        return resourceBuilder.build();
    }

    @Override
    public TypeSpec buildResourceImpl() {
        reset();
        setArtifactType(ArtifactType.RESOURCE_IMPL);
        TypeSpec.Builder builder = classBaseInstance();

        builder.addAnnotation(createAnnotation(SPRING_REST_CONTROLLER));

        builder.addSuperinterface(ClassName.get(getCurrentPackageName(), resourceName()));

        super.generateResourceMethods();
        return builder.build();
    }

    @Override
    protected void createOptionsMethod() {

        Verb verb = new Verb();
        verb.setVerb("OPTIONS");

        setCurrentVerb(verb);
        MethodSpec.Builder optionsMethod = createMethod("getOptions", STRING_CLASS);

        optionsMethod.addStatement("return OPTIONS_CONTENT");

        interfaceBaseInstance().addMethod(optionsMethod.build());
        setCurrentVerb(null);
    }

    @Override
    public AnnotationSpec getQueryParamAnnotation(String paramName) {
        return AnnotationSpec.builder(SPRING_REQUEST_PARAM.getClassName())
                .addMember("value", "$S", paramName).build();
    }

    @Override
    public Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId, Representation representation) {
        List<AnnotationSpec> annotations = new ArrayList<>();

        String httpMethod = getHttpMethod();

        AnnotationSpec.Builder builder = AnnotationSpec.builder(SPRING_REQUEST_MAPPING.getClassName());

        builder.addMember("method", "$T." + httpMethod.toUpperCase(), SPRING_REQUEST_METHOD.getClassName());

        if (applyId) {
            if (representation.isJson()) {
                builder.addMember("path", "\"/{$L}\"", "id");
            } else {
                builder.addMember("path", "\"/{$L}.$L\"", "id", representation.getName());
            }
        }

        if (representation.isJson() && getResponseEncoding() != null) {

            if (Charset.forName("UTF-8").equals(getResponseEncoding())) {
                builder.addMember("produces", "$T.APPLICATION_JSON_UTF8_VALUE", SPRING_HTTP_MEDIA_TYPE.getClassName());
            }else {
                builder.addMember("produces", "application/json;charset=$L", getResponseEncoding().name());
            }

        } else if (representation.isJson()) {
            builder.addMember("produces", "$T.APPLICATION_JSON_VALUE", SPRING_HTTP_MEDIA_TYPE.getClassName());
        } else {
            builder.addMember("produces", "$S", representation.getMimetype());
        }

        annotations.add(builder.build());

        if (representation.isJson()) {
            annotations.add(createAnnotation(SPRING_RESPONSE_BODY));
        }
        return annotations;
    }

    @Override
    public PluginTypes getPathVariableAnnotationType() {
        return SPRING_PATH_VARIABLE;
    }

    @Override
    public void generateMethodNotAllowedStatement(MethodSpec.Builder builder) {
        builder.addStatement("return new $T<>($T.METHOD_NOT_ALLOWED)", getMethodNowAllowedReturnType(), SPRING_HTTP_STATUS.getClassName());
    }

    @Override
    public ClassName getMethodNowAllowedReturnType() {
        return SPRING_RESPONSE_ENTITY.getClassName();
    }

    @Override
    public TypeName resourceMethodReturnType(Verb verb, Representation representation) {
        String v = toHttpMethod(verb);
        return GeneratorUtil.getSpringBootReturnType(getResourceContractContainer().getSourceFileName(), v, false, getCurrentPackageName(), representation);
    }
}
