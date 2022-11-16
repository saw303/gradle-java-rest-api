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
package ch.silviowangler.gradle.restapi.builder;

import static ch.silviowangler.gradle.restapi.PluginTypes.COLLECTION_MODEL;
import static ch.silviowangler.gradle.restapi.PluginTypes.ENTITY_MODEL;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_DECIMAL_MAX;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_DECIMAL_MIN;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_EMAIL;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_MAX;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_MIN;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_NOT_NULL;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_SIZE;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_HTTP_RESPONSE;
import static ch.silviowangler.gradle.restapi.PluginTypes.RESTAPI_IDENTIFIABLE;
import static ch.silviowangler.gradle.restapi.PluginTypes.RESTAPI_RESOURCE_MODEL;
import static ch.silviowangler.gradle.restapi.PluginTypes.VALIDATION_PHONE_NUMBER;
import static ch.silviowangler.gradle.restapi.builder.ArtifactType.CLIENT;
import static ch.silviowangler.gradle.restapi.builder.ArtifactType.RESOURCE;
import static ch.silviowangler.gradle.restapi.util.SupportedDataTypes.BOOL;
import static ch.silviowangler.gradle.restapi.util.SupportedDataTypes.DATE;
import static ch.silviowangler.gradle.restapi.util.SupportedDataTypes.DATETIME;
import static ch.silviowangler.gradle.restapi.util.SupportedDataTypes.STRING;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import ch.silviowangler.gradle.restapi.LinkParser;
import ch.silviowangler.gradle.restapi.UnsupportedDataTypeException;
import ch.silviowangler.rest.contract.model.v1.CustomTypeField;
import ch.silviowangler.rest.contract.model.v1.FieldType;
import ch.silviowangler.rest.contract.model.v1.GeneralDetails;
import ch.silviowangler.rest.contract.model.v1.Representation;
import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.ResourceField;
import ch.silviowangler.rest.contract.model.v1.ResourceTypes;
import ch.silviowangler.rest.contract.model.v1.SubResource;
import ch.silviowangler.rest.contract.model.v1.Verb;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.github.getify.minify.Minify;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Silvio Wangler
 */
public abstract class AbstractResourceBuilder implements ResourceBuilder {

  private TypeSpec.Builder typeBuilder;
  private ResourceContractContainer resourceContractContainer;
  private Verb currentVerb;
  private String currentPackageName;
  private boolean printTimestamp = true;
  private ArtifactType artifactType;
  private Charset responseEncoding;

  private Verb getCurrentVerb() {
    return currentVerb;
  }

  protected void setCurrentVerb(Verb currentVerb) {
    this.currentVerb = currentVerb;
  }

  @Override
  public String getCurrentPackageName() {
    return currentPackageName;
  }

  @Override
  public ArtifactType getArtifactType() {
    return artifactType;
  }

  public void setArtifactType(ArtifactType artifactType) {
    this.artifactType = artifactType;
  }

  protected Charset getResponseEncoding() {
    return responseEncoding;
  }

  @Override
  public ResourceBuilder withResourceContractContainer(ResourceContractContainer resourceContract) {
    this.resourceContractContainer = resourceContract;
    return this;
  }

  @Override
  public ResourceBuilder withTimestampInGeneratedAnnotation(boolean val) {
    printTimestamp = val;
    return this;
  }

  @Override
  public ResourceBuilder withCurrentPackageName(String packageName) {
    this.currentPackageName = packageName;
    return this;
  }

  @Override
  public ResourceBuilder withResponseEncoding(Charset responseEncoding) {
    this.responseEncoding = responseEncoding;
    return this;
  }

  @Override
  public MethodSpec.Builder createMethodNotAllowedHandler(String methodName) {
    Representation representation = Representation.json(this.responseEncoding);

    MethodContext context =
        new MethodContext(methodName, getMethodNowAllowedReturnType(), representation);
    MethodSpec.Builder builder = createMethod(context);
    generateMethodNotAllowedStatement(builder);

    return builder;
  }

  @Override
  public MethodSpec.Builder createMethod(String methodName, TypeName returnType) {
    Representation representation = Representation.json(this.responseEncoding);

    MethodContext context = new MethodContext(methodName, returnType, representation);
    return createMethod(context);
  }

  @Override
  public ResourceContractContainer getResourceContractContainer() {
    return this.resourceContractContainer;
  }

  protected TypeSpec.Builder resourceBaseTypeBuilder() {
    return resourceBaseTypeBuilder(resourceName());
  }

  protected TypeSpec.Builder resourceBaseTypeBuilder(String resourceName) {

    if (this.typeBuilder == null) {

      if (supportsInterfaces()) {
        this.typeBuilder =
            TypeSpec.interfaceBuilder(resourceName)
                .addModifiers(PUBLIC)
                .addAnnotation(createGeneratedAnnotation(printTimestamp));
      } else {
        this.typeBuilder =
            TypeSpec.classBuilder(resourceName)
                .addModifiers(PUBLIC)
                .addAnnotation(createGeneratedAnnotation(printTimestamp));

        if (!supportsDelegation()) {
          this.typeBuilder.addModifiers(ABSTRACT);
        }
      }
      addJavadocToClass();
    }

    return this.typeBuilder;
  }

