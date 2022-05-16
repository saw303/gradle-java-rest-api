/*
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2020 Silvio Wangler (silvio.wangler@gmail.com)
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
package ch.silviowangler.gradle.restapi.builder.micronaut;

import static ch.silviowangler.gradle.restapi.PluginTypes.JAKARTA_INJECT;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAKARTA_SINGLETON;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_INJECT;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_NULLABLE;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_SINGLETON;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_NOT_EMPTY;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_NOT_NULL;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_SIZE;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_CLIENT;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_CONSUMES;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_CONTROLLER;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_DATE_FORMAT;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_DATE_TIME_FORMAT;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_DELETE;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_EXECUTE_ON;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_GET;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_HEAD;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_HEADER;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_HTTP_MEDIA_TYPE;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_HTTP_RESPONSE;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_HTTP_STATUS;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_INTROSPECTED;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_OPTIONS;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_POST;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_PRODUCES;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_PUT;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_QUERY_VALUE;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_REQUEST_BODY;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_STATUS;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_VALIDATED;
import static ch.silviowangler.gradle.restapi.PluginTypes.RESTAPI_RESPONSE_CREATOR;
import static ch.silviowangler.gradle.restapi.builder.ArtifactType.CLIENT;
import static ch.silviowangler.gradle.restapi.builder.ArtifactType.DELEGATOR_RESOURCE;

import ch.silviowangler.gradle.restapi.GenerationMode;
import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.PluginTypes;
import ch.silviowangler.gradle.restapi.RestApiExtension;
import ch.silviowangler.gradle.restapi.TargetFramework;
import ch.silviowangler.gradle.restapi.builder.AbstractResourceBuilder;
import ch.silviowangler.gradle.restapi.builder.ArtifactType;
import ch.silviowangler.gradle.restapi.builder.MethodContext;
import ch.silviowangler.rest.contract.model.v1.Header;
import ch.silviowangler.rest.contract.model.v1.Representation;
import ch.silviowangler.rest.contract.model.v1.Verb;
import ch.silviowangler.rest.contract.model.v1.VerbParameter;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.lang.model.element.Modifier;
import org.apache.groovy.util.Maps;

/**
 * @author Silvio Wangler
 */
public class MicronautResourceFactory extends AbstractResourceBuilder {

  private static final ClassName STRING_CLASS = ClassName.get(String.class);
  private static final String DELEGATE_VAR_NAME = "delegate";
  private final RestApiExtension restApiExtension;

  public MicronautResourceFactory(RestApiExtension restApiExtension) {
    this.restApiExtension = restApiExtension;
  }

  @Override
  protected void createOptionsMethod() {
    Verb verb = new Verb("OPTIONS");
    setCurrentVerb(verb);
    MethodSpec.Builder optionsMethod = createMethod("getOptions", STRING_CLASS);

    optionsMethod.addStatement("return OPTIONS_CONTENT");

    resourceBaseTypeBuilder().addMethod(optionsMethod.build());
    setCurrentVerb(null);
  }

  @Override
  public TypeSpec buildResource() {
    reset();
    setArtifactType(DELEGATOR_RESOURCE);
    TypeSpec.Builder resourceBuilder = resourceBaseTypeBuilder();

    Map<String, Object> args = new HashMap<>();
    args.put("value", getPath());

    resourceBuilder.addAnnotation(createAnnotation(MICRONAUT_CONTROLLER, args));

    ClassName delegatorClass = ClassName.get(getCurrentPackageName(), resourceDelegateName());

    FieldSpec fieldDelegate =
        FieldSpec.builder(delegatorClass, DELEGATE_VAR_NAME)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

    MethodSpec.Builder methodBuilder =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(delegatorClass, DELEGATE_VAR_NAME)
            .addStatement("this.$N = $N", DELEGATE_VAR_NAME, DELEGATE_VAR_NAME);

    TargetFramework targetFramework = this.restApiExtension.getTargetFramework();
    if (targetFramework == TargetFramework.MICRONAUT_3) {
      methodBuilder.addAnnotation(createAnnotation(JAKARTA_INJECT));
    } else {
      methodBuilder.addAnnotation(createAnnotation(JAVAX_INJECT));
    }

    MethodSpec constructor = methodBuilder.build();

    resourceBuilder.addField(fieldDelegate);
    resourceBuilder.addMethod(constructor);

    generateResourceMethods();
    return resourceBuilder.build();
  }

  @Override
  public TypeSpec buildClient() {
    reset();
    setArtifactType(CLIENT);
    TypeSpec.Builder resourceBuilder = resourceBaseTypeBuilder(clientName());

    Map<String, Object> clientArgs = new HashMap<>();
    clientArgs.put("id", this.restApiExtension.getClientId());

    resourceBuilder.addAnnotation(createAnnotation(MICRONAUT_CLIENT, clientArgs));
    resourceBuilder.addAnnotation(createAnnotation(MICRONAUT_VALIDATED));

    generateResourceMethods();
    generateClientMethods();

    return resourceBuilder.build();
  }

