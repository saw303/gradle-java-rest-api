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
package ch.silviowangler.gradle.restapi.builder.spring;

import static ch.silviowangler.gradle.restapi.PluginTypes.*;

import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.PluginTypes;
import ch.silviowangler.gradle.restapi.builder.AbstractResourceBuilder;
import ch.silviowangler.gradle.restapi.builder.ArtifactType;
import ch.silviowangler.rest.contract.model.v1.Representation;
import ch.silviowangler.rest.contract.model.v1.Verb;
import ch.silviowangler.rest.contract.model.v1.VerbParameter;
import com.squareup.javapoet.*;
import java.util.*;

public class SpringRootResourceFactory extends AbstractResourceBuilder {

  private static final ClassName STRING_CLASS = ClassName.get(String.class);

  private final boolean explicitExtensions;

  public SpringRootResourceFactory(boolean explicitExtensions) {
    this.explicitExtensions = explicitExtensions;
  }

  @Override
  public boolean supportsInterfaces() {
    return false;
  }

  @Override
  public TypeSpec buildResource() {
    reset();
    setArtifactType(ArtifactType.ABSTRACT_RESOURCE);
    TypeSpec.Builder resourceBuilder = resourceBaseTypeBuilder();

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
    builder.superclass(ClassName.get(getCurrentPackageName(), resourceName()));

    generateResourceMethods();
    return builder.build();
  }

  @Override
  protected void createOptionsMethod() {

    Verb verb = new Verb();
    verb.setVerb("OPTIONS");

    setCurrentVerb(verb);
    MethodSpec.Builder optionsMethod = createMethod("getOptions", STRING_CLASS);

    optionsMethod.addStatement("return OPTIONS_CONTENT");

    resourceBaseTypeBuilder().addMethod(optionsMethod.build());
    setCurrentVerb(null);
  }

  @Override
  public boolean providesRequestBodyAnnotation() {
    return true;
  }

  @Override
  public AnnotationSpec buildRequestBodyAnnotation() {
    return createAnnotation(SPRING_REQUEST_BODY);
  }

  @Override
  public List<AnnotationSpec> getQueryParamAnnotations(VerbParameter param) {
    AnnotationSpec.Builder builder =
        AnnotationSpec.builder(SPRING_REQUEST_PARAM.getClassName())
            .addMember("value", "$S", param.getName());

    if (!param.getMandatory()) {
      builder.addMember("required", "$L", false);
    }
    // TODO handle other VerbParameter options like defaultValue

    return Collections.singletonList(builder.build());
  }

  @Override
  public Iterable<AnnotationSpec> getResourceMethodAnnotations(
      boolean applyId, Representation representation, String methodName) {
    List<AnnotationSpec> annotations = new ArrayList<>();

    String httpMethod = getHttpMethod();

    AnnotationSpec.Builder builder = AnnotationSpec.builder(SPRING_REQUEST_MAPPING.getClassName());

    builder.addMember(
        "method", "$T." + httpMethod.toUpperCase(), SPRING_REQUEST_METHOD.getClassName());

    if (applyId) {
      if (representation.isJson() && !explicitExtensions) {
        builder.addMember("path", "\"/{$L}\"", "id");
      } else {
        builder.addMember("path", "\"/{$L}.$L\"", "id", representation.getName());
      }
    } else if (explicitExtensions) {
      builder.addMember("path", "\"/.$L\"", representation.getName());
    }

    builder.addMember("produces", "$S", representation.getMimetype().toString());

    annotations.add(builder.build());

    if (representation.isJson()) {
      annotations.add(createAnnotation(SPRING_RESPONSE_BODY));
    }

    List<String> responseStatusRequired =
        Arrays.asList("createEntity", "deleteEntity", "deleteCollection");

    if (responseStatusRequired.contains(methodName)) {

      String v;
      if (methodName.startsWith("create")) {
        v = "$T.CREATED";
      } else if (methodName.startsWith("delete")) {
        v = "$T.NO_CONTENT";
      } else {
        throw new IllegalArgumentException("Unknown method name " + methodName);
      }

      AnnotationSpec.Builder b = AnnotationSpec.builder(SPRING_RESPONSE_STATUS.getClassName());
      b.addMember("code", v, SPRING_HTTP_STATUS.getClassName());
      annotations.add(b.build());
    }

    return annotations;
  }

  @Override
  public PluginTypes getPathVariableAnnotationType() {
    return SPRING_PATH_VARIABLE;
  }

  @Override
  public boolean shouldGenerateHeadMethod() {
    return false;
  }

  @Override
  public void generateMethodNotAllowedStatement(MethodSpec.Builder builder) {
    builder.addStatement(
        "return new $T<>($T.METHOD_NOT_ALLOWED)",
        getMethodNowAllowedReturnType(),
        SPRING_HTTP_STATUS.getClassName());
  }

  @Override
  public ClassName getMethodNowAllowedReturnType() {
    return SPRING_RESPONSE_ENTITY.getClassName();
  }

  @Override
  public TypeName resourceMethodReturnType(Verb verb, Representation representation) {
    String v = toHttpMethod(verb);
    return GeneratorUtil.getSpringBootReturnType(
        getResourceContractContainer().getSourceFileName(),
        v,
        verb.getVerb().endsWith("_COLLECTION"),
        getCurrentPackageName(),
        representation);
  }
}