  private void addJavadocToClass() {
    ResourceContract resourceContract = this.resourceContractContainer.getResourceContract();
    GeneralDetails general = resourceContract.getGeneral();
    if (general != null && general.getDescription() != null) {
      this.typeBuilder.addJavadoc(String.format("%s\n", general.getDescription()));
    }
  }

  protected void reset() {
    this.artifactType = null;
    this.typeBuilder = null;
  }

  protected TypeSpec.Builder classBaseInstance() {
    return classBaseInstance(resourceImplName());
  }

  protected TypeSpec.Builder classBaseInstance(String resourceName) {

    if (this.typeBuilder == null) {
      this.typeBuilder = TypeSpec.classBuilder(resourceName).addModifiers(PUBLIC);
    }
    return this.typeBuilder;
  }

  protected abstract void createOptionsMethod();

  @Override
  public void generateClientMethods() {

    if (artifactType != CLIENT) {
      throw new IllegalStateException("Only available for client generation");
    }

    List<Verb> verbs = getResourceContractContainer().getResourceContract().getVerbs();
    verbs.sort(Comparator.comparing(Verb::getVerb));

    LinkParser parser =
        new LinkParser(
            getResourceContractContainer().getResourceContract().getGeneral().getxRoute(),
            getResourceContractContainer()
                .getResourceContract()
                .getGeneral()
                .getVersion()
                .split("\\.")[0]);

    for (Verb verb : verbs) {

      this.currentVerb = verb;

      if (HEAD_METHODS.contains(verb.getVerb()) && !shouldGenerateHeadMethod()) {
        continue;
      }

      Map<String, TypeName> paramClasses = new HashMap<>();

      for (Representation representation :
          verb.getRepresentations().stream()
              .filter(representation -> Objects.equals(representation.getName(), "json"))
              .collect(Collectors.toList())) {

        boolean directEntity = parser.isDirectEntity();

        List<ParameterSpec> pathParams =
            getPathParams(parser, isAbstractOrInterfaceResource() && !isDelegatorResource());

        TypeName returnType;
        if (DELETE_COLLECTION.equals(verb.getVerb())
            || DELETE_ENTITY.equals(verb.getVerb())
            || POST_ENTITY.equals(verb.getVerb())
            || POST_COLLECTION.equals(verb.getVerb())
            || PUT_ENTITY.equals(verb.getVerb())
            || PUT_COLLECTION.equals(verb.getVerb())) {
          returnType = resourceMethodReturnType(verb, representation);
        } else {
          returnType =
              ParameterizedTypeName.get(
                  GET_COLLECTION.equals(verb.getVerb())
                      ? COLLECTION_MODEL.getClassName()
                      : ENTITY_MODEL.getClassName(),
                  resourceMethodReturnType(
                      GET_COLLECTION.equals(verb.getVerb()) ? new Verb(GET_ENTITY) : verb,
                      representation));
        }

        MethodContext context =
            new MethodContext(
                returnType,
                verb.getParameters(),
                verb.getHeaders(),
                paramClasses,
                representation,
                pathParams,
                parser);

        TypeName rawReturnType;

        if (returnType == TypeName.VOID) {
          rawReturnType = MICRONAUT_HTTP_RESPONSE.getTypeName();
        } else if (returnType == MICRONAUT_HTTP_RESPONSE.getTypeName()) {
          rawReturnType = returnType;
        } else {
          rawReturnType =
              ParameterizedTypeName.get(MICRONAUT_HTTP_RESPONSE.getClassName(), returnType);
        }

        MethodContext contextRaw =
            new MethodContext(
                rawReturnType,
                verb.getParameters(),
                verb.getHeaders(),
                paramClasses,
                representation,
                pathParams,
                parser);

        TypeName hateoasReturnType;
        if (returnType == TypeName.VOID) {
          hateoasReturnType = MICRONAUT_HTTP_RESPONSE.getTypeName();
        } else if (returnType == MICRONAUT_HTTP_RESPONSE.getTypeName()) {
          hateoasReturnType = returnType;
        } else {
          hateoasReturnType =
              ParameterizedTypeName.get(
                  GET_COLLECTION.equals(verb.getVerb())
                      ? COLLECTION_MODEL.getClassName()
                      : ENTITY_MODEL.getClassName(),
                  resourceMethodReturnType(
                      GET_COLLECTION.equals(verb.getVerb()) ? new Verb(GET_ENTITY) : verb,
                      representation));
        }

        MethodContext contextHateoas =
            new MethodContext(
                hateoasReturnType,
                verb.getParameters(),
                verb.getHeaders(),
                paramClasses,
                representation,
                pathParams,
                parser);

        if (GET_COLLECTION.equals(verb.getVerb())) {

          if (directEntity) {
            continue;
          }

          context.setMethodName("getCollectionHateoas");
          this.typeBuilder.addMethod(createMethod(context).build());

          if (resourceContractContainer.getResourceContract().getSubresources().stream()
              .anyMatch(SubResource::isExpandable)) {
            context.setMethodName("getCollectionHateoasExpanded");
            context.setExpandable(true);
            this.typeBuilder.addMethod(createMethod(context).build());
          }

          contextRaw.setMethodName("getCollectionRaw");
          this.typeBuilder.addMethod(createMethod(contextRaw).build());

        } else if (GET_ENTITY.equals(verb.getVerb())) {

          context.setMethodName("getEntityHateoas");
          this.typeBuilder.addMethod(createMethod(context).build());

          if (resourceContractContainer.getResourceContract().getSubresources().stream()
              .anyMatch(SubResource::isExpandable)) {
            context.setMethodName("getEntityHateoasExpanded");
            context.setExpandable(true);
            this.typeBuilder.addMethod(createMethod(context).build());
          }

          contextRaw.setMethodName("getEntityRaw");
          this.typeBuilder.addMethod(createMethod(contextRaw).build());

        } else if (HEAD_COLLECTION.equals(verb.getVerb())) {

          // do nothing

        } else if (HEAD_ENTITY.equals(verb.getVerb())) {

          // do nothing

        } else {
          ClassName model = resourceModelName(verb);

          if (POST.equals(verb.getVerb()) || POST_ENTITY.equals(verb.getVerb())) {

            paramClasses.put("model", model);
            contextHateoas.setMethodName("createEntityHateoas");
            this.typeBuilder.addMethod(createMethod(contextHateoas).build());

            contextRaw.setMethodName("createEntityRaw");
            this.typeBuilder.addMethod(createMethod(contextRaw).build());

          } else if (POST_COLLECTION.equals(verb.getVerb())) {
            paramClasses.put(
                "model", ParameterizedTypeName.get(ClassName.get(Collection.class), model));
            context.setMethodName("createCollectionHateoas");
            this.typeBuilder.addMethod(createMethod(context).build());

            contextRaw.setMethodName("createCollectionRaw");
            this.typeBuilder.addMethod(createMethod(contextRaw).build());

          } else if (PUT.equals(verb.getVerb()) || PUT_ENTITY.equals(verb.getVerb())) {
            paramClasses.put("model", model);
            contextHateoas.setMethodName("updateEntityHateoas");
            this.typeBuilder.addMethod(createMethod(contextHateoas).build());

            contextRaw.setMethodName("updateEntityRaw");
            this.typeBuilder.addMethod(createMethod(contextRaw).build());

          } else if (PUT_COLLECTION.equals(verb.getVerb())) {

            paramClasses.put(
                "model", ParameterizedTypeName.get(ClassName.get(Collection.class), model));
            context.setMethodName("updateCollectionHateoas");
            this.typeBuilder.addMethod(createMethod(context).build());

            contextRaw.setMethodName("updateCollectionRaw");
            this.typeBuilder.addMethod(createMethod(contextRaw).build());

          } else if (DELETE_COLLECTION.equals(verb.getVerb())) {

            context.setMethodName("deleteCollectionHateoas");
            this.typeBuilder.addMethod(createMethod(context).build());

            contextRaw.setMethodName("deleteCollectionRaw");
            this.typeBuilder.addMethod(createMethod(contextRaw).build());

          } else if (DELETE_ENTITY.equals(verb.getVerb())) {

            context.setMethodName("deleteEntityHateoas");
            this.typeBuilder.addMethod(createMethod(context).build());

            contextRaw.setMethodName("deleteEntityRaw");
            this.typeBuilder.addMethod(createMethod(contextRaw).build());

          } else {
            throw new IllegalArgumentException(String.format("Verb %s is unknown", verb.getVerb()));
          }
        }
      }
      this.currentVerb = null;
    }
  }