  @Override
  public TypeSpec buildResourceImpl() {
    reset();
    setArtifactType(ArtifactType.RESOURCE_IMPL);
    TypeSpec.Builder builder =
        classBaseInstance(
            GeneratorUtil.createResourceDelegateName(
                getResourceContractContainer().getSourceFileName()));

    if (this.restApiExtension.getTargetFramework() == TargetFramework.MICRONAUT_3) {
      builder.addAnnotation(createAnnotation(JAKARTA_SINGLETON));
    } else {
      builder.addAnnotation(createAnnotation(JAVAX_SINGLETON));
    }

    generateResourceMethods();
    return builder.build();
  }

  @Override
  public TypeName resourceMethodReturnType(Verb verb, Representation representation) {
    String v = toHttpMethod(verb);
    return GeneratorUtil.getMicronautReturnType(
        getResourceContractContainer().getSourceFileName(),
        v,
        verb.getVerb().endsWith("_COLLECTION"),
        getCurrentPackageName(),
        representation);
  }

  @Override
  public void generateMethodNotAllowedStatement(MethodSpec.Builder builder) {
    // not implemented
  }

  @Override
  public ClassName getMethodNowAllowedReturnType() {
    return MICRONAUT_HTTP_RESPONSE.getClassName();
  }

  @Override
  public List<AnnotationSpec> getQueryParamAnnotations(VerbParameter param) {
    List<AnnotationSpec> annotationSpecs = new ArrayList<>();

    if (param.getMandatory()) {
      if (param.isMultiple()) {
        annotationSpecs.add(
            AnnotationSpec.builder(JAVAX_VALIDATION_NOT_EMPTY.getClassName()).build());
      } else {
        annotationSpecs.add(
            AnnotationSpec.builder(JAVAX_VALIDATION_NOT_NULL.getClassName()).build());
      }
    } else {
      annotationSpecs.add(AnnotationSpec.builder(JAVAX_NULLABLE.getClassName()).build());
    }

    if (param.hasMinMaxConstraints() && !param.isMultiple()) {
      AnnotationSpec.Builder sizeAnnotationBuilder =
          AnnotationSpec.builder(JAVAX_VALIDATION_SIZE.getClassName());

      if (param.getMin() != null) {
        sizeAnnotationBuilder.addMember("min", "$L", param.getMin().intValue());
      }

      if (param.getMax() != null) {
        sizeAnnotationBuilder.addMember("max", "$L", param.getMax().intValue());
      }
      annotationSpecs.add(sizeAnnotationBuilder.build());
    }

    AnnotationSpec.Builder queryValueBuilder =
        AnnotationSpec.builder(MICRONAUT_QUERY_VALUE.getClassName());

    if (param.getDefaultValue() != null) {

      if (Objects.equals(param.getType(), "int") && param.getDefaultValue() instanceof Number) {
        queryValueBuilder.addMember(
            "defaultValue", "$S", ((Number) param.getDefaultValue()).intValue());
      } else {
        queryValueBuilder.addMember("defaultValue", "$S", param.getDefaultValue().toString());
      }
    }

    annotationSpecs.add(queryValueBuilder.build());

    if ("date".equals(param.getType())) {
      AnnotationSpec.Builder formatBuilder =
          AnnotationSpec.builder(MICRONAUT_DATE_FORMAT.getClassName());
      annotationSpecs.add(formatBuilder.build());
    } else if ("datetime".equals(param.getType())) {
      AnnotationSpec.Builder formatBuilder =
          AnnotationSpec.builder(MICRONAUT_DATE_TIME_FORMAT.getClassName());
      annotationSpecs.add(formatBuilder.build());
    }

    return annotationSpecs;
  }

  @Override
  public List<AnnotationSpec> getHeaderAnnotations(Header header) {

    List<AnnotationSpec> annotationSpecs = new ArrayList<>();
    annotationSpecs.add(AnnotationSpec.builder(MICRONAUT_HEADER.getClassName()).build());
    return annotationSpecs;
  }

