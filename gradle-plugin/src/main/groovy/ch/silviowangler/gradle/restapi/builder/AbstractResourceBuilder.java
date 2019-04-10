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
package ch.silviowangler.gradle.restapi.builder;

import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_DECIMAL_MAX;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_DECIMAL_MIN;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_EMAIL;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_NOT_NULL;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_VALIDATION_SIZE;
import static ch.silviowangler.gradle.restapi.PluginTypes.RESTAPI_IDENTIFIABLE;
import static ch.silviowangler.gradle.restapi.PluginTypes.RESTAPI_RESOURCE_MODEL;
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
import ch.silviowangler.rest.contract.model.v1.FieldType;
import ch.silviowangler.rest.contract.model.v1.GeneralDetails;
import ch.silviowangler.rest.contract.model.v1.Representation;
import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.ResourceField;
import ch.silviowangler.rest.contract.model.v1.ResourceTypeField;
import ch.silviowangler.rest.contract.model.v1.ResourceTypes;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Silvio Wangler */
public abstract class AbstractResourceBuilder implements ResourceBuilder {

  private TypeSpec.Builder typeBuilder;
  private ResourceContractContainer resourceContractContainer;
  private Verb currentVerb;
  private String currentPackageName;
  private boolean printTimestamp = true;
  private ArtifactType artifactType;
  private Charset responseEncoding;
  private boolean explicitExtensions = false;

  protected void setExplicitExtensions(boolean explicitExtensions) {
    this.explicitExtensions = explicitExtensions;
  }

  public boolean isExplicitExtensions() {
    return explicitExtensions;
  }

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
                paramClasses,
                representation,
                pathParams,
                directEntity);

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

          if (POST.equals(verb.getVerb())) {

            paramClasses.put("model", model);
            context.setMethodName("createEntity");
            methodBuilder = createMethod(context);

          } else if (PUT.equals(verb.getVerb()) || PUT_ENTITY.equals(verb.getVerb())) {

            paramClasses.put("model", model);
            context.setMethodName("updateEntity");
            methodBuilder = createMethod(context);

          } else if (PUT_COLLECTION.equals(verb.getVerb())) {

            paramClasses.put(
                "model", ParameterizedTypeName.get(ClassName.get(Collection.class), model));
            context.setMethodName("updateEntities");
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

      for (ResourceTypeField field : type.getFields()) {

        TypeName fieldType;
        try {
          fieldType = getFieldType(types, field);
        } catch (UnsupportedDataTypeException ex) {
          // handle case where a type contains an enum field
          if (field.isEnumType()) {
            TypeSpec customEnum = buildEnumType(field);
            types.add(ClassName.get(packageName, customEnum.name));
            specTypes.add(customEnum);
            fieldType = getFieldType(types, field);
          } else {
            throw ex;
          }
        }

        if ("true".equals(field.getMultiple())) {
          ClassName list = ClassName.get(List.class);
          fieldType = ParameterizedTypeName.get(list, fieldType);
        }

        builder.addField(FieldSpec.builder(fieldType, field.getName(), PRIVATE).build());

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
    List<Verb> declaredVerbs = resourceContract.getVerbs();
    ensureHeadVerbHasGetVerbCounterpart(declaredVerbs);

    if (declaredVerbs.size() == 1 && declaredVerbs.get(0).getVerb().equals(GET_COLLECTION)) {
      verbs = declaredVerbs;
    } else {

      List<String> excludeVerbs = new ArrayList<>();
      excludeVerbs.add(DELETE_ENTITY);

      Optional<Verb> getEntity =
          declaredVerbs.stream().filter(verb -> verb.getVerb().equals(GET_ENTITY)).findAny();

      if (getEntity.isPresent()) {
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

      List<String> fieldNamesApplied = new ArrayList<>();

      TypeSpec.Builder builder = resourceModelBaseInstance(verb);
      Optional<Representation> jsonRepresentation =
          verbGet.getRepresentations().stream().filter(r -> "json".equals(r.getName())).findAny();

      if (jsonRepresentation.isPresent()) {

        Optional<ResourceField> idField =
            fields.stream().filter(f -> "id".equals(f.getName())).findAny();

        if (idField.isPresent()) {
          builder.addSuperinterface(RESTAPI_IDENTIFIABLE.getTypeName());
        }

        for (ResourceField field : fields) {

          if (!field.isVisible() && verb.equals(verbGet)) continue;
          if (field.isReadonly() && !verb.equals(verbGet)) continue;

          fieldNamesApplied.add(field.getName());

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

          if (!verb.equals(verbGet) && "email".equalsIgnoreCase(field.getType())) {
            fieldBuilder.addAnnotation(
                AnnotationSpec.builder(JAVAX_VALIDATION_EMAIL.getClassName()).build());
          }

          if (!verb.equals(verbGet)
              && (field.getMin() instanceof Number || field.getMax() instanceof Number)) {

            Number min = field.getMin();
            Number max = field.getMax();

            if ("integer".equalsIgnoreCase(field.getType())
                || "string".equalsIgnoreCase(field.getType())) {

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
              fieldBuilder.initializer("$T.now()", ClassName.get(LocalDateTime.class));
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
      }

      if (!fieldNamesApplied.isEmpty()) {
        ClassName resourceModelName = resourceModelName(verb);

        // --> overwrite equals method
        String equalsParamName = "other";
        String equalsCastVarName = "that";

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

        code = "$T.hash(" + String.join(", ", fieldNamesApplied) + ")";

        hashCodeBuilder.addStatement("return " + code, Objects.class);

        builder.addMethod(hashCodeBuilder.build());

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
    return hasVerb(POST);
  }

  private boolean hasPutVerb() {
    return hasVerb(PUT);
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
        TypeSpec.classBuilder(LOWER_CAMEL.to(UPPER_CAMEL, name) + "Type")
            .addModifiers(PUBLIC)
            .addSuperinterface(ClassName.get(Serializable.class));
    return this.typeBuilder = builder;
  }

  private TypeSpec.Builder resourceModelBaseInstance(Verb verb) {
    TypeSpec.Builder builder =
        TypeSpec.classBuilder(resourceModelName(verb))
            .addModifiers(PUBLIC)
            .addAnnotation(createGeneratedAnnotation(printTimestamp))
            .addSuperinterface(Serializable.class)
            .addSuperinterface(RESTAPI_RESOURCE_MODEL.getTypeName());

    return builder;
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