  @Override
  public void generateResourceMethods() {

    if (isAbstractOrInterfaceResource()) {

      String content = getResourceContractContainer().getResourceContractPlainText();
      content = Minify.minify(content).replaceAll("\"", "\\\\\"");

      FieldSpec.Builder fieldBuilder =
          FieldSpec.builder(ClassName.get(String.class), "OPTIONS_CONTENT")
              .addModifiers(PUBLIC, STATIC, FINAL)
              .initializer("$N", "\"" + content + "\"");

      this.typeBuilder.addField(fieldBuilder.build());

      createOptionsMethod();
    }

    List<Verb> verbs = getResourceContractContainer().getResourceContract().getVerbs();
    verbs.sort(Comparator.comparing(Verb::getVerb));

    LinkParser parser =
        new LinkParser(
            getResourceContractContainer().getResourceContract().getGeneral().getxRoute(),
            getResourceContractContainer()
                .getResourceContract()
                .getGeneral()
                .getVersion()
                .split("\\.")[0]);

    for (Verb verb : verbs) {

      MethodSpec.Builder methodBuilder;

      this.currentVerb = verb;

      if (HEAD_METHODS.contains(verb.getVerb()) && !shouldGenerateHeadMethod()) {
        continue;
      }

      Map<String, TypeName> paramClasses = new HashMap<>();

      for (Representation representation : verb.getRepresentations()) {

        boolean directEntity = parser.isDirectEntity();

        List<ParameterSpec> pathParams =
            getPathParams(parser, isAbstractOrInterfaceResource() && !isDelegatorResource());

        MethodContext context =
            new MethodContext(
                resourceMethodReturnType(verb, representation),
                verb.getParameters(),
                verb.getHeaders(),
                paramClasses,
                representation,
                pathParams,
                parser);

        if (GET_COLLECTION.equals(verb.getVerb())) {

          if (directEntity) {
            continue;
          }

          context.setMethodName("getCollection");
          methodBuilder = createMethod(context);

        } else if (GET_ENTITY.equals(verb.getVerb())) {

          context.setMethodName("getEntity");
          methodBuilder = createMethod(context);

        } else if (HEAD_COLLECTION.equals(verb.getVerb())) {

          if (directEntity) {
            continue;
          }

          context.setMethodName("headCollection");
          methodBuilder = createMethod(context);

        } else if (HEAD_ENTITY.equals(verb.getVerb())) {

          context.setMethodName("headEntity");
          methodBuilder = createMethod(context);

        } else {
          ClassName model = resourceModelName(verb);

          if (POST.equals(verb.getVerb()) || POST_ENTITY.equals(verb.getVerb())) {

            paramClasses.put("model", model);
            context.setMethodName("createEntity");
            methodBuilder = createMethod(context);

          } else if (POST_COLLECTION.equals(verb.getVerb())) {
            paramClasses.put(
                "model", ParameterizedTypeName.get(ClassName.get(Collection.class), model));
            context.setMethodName("createCollection");
            methodBuilder = createMethod(context);

          } else if (PUT.equals(verb.getVerb()) || PUT_ENTITY.equals(verb.getVerb())) {

            paramClasses.put("model", model);
            context.setMethodName("updateEntity");
            methodBuilder = createMethod(context);

          } else if (PUT_COLLECTION.equals(verb.getVerb())) {

            paramClasses.put(
                "model", ParameterizedTypeName.get(ClassName.get(Collection.class), model));
            context.setMethodName("updateCollection");
            methodBuilder = createMethod(context);

          } else if (DELETE_COLLECTION.equals(verb.getVerb())) {

            context.setMethodName("deleteCollection");
            methodBuilder = createMethod(context);

          } else if (DELETE_ENTITY.equals(verb.getVerb())) {

            context.setMethodName("deleteEntity");
            methodBuilder = createMethod(context);

          } else {
            throw new IllegalArgumentException(String.format("Verb %s is unknown", verb.getVerb()));
          }
        }

        MethodSpec resourceMethod = methodBuilder.build();

        if (!supportsInterfaces()
            && !isDelegatorResource()
            && resourceMethod.modifiers.size() == 1
            && resourceMethod.modifiers.contains(PUBLIC)) {

          if (inheritsFromResource()) {
            context.setMethodName(
                String.format("handle%s", LOWER_CAMEL.to(UPPER_CAMEL, context.getMethodName())));
          }
          context.setPathParams(getPathParams(parser, false));
          MethodSpec.Builder handlerBuilder = createMethod(context);
          this.typeBuilder.addMethod(handlerBuilder.build());
        }

        if (supportsInterfaces() || !isResourceImpl()) {
          this.typeBuilder.addMethod(resourceMethod);
        }
      }
      this.currentVerb = null;
    }

    if (isAbstractOrInterfaceResource() && supportsMethodNotAllowedGeneration()) {
      generatedDefaultMethodNotAllowedHandlersForMissingVerbs(parser.isDirectEntity());
    }
  }