  @Override
  public Iterable<AnnotationSpec> getResourceMethodAnnotations(MethodContext methodContext) {

    List<AnnotationSpec> methodAnnotations = new ArrayList<>();
    String httpMethod = getHttpMethod();
    boolean applyId = isIdGenerationRequired(methodContext);
    Representation representation = methodContext.getRepresentation();

    Map<String, Object> annotationsFields = new HashMap<>();

    if (this.restApiExtension.getGenerationMode() == GenerationMode.CLIENT) {

      if (applyId) {
        annotationsFields.put("value", methodContext.getLinkParser().toBasePath() + "/{id}");
      } else {
        annotationsFields.put("value", methodContext.getLinkParser().toBasePath());
      }

      if (methodContext.isExpandable()) {
        annotationsFields.put("value", annotationsFields.get("value") + "?expands=*");
      }

    } else {
      if (applyId) {
        if (representation.isJson()) {
          annotationsFields.put("uri", "/{id}");
        } else {
          annotationsFields.put("uri", String.format("/{id}.%s", representation.getName()));
        }
      } else {
        if (!representation.isJson()) {
          annotationsFields.put("uri", String.format("/.%s", representation.getName()));
        }
      }

      TargetFramework targetFramework = this.restApiExtension.getTargetFramework();
      if (targetFramework == TargetFramework.MICRONAUT_3
          || targetFramework == TargetFramework.MICRONAUT_24) {

        methodAnnotations.add(createAnnotation(MICRONAUT_EXECUTE_ON, Maps.of("value", "io")));
      }
    }

    switch (httpMethod.toLowerCase()) {
      case "get":
        methodAnnotations.add(createAnnotation(MICRONAUT_GET, annotationsFields));
        break;
      case "options":
        methodAnnotations.add(createAnnotation(MICRONAUT_OPTIONS, annotationsFields));
        break;
      case "head":
        methodAnnotations.add(createAnnotation(MICRONAUT_HEAD, annotationsFields));
        break;
      case "post":
        methodAnnotations.add(createAnnotation(MICRONAUT_POST, annotationsFields));
        break;
      case "put":
        methodAnnotations.add(createAnnotation(MICRONAUT_PUT, annotationsFields));
        break;
      case "delete":
        methodAnnotations.add(createAnnotation(MICRONAUT_DELETE, annotationsFields));
        break;
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported http method %s", httpMethod));
    }
    annotationsFields.clear();

    annotationsFields.put("value", representation.getMimetype());

    if (this.restApiExtension.getGenerationMode() == GenerationMode.CLIENT) {
      methodAnnotations.add(createAnnotation(MICRONAUT_CONSUMES, annotationsFields));
    } else {
      methodAnnotations.add(createAnnotation(MICRONAUT_PRODUCES, annotationsFields));
    }

    List<String> responseStatusRequired =
        Arrays.asList("createEntity", "deleteEntity", "deleteCollection");

    String methodName = methodContext.getMethodName();
    if (responseStatusRequired.contains(methodName)) {

      String v;
      if (methodName.startsWith("create")) {
        v = "$T.CREATED";
      } else if (methodName.startsWith("delete")) {
        v = "$T.NO_CONTENT";
      } else {
        throw new IllegalArgumentException("Unknown method name " + methodName);
      }

      if (!this.restApiExtension.getGenerationMode().isClientCodeGenerationRequired()) {
        AnnotationSpec.Builder b = AnnotationSpec.builder(MICRONAUT_STATUS.getClassName());
        b.addMember("value", v, MICRONAUT_HTTP_STATUS.getClassName());
        methodAnnotations.add(b.build());
      }
    }

    return methodAnnotations;
  }

  @Override
  public PluginTypes getPathVariableAnnotationType() {
    throw new UnsupportedOperationException("Micronaut does not have path variable annotations");
  }

  @Override
  public boolean shouldGenerateHeadMethod() {
    return DELEGATOR_RESOURCE.equals(getArtifactType());
  }

  @Override
  public boolean supportsInterfaces() {
    return false || this.restApiExtension.getGenerationMode() == GenerationMode.CLIENT;
  }

  @Override
  public boolean isHandlerMethod(String methodName) {
    return true;
  }

  @Override
  public boolean providesRequestBodyAnnotation() {
    return true;
  }

  @Override
  public AnnotationSpec buildRequestBodyAnnotation() {
    return createAnnotation(MICRONAUT_REQUEST_BODY);
  }

  @Override
  public boolean supportsMethodNotAllowedGeneration() {
    return false;
  }

  @Override
  public boolean supportsQueryParams() {
    return false;
  }

  @Override
  public boolean supportsDelegation() {
    return true;
  }

  @Override
  public boolean inheritsFromResource() {
    return false;
  }

  @Override
  public void addHeadStatement(
      MethodSpec.Builder methodBuilder, MethodContext context, String params) {
    String nameGetMethod = context.getMethodName().replace("head", "get");

    methodBuilder.addStatement(
        "return $T.buildHeadResponse(this.$L($L), $T.of($S))",
        RESTAPI_RESPONSE_CREATOR.getClassName(),
        nameGetMethod,
        params,
        MICRONAUT_HTTP_MEDIA_TYPE.getClassName(),
        context.getRepresentation().getMimetype().toString());
  }

  @Override
  protected void enhanceResourceModelBaseInstance(Verb verb, TypeSpec.Builder builder) {

    if (POST_METHODS.contains(verb.getVerb()) || PUT_METHODS.contains(verb.getVerb())) {
      builder.addAnnotation(createAnnotation(MICRONAUT_INTROSPECTED));
    }
  }
}