  protected String getPath() {
    GeneralDetails general = getResourceContractContainer().getResourceContract().getGeneral();
    return new LinkParser(general.getxRoute(), general.getVersion().split("\\.")[0]).toBasePath();
  }

  protected String getHttpMethod() {
    return toHttpMethod(getCurrentVerb()).toUpperCase();
  }

  @Override
  public Set<TypeSpec> buildResourceTypes(Set<ClassName> types, String packageName) {

    ResourceContract resourceContract = getResourceContractContainer().getResourceContract();
    List<ResourceTypes> contractTypes = resourceContract.getTypes();
    Set<TypeSpec> specTypes = new HashSet<>(types.size());

    for (ResourceTypes type : contractTypes) {
      TypeSpec.Builder builder = resourceTypeBaseInstance(type.getName());

      builder.addAnnotation(createGeneratedAnnotation(this.printTimestamp));

      for (CustomTypeField field : type.getFields()) {

        TypeName fieldType;
        try {
          fieldType = getFieldType(types, field);
        } catch (UnsupportedDataTypeException ex) {
          // handle case where a type contains an enum field
          if (field.isEnumType()) {

            TypeSpec customEnum = buildEnumType(field);
            builder.addType(customEnum);
            fieldType =
                ClassName.get(
                    packageName, resourceTypeName(type.getName()) + "." + customEnum.name);
          } else {
            throw ex;
          }
        }

        if (field.isMultiple()) {
          ClassName list = ClassName.get(List.class);
          fieldType = ParameterizedTypeName.get(list, fieldType);
        }

        FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, field.getName(), PRIVATE);

        if (field.getComment() != null) {
          fieldBuilder.addJavadoc(field.getComment());
        }

        builder.addField(fieldBuilder.build());

        // write Getter/Setters
        writeGetterSetter(builder, fieldType, field.getName());
      }
      TypeSpec typeSpec = builder.build();
      types.add(ClassName.get(packageName, typeSpec.name));
      specTypes.add(typeSpec);
    }

    // Generate Java enums for fields...
    List<ResourceField> enumFields =
        resourceContract.getFields().stream()
            .filter(field -> "enum".equals(field.getType()))
            .collect(Collectors.toList());

    for (FieldType enumField : enumFields) {
      TypeSpec customEnum = buildEnumType(enumField);
      types.add(ClassName.get(packageName, customEnum.name));
      specTypes.add(customEnum);
    }
    return specTypes;
  }

  @SuppressWarnings("unchecked")
  private TypeSpec buildEnumType(FieldType enumField) {
    TypeSpec.Builder enumBuilder =
        TypeSpec.enumBuilder(
                LOWER_CAMEL.to(UPPER_CAMEL, String.format("%sType", enumField.getName())))
            .addModifiers(PUBLIC);

    if (enumField.getOptions() instanceof Iterable) {
      Iterable<String> values = (Iterable<String>) enumField.getOptions();
      for (String value : values) {
        enumBuilder.addEnumConstant(value);
      }
    } else {
      throw new IllegalStateException(
          String.format("enum field %s must contain a list in options field", enumField.getName()));
    }
    enumBuilder.addAnnotation(createGeneratedAnnotation(this.printTimestamp));
    return enumBuilder.build();
  }

  @Override
  public Set<TypeSpec> buildResourceModels(Set<ClassName> types) {
    ResourceContract resourceContract = getResourceContractContainer().getResourceContract();

    List<Verb> verbs;
    List<Verb> declaredVerbs =
        resourceContract.getVerbs().stream()
            .filter(Verb::containsRepresentationJson)
            .collect(Collectors.toList());
    ensureHeadVerbHasGetVerbCounterpart(declaredVerbs);

    if (declaredVerbs.size() == 1 && GET_COLLECTION.equals(declaredVerbs.get(0).getVerb())) {
      verbs = declaredVerbs;
    } else {

      List<String> excludeVerbs = new ArrayList<>();
      excludeVerbs.add(DELETE_ENTITY);

      if (declaredVerbs.stream().anyMatch(verb -> GET_ENTITY.equals(verb.getVerb()))) {
        excludeVerbs.add(GET_COLLECTION);
      }

      verbs =
          declaredVerbs.stream()
              .filter(v -> !excludeVerbs.contains(v.getVerb()))
              .collect(Collectors.toList());
    }

    Set<TypeSpec> specTypes = new HashSet<>(verbs.size());

    Verb verbGet =
        verbs.stream()
            .filter(v -> v.getVerb().equals(GET_ENTITY))
            .findAny()
            .orElse(verbs.isEmpty() ? null : verbs.get(0));

    List<ResourceField> fields = resourceContract.getFields();

    for (Verb verb : verbs) {

      if (HEAD_METHODS.contains(verb.getVerb())) {
        continue;
      }

      List<ResourceField> fieldNamesApplied = new ArrayList<>();

      TypeSpec.Builder builder = resourceModelBaseInstance(verb);

      if (fields.stream().anyMatch(f -> "id".equals(f.getName()))) {
        builder.addSuperinterface(RESTAPI_IDENTIFIABLE.getTypeName());
      }

      // add default constructor
      builder.addMethod(MethodSpec.constructorBuilder().addModifiers(PUBLIC).build());

      for (ResourceField field : fields) {

        if (!field.isVisible() && verb.equals(verbGet)) continue;
        if (field.isReadonly() && !verb.equals(verbGet)) continue;

        fieldNamesApplied.add(field);

        TypeName fieldType = getFieldType(types, field);

        if (field.isMultiple()) {
          ClassName list = ClassName.get(List.class);
          fieldType = ParameterizedTypeName.get(list, fieldType);
        }

        FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, field.getName(), PRIVATE);

        if (field.getxComment() != null) {
          fieldBuilder.addJavadoc(String.format("%s\n", field.getxComment()));
        }

        if (field.getMandatory().stream().anyMatch(v -> v.equalsIgnoreCase(verb.getVerb()))) {
          fieldBuilder.addAnnotation(createAnnotation(JAVAX_VALIDATION_NOT_NULL));
        }

        boolean isEntityGet = hasGetEntityVerb() && verb.equals(verbGet);

        if (!isEntityGet && "email".equalsIgnoreCase(field.getType())) {
          fieldBuilder.addAnnotation(
              AnnotationSpec.builder(JAVAX_VALIDATION_EMAIL.getClassName()).build());
        }

        if (!isEntityGet && "phoneNumber".equalsIgnoreCase(field.getType())) {
          fieldBuilder.addAnnotation(
              AnnotationSpec.builder(VALIDATION_PHONE_NUMBER.getClassName()).build());
        }

        if (!isEntityGet && (field.getMin() != null || field.getMax() != null)) {

          Number min = field.getMin();
          Number max = field.getMax();

          if ("string".equalsIgnoreCase(field.getType())) {

            AnnotationSpec.Builder annoBuilder =
                AnnotationSpec.builder(JAVAX_VALIDATION_SIZE.getClassName());

            if (field.getMin() != null) {
              annoBuilder.addMember("min", "$L", min.intValue());
            }

            if (field.getMax() != null) {
              annoBuilder.addMember("max", "$L", max.intValue());
            }
            fieldBuilder.addAnnotation(annoBuilder.build());

          } else if ("decimal".equalsIgnoreCase(field.getType())) {
            fieldBuilder.addAnnotation(
                AnnotationSpec.builder(JAVAX_VALIDATION_DECIMAL_MIN.getClassName())
                    .addMember("value", "$S", min.doubleValue())
                    .build());
            fieldBuilder.addAnnotation(
                AnnotationSpec.builder(JAVAX_VALIDATION_DECIMAL_MAX.getClassName())
                    .addMember("value", "$S", max.doubleValue())
                    .build());
          } else if ("int".equalsIgnoreCase(field.getType())) {
            fieldBuilder.addAnnotation(
                AnnotationSpec.builder(JAVAX_VALIDATION_MIN.getClassName())
                    .addMember("value", "$L", min.intValue())
                    .build());
            fieldBuilder.addAnnotation(
                AnnotationSpec.builder(JAVAX_VALIDATION_MAX.getClassName())
                    .addMember("value", "$L", max.intValue())
                    .build());
          }
        }

        if (field.isMultiple()) {
          fieldBuilder.initializer("new java.util.ArrayList<>()");
        } else if (field.getDefaultValue() != null) {

          if (fieldType == STRING.getClassName()) {
            fieldBuilder.initializer("$S", field.getDefaultValue());
          } else if (fieldType == DATE.getClassName()) {
            fieldBuilder.initializer("$T.now()", ClassName.get(LocalDate.class));
          } else if (fieldType == DATETIME.getClassName()) {
            fieldBuilder.initializer("$T.now()", ClassName.get(Instant.class));
          } else if (fieldType == BOOL.getClassName()) {
            fieldBuilder.initializer(
                "$T.$L",
                ClassName.get(Boolean.class),
                Boolean.TRUE.equals(field.getDefaultValue()) ? "TRUE" : "FALSE");
          } else {
            fieldBuilder.initializer("$S", field.getDefaultValue());
          }
        }
        builder.addField(fieldBuilder.build());

        // write Getter/Setters
        writeGetterSetter(builder, fieldType, field.getName());
      }

      if (!fieldNamesApplied.isEmpty()) {
        ClassName resourceModelName = resourceModelName(verb);

        // --> overwrite equals method
        final String equalsParamName = "other";
        final String equalsCastVarName = "that";

        MethodSpec.Builder equalsBuilder =
            MethodSpec.methodBuilder("equals")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(Object.class, equalsParamName)
                .returns(TypeName.BOOLEAN);

        equalsBuilder
            .addStatement("if (this == $L) return true", equalsParamName)
            .addStatement(
                "if (! ($L instanceof $T)) return false", equalsParamName, resourceModelName)
            .addStatement(
                "$T $L = ($T) $L",
                resourceModelName,
                equalsCastVarName,
                resourceModelName,
                equalsParamName);

        String code =
            fieldNamesApplied.stream()
                .map(ResourceField::getName)
                .map(f -> "get" + LOWER_CAMEL.to(UPPER_CAMEL, f))
                .map(f -> "$T.equals(" + f + "(), " + equalsCastVarName + "." + f + "())")
                .collect(Collectors.joining(" && "));

        equalsBuilder.addStatement(
            "return " + code,
            Collections.nCopies(fieldNamesApplied.size(), Objects.class).toArray());

        builder.addMethod(equalsBuilder.build());

        // --> overwrite hashCode method
        MethodSpec.Builder hashCodeBuilder =
            MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .returns(INT);

        code =
            "$T.hash("
                + String.join(
                    ", ",
                    fieldNamesApplied.stream()
                        .map(ResourceField::getName)
                        .collect(Collectors.toList()))
                + ")";

        hashCodeBuilder.addStatement("return " + code, Objects.class);

        builder.addMethod(hashCodeBuilder.build());

        // fully qualified constructor
        MethodSpec.Builder constructorBuilder =
            MethodSpec.constructorBuilder().addModifiers(PUBLIC);

        fieldNamesApplied.forEach(
            field -> {
              TypeName fieldType = getFieldType(types, field);
              if (field.isMultiple()) {
                ClassName list = ClassName.get(List.class);
                fieldType = ParameterizedTypeName.get(list, fieldType);
              }

              constructorBuilder.addParameter(fieldType, field.getName());
              constructorBuilder.addStatement("this.$N = $N", field.getName(), field.getName());
            });
        builder.addMethod(constructorBuilder.build());

        specTypes.add(builder.build());
      }
    }
    return specTypes;
  }

  private void ensureHeadVerbHasGetVerbCounterpart(List<Verb> verbs) {
    Set<String> headRepresentations = new TreeSet<>();
    Set<String> getRepresentations = new TreeSet<>();

    for (Verb verb : verbs) {
      String verbName = verb.getVerb();
      for (Representation representation : verb.getRepresentations()) {
        String verbRepresentation =
            String.format("Verb: [%s] Representation: [%s]", verbName, representation.getName());

        if (HEAD_METHODS.contains(verbName)) {
          headRepresentations.add(verbRepresentation);
        } else if (GET_METHODS.contains(verbName)) {
          getRepresentations.add(verbRepresentation);
        }
      }
    }

    for (String headRepresentation : headRepresentations) {
      if (!getRepresentations.contains(headRepresentation.replace("HEAD", "GET"))) {
        throw new IllegalStateException(
            String.format("%s has no GET counterpart", headRepresentation));
      }
    }
  }

  private TypeName getFieldType(Set<ClassName> types, FieldType resourceField) {
    TypeName type;
    try {
      type = JavaTypeRegistry.translateToJava(resourceField);
    } catch (UnsupportedDataTypeException ex) {

      Stream<ClassName> typeStream;

      if (resourceField.isEnumType()) {
        typeStream =
            types.stream()
                .filter(
                    t ->
                        t.packageName().equals(this.currentPackageName)
                            && t.simpleName()
                                .equals(
                                    String.format(
                                        "%sType",
                                        LOWER_CAMEL.to(UPPER_CAMEL, resourceField.getName()))));
      } else {
        typeStream =
            types.stream()
                .filter(t -> t.simpleName().equalsIgnoreCase(resourceField.getType() + "Type"));
      }
      return typeStream.findAny().orElseThrow(() -> ex);
    }
    return type;
  }

  private void writeGetterSetter(TypeSpec.Builder builder, TypeName fieldType, String name) {
    String methodName = LOWER_CAMEL.to(UPPER_CAMEL, name);
    MethodSpec.Builder getterBuilder =
        MethodSpec.methodBuilder("get" + methodName)
            .returns(fieldType)
            .addModifiers(PUBLIC)
            .addStatement("return this.$L", name);

    builder.addMethod(getterBuilder.build());
    MethodSpec.Builder setterBuilder =
        MethodSpec.methodBuilder("set" + methodName)
            .returns(TypeName.VOID)
            .addModifiers(PUBLIC)
            .addParameter(ParameterSpec.builder(fieldType, name).build())
            .addStatement("this.$L = $L", name, name);
    builder.addMethod(setterBuilder.build());
  }

  private boolean isResourceInterface() {
    return RESOURCE.equals(getArtifactType());
  }

  private boolean isAbstractResourceInterface() {
    return ArtifactType.ABSTRACT_RESOURCE.equals(getArtifactType());
  }

  private boolean isDelegatorResource() {
    return ArtifactType.DELEGATOR_RESOURCE.equals(getArtifactType());
  }

  private boolean isAbstractOrInterfaceResource() {
    return isAbstractResourceInterface() || isResourceInterface() || isDelegatorResource();
  }

  private boolean isResourceImpl() {
    return ArtifactType.RESOURCE_IMPL.equals(getArtifactType());
  }

  private void generatedDefaultMethodNotAllowedHandlersForMissingVerbs(boolean directEntity) {

    if (!hasPostVerb()) {
      this.currentVerb = new Verb(POST);
      this.typeBuilder.addMethod(createMethodNotAllowedHandler("createEntityAutoAnswer").build());
    }

    if (!hasDeleteCollectionVerb()) {
      this.currentVerb = new Verb(DELETE_COLLECTION);
      this.typeBuilder.addMethod(
          createMethodNotAllowedHandler("deleteCollectionAutoAnswer").build());
    }

    if (!hasDeleteEntityVerb()) {
      this.currentVerb = new Verb(DELETE_ENTITY);
      this.typeBuilder.addMethod(createMethodNotAllowedHandler("deleteEntityAutoAnswer").build());
    }

    if (!hasGetCollectionVerb() && !directEntity) {
      this.currentVerb = new Verb(GET_COLLECTION);
      this.typeBuilder.addMethod(createMethodNotAllowedHandler("getCollectionAutoAnswer").build());
    } else if (!hasGetEntityVerb()) {
      this.currentVerb = new Verb(GET_ENTITY);
      this.typeBuilder.addMethod(createMethodNotAllowedHandler("getEntityAutoAnswer").build());
    }

    if (shouldGenerateHeadMethod()) {
      if (!hasHeadCollectionVerb() && !directEntity) {
        this.currentVerb = new Verb(HEAD_COLLECTION);
        this.typeBuilder.addMethod(
            createMethodNotAllowedHandler("headCollectionAutoAnswer").build());
      } else if (!hasHeadEntityVerb()) {
        this.currentVerb = new Verb(HEAD_ENTITY);
        this.typeBuilder.addMethod(createMethodNotAllowedHandler("headEntityAutoAnswer").build());
      }
    }

    if (!hasPutVerb()) {
      this.currentVerb = new Verb(PUT);
      this.typeBuilder.addMethod(createMethodNotAllowedHandler("updateEntityAutoAnswer").build());
    }
    this.currentVerb = null;
  }

  private boolean hasGetEntityVerb() {
    return hasVerb(GET_ENTITY);
  }

  private boolean hasGetCollectionVerb() {
    return hasVerb(GET_COLLECTION);
  }

  private boolean hasHeadEntityVerb() {
    return hasVerb(HEAD_ENTITY);
  }

  private boolean hasHeadCollectionVerb() {
    return hasVerb(HEAD_COLLECTION);
  }

  private boolean hasPostVerb() {
    return hasVerb(POST) || hasVerb(POST_COLLECTION) || hasVerb(POST_ENTITY);
  }

  private boolean hasPutVerb() {
    return hasVerb(PUT) || hasVerb(PUT_COLLECTION) || hasVerb(PUT_ENTITY);
  }

  private boolean hasDeleteCollectionVerb() {
    return hasVerb(DELETE_COLLECTION);
  }

  private boolean hasDeleteEntityVerb() {
    return hasVerb(DELETE_ENTITY);
  }

  private boolean hasVerb(String verb) {
    return getResourceContractContainer().getResourceContract().getVerbs().stream()
        .filter(v -> verb.equals(v.getVerb()))
        .findAny()
        .isPresent();
  }

  private TypeSpec.Builder resourceTypeBaseInstance(String name) {
    TypeSpec.Builder builder =
        TypeSpec.classBuilder(resourceTypeName(name))
            .addModifiers(PUBLIC)
            .addSuperinterface(ClassName.get(Serializable.class));
    return this.typeBuilder = builder;
  }

  private String resourceTypeName(String name) {
    return LOWER_CAMEL.to(UPPER_CAMEL, name) + "Type";
  }

  private TypeSpec.Builder resourceModelBaseInstance(Verb verb) {
    TypeSpec.Builder builder =
        TypeSpec.classBuilder(resourceModelName(verb))
            .addModifiers(PUBLIC)
            .addAnnotation(createGeneratedAnnotation(printTimestamp))
            .addSuperinterface(Serializable.class)
            .addSuperinterface(RESTAPI_RESOURCE_MODEL.getTypeName());

    enhanceResourceModelBaseInstance(verb, builder);
    return builder;
  }

  /**
   * Can be used to add additional annotations or method to a resource model class by overwriting
   * this method. The base implementation is NO-OP.
   *
   * @param verb the verb (e.g. GET_ENTITY, POST, PUT)
   * @param builder the initial resource model builder that can be enhanced.
   * @since 2.0.23
   */
  protected void enhanceResourceModelBaseInstance(Verb verb, TypeSpec.Builder builder) {
    // noop
  }

  private List<ParameterSpec> getPathParams(LinkParser parser, boolean addAnnotations) {
    List<ParameterSpec> pathParams = new ArrayList<>(parser.getPathVariables().size());

    for (String pathVar : parser.getPathVariables()) {
      ParameterSpec.Builder paramBuilder = ParameterSpec.builder(String.class, pathVar);

      if (addAnnotations) {
        paramBuilder.addAnnotation(
            AnnotationSpec.builder(getPathVariableAnnotationType().getClassName())
                .addMember("value", "$S", pathVar)
                .build());
      }
      pathParams.add(paramBuilder.build());
    }
    return pathParams;
  }
}
