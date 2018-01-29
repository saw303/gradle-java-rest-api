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

import ch.silviowangler.gradle.restapi.GenerateRestApiTask;
import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.LinkParser;
import ch.silviowangler.gradle.restapi.PluginTypes;
import ch.silviowangler.rest.contract.model.v1.*;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import io.github.getify.minify.Minify;

import javax.lang.model.element.Modifier;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.*;

/**
 * @author Silvio Wangler
 */
public abstract class AbstractResourceBuilder implements ResourceBuilder {

    protected TypeSpec.Builder typeBuilder;
    private ResourceContractContainer resourceContractContainer;
    private Verb currentVerb;
    private String currentPackageName;
    private boolean printTimestamp = true;
    private ArtifactType artifactType;


    public Verb getCurrentVerb() {
        return currentVerb;
    }

    protected void setCurrentVerb(Verb currentVerb) {
        this.currentVerb = currentVerb;
    }

    public String getCurrentPackageName() {
        return currentPackageName;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(ArtifactType artifactType) {
        this.artifactType = artifactType;
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
    public ResourceContractContainer getResourceContractContainer() {
        return this.resourceContractContainer;
    }

    protected TypeSpec.Builder interfaceBaseInstance() {

        if (this.typeBuilder == null) {
            this.typeBuilder = TypeSpec.interfaceBuilder(resourceName())
                    .addModifiers(PUBLIC)
                    .addAnnotation(createGeneratedAnnotation(printTimestamp));
        }
        return this.typeBuilder;
    }

    protected TypeSpec.Builder resourceTypeBaseInstance(String name) {
        TypeSpec.Builder builder = TypeSpec
                .classBuilder(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name) + "Type")
                .addModifiers(PUBLIC)
                .addSuperinterface(ClassName.get(Serializable.class));
        return this.typeBuilder = builder;
    }

    protected TypeSpec.Builder resourceModelBaseInstance(Verb verb) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(resourceModelName(verb))
                .addModifiers(PUBLIC)
                .addAnnotation(createGeneratedAnnotation(printTimestamp))
                .addSuperinterface(Serializable.class);

        return builder;
    }

    protected void reset() {
        this.artifactType = null;
        this.typeBuilder = null;
    }

    protected TypeSpec.Builder classBaseInstance() {

        if (this.typeBuilder == null) {
            this.typeBuilder = TypeSpec.classBuilder(resourceImplName()).addModifiers(PUBLIC);
        }
        return this.typeBuilder;
    }

    protected abstract void createOptionsMethod();

    @Override
    public void generateResourceMethods() {

        if (isResourceInterface()) {

            String content = getResourceContractContainer().getResourceContractPlainText();
            content = Minify.minify(content).replaceAll("\"", "\\\\\"");

            FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.get(String.class), "OPTIONS_CONTENT").addModifiers(PUBLIC, STATIC, FINAL)
                    .initializer("$N", "\"" + content + "\"");

            this.typeBuilder.addField(fieldBuilder.build());

            createOptionsMethod();
        }

        List<Verb> verbs = getResourceContractContainer().getResourceContract().getVerbs();
        Collections.sort(verbs, Comparator.comparing(Verb::getVerb));

        for (Verb verb : verbs) {

            MethodSpec.Builder methodBuilder;

            Map<String, ClassName> params = new HashMap<>();
            this.currentVerb = verb;

            verb.getParameters().forEach(p -> params.put(p.getName(), GeneratorUtil.translateToJava(p.getType())));

            for (Representation representation : verb.getRepresentations()) {

                if (GenerateRestApiTask.GET_COLLECTION.equals(verb.getVerb())) {

                    methodBuilder = createMethod(
                            "getCollection",
                            resourceMethodReturnType(verb, representation),
                            params,
                            representation
                    );

                } else if (GenerateRestApiTask.GET_ENTITY.equals(verb.getVerb())) {


                    methodBuilder = createMethod(
                            "getEntity",
                            resourceMethodReturnType(verb,representation),
                            params,
                            representation
                    );

                    methodBuilder.addParameter(generateIdParam());


                } else if (GenerateRestApiTask.POST.equals(verb.getVerb())) {

                    methodBuilder = createMethod(
                            "createEntity",
                            resourceMethodReturnType(verb, representation),
                            params,
                            representation
                    );

                    ParameterSpec.Builder param = ParameterSpec.builder(resourceModelName(verb), "model");
                    if (getArtifactType().equals(ArtifactType.RESOURCE)) {
                        param.addAnnotation(
                                createAnnotation(PluginTypes.JAVAX_VALIDATION_VALID)
                        ).build();
                    }
                    methodBuilder.addParameter(param.build());

                } else if (GenerateRestApiTask.PUT.equals(verb.getVerb())) {

                    methodBuilder = createMethod(
                            "updateEntity",
                            resourceMethodReturnType(verb, representation),
                            params,
                            representation
                    );

                    ParameterSpec.Builder param = ParameterSpec.builder(resourceModelName(verb), "model");
                    if (getArtifactType().equals(ArtifactType.RESOURCE)) {
                        param.addAnnotation(
                                createAnnotation(PluginTypes.JAVAX_VALIDATION_VALID)
                        ).build();
                    }
                    methodBuilder.addParameter(param.build());

                    param = ParameterSpec.builder(ClassName.get(String.class), "id");

                    if (getArtifactType().equals(ArtifactType.RESOURCE)) {

                        Map<String, Object> attrs = new HashMap<>();
                        attrs.put("value", "id");

                        param.addAnnotation(
                                createAnnotation(getPathVariableAnnotationType(), attrs)
                        ).build();
                    }
                    methodBuilder.addParameter(param.build());


                } else if (GenerateRestApiTask.DELETE_COLLECTION.equals(verb.getVerb())) {

                    methodBuilder = createMethod(
                            "deleteCollection",
                            resourceMethodReturnType(verb, representation),
                            params,
                            representation
                    );

                } else if (GenerateRestApiTask.DELETE_ENTITY.equals(verb.getVerb())) {

                    methodBuilder = createMethod(
                            "deleteEntity",
                            resourceMethodReturnType(verb, representation),
                            params,
                            representation
                    );

                } else {
                    throw new IllegalArgumentException(String.format("Verb %s is unknown", verb.getVerb()));
                }
                this.typeBuilder.addMethod(methodBuilder.build());
            }
            this.currentVerb = null;
        }


        if (isResourceInterface()) {
            generatedDefaultMethodNotAllowedHandlersForMissingVerbs();
        }

    }


    protected boolean isResourceInterface() {
        return ArtifactType.RESOURCE.equals(getArtifactType());
    }

    private void generatedDefaultMethodNotAllowedHandlersForMissingVerbs() {


        if (!hasPostVerb()) {
            this.currentVerb = new Verb(GenerateRestApiTask.POST);
            this.typeBuilder.addMethod(createMethodNotAllowedHandler("createEntityAutoAnswer").build());
        }

        if (!hasDeleteCollectionVerb()) {
            this.currentVerb = new Verb(GenerateRestApiTask.DELETE_COLLECTION);
            this.typeBuilder.addMethod(createMethodNotAllowedHandler("deleteCollectionAutoAnswer").build());
        }

        if (!hasDeleteEntityVerb()) {
            this.currentVerb = new Verb(GenerateRestApiTask.DELETE_ENTITY);
            this.typeBuilder.addMethod(createMethodNotAllowedHandler("deleteEntityAutoAnswer").build());
        }

        if (!hasGetCollectionVerb()) {
            this.currentVerb = new Verb(GenerateRestApiTask.GET_COLLECTION);
            this.typeBuilder.addMethod(createMethodNotAllowedHandler("getCollectionAutoAnswer").build());
        } else if (!hasGetEntityVerb()) {
            this.currentVerb = new Verb(GenerateRestApiTask.GET_ENTITY);
            this.typeBuilder.addMethod(createMethodNotAllowedHandler("getEntityAutoAnswer").build());
        }

        if (!hasPutVerb()) {
            this.currentVerb = new Verb(GenerateRestApiTask.PUT);
            this.typeBuilder.addMethod(createMethodNotAllowedHandler("updateEntityAutoAnswer").build());
        }
        this.currentVerb = null;
    }

    private boolean hasGetEntityVerb() {
        return hasVerb(GenerateRestApiTask.GET_ENTITY);
    }

    private boolean hasGetCollectionVerb() {
        return hasVerb(GenerateRestApiTask.GET_COLLECTION);
    }

    private boolean hasPostVerb() {
        return hasVerb(GenerateRestApiTask.POST);
    }

    private boolean hasPutVerb() {
        return hasVerb(GenerateRestApiTask.PUT);
    }

    private boolean hasDeleteCollectionVerb() {
        return hasVerb(GenerateRestApiTask.DELETE_COLLECTION);
    }

    private boolean hasDeleteEntityVerb() {
        return hasVerb(GenerateRestApiTask.DELETE_ENTITY);
    }

    private boolean hasVerb(String verb) {
        return getResourceContractContainer().getResourceContract().getVerbs().stream().filter(v -> verb.equals(v.getVerb())).findAny().isPresent();
    }

    protected String getPath() {
        return new LinkParser(
                getResourceContractContainer().getResourceContract().getGeneral().getxRoute(),
                getResourceContractContainer().getResourceContract().getGeneral().getVersion().split("\\.")[0]
        ).toBasePath();
    }

    protected String getHttpMethod() {

        String v = Objects.requireNonNull(getCurrentVerb()).getVerb();

        if (GenerateRestApiTask.GET_ENTITY.equals(v) || GenerateRestApiTask.GET_COLLECTION.equals(v)) {
            return "GET";
        } else if (GenerateRestApiTask.DELETE_ENTITY.equals(v) || GenerateRestApiTask.DELETE_COLLECTION.equals(v)) {
            return "DELETE";
        } else if (GenerateRestApiTask.PUT.equals(v)) {
            return "PUT";
        } else if (GenerateRestApiTask.POST.equals(v)) {
            return "POST";
        } else if ("OPTIONS".equals(v)) {
            return v;
        } else {
            throw new IllegalArgumentException("Unknown verb " + v);
        }
    }

    @Override
    public Set<TypeSpec> buildResourceTypes(Set<ClassName> types) {

        ResourceContract resourceContract = getResourceContractContainer().getResourceContract();
        List<ResourceTypes> contractTypes = resourceContract.getTypes();
        Set<TypeSpec> specTypes = new HashSet<>(types.size());

        for (ResourceTypes type : contractTypes) {
            TypeSpec.Builder builder = resourceTypeBaseInstance(type.getName());

            for (ResourceTypeField field : type.getFields()) {

                TypeName fieldType = getFieldType(types, field.getType());

                builder.addField(FieldSpec.builder(fieldType, field.getName(), PRIVATE).build());

                // Getter/Setters schreiben
                writeGetterSetter(builder, fieldType, field.getName());
            }
            specTypes.add(builder.build());
        }
        return specTypes;
    }


    @Override
    public Set<TypeSpec> buildResourceModels(Set<ClassName> types) {
        ResourceContract resourceContract = getResourceContractContainer().getResourceContract();
        List<Verb> verbs = resourceContract.getVerbs().stream().filter( v -> !v.getVerb().equals(DELETE_ENTITY)).collect(Collectors.toList());
        Set<TypeSpec> specTypes = new HashSet<>(verbs.size());


        Verb verbGet = verbs.stream().filter(v -> v.getVerb().equals(GET_ENTITY)).findAny().orElse(verbs.get(0));

        List<ResourceField> fields = resourceContract.getFields().stream().filter(f -> f.isVisible()).collect(Collectors.toList());

        List<String> fieldNames = fields.stream().filter(f -> f.isVisible()).map(ResourceField::getName).collect(Collectors.toList());

        List<String> getters = fieldNames.stream().map(name -> "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name))
                .collect(Collectors.toList());

        for (Verb verb : verbs) {

            ClassName resourceModelName = resourceModelName(verb);
            TypeSpec.Builder builder = resourceModelBaseInstance(verb);
            String mimeType = verbGet.getRepresentations().stream().filter(r -> "json".equals(r.getName())).findAny().get().getMimetype();

            builder.addField(
                    FieldSpec.builder(String.class, "TYPE")
                            .addModifiers(PUBLIC, FINAL, STATIC)
                            .initializer("$S", mimeType)
                            .build()
            );

            for (ResourceField field : fields) {
                TypeName fieldType = getFieldType(types, field.getType());

                if (field.isMultiple()) {
                    ClassName list = ClassName.get(List.class);
                    fieldType = ParameterizedTypeName.get(list, fieldType);
                }

                FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, field.getName(), PRIVATE);

                if (field.getMandatory().stream().filter(v -> v.equalsIgnoreCase(verb.getVerb())).findAny().isPresent()) {
                    fieldBuilder.addAnnotation(createAnnotation(PluginTypes.JAVAX_VALIDATION_NOT_NULL));
                }

                builder.addField(fieldBuilder.build());

                // Getter/Setters schreiben
                writeGetterSetter(builder, fieldType, field.getName());
            }

            // --> equals Methode überschreiben
            String equalsParamName = "other";
            String equalsCastVarName = "that";

            MethodSpec.Builder equalsBuilder = MethodSpec.methodBuilder("equals")
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC).addParameter(Object.class, equalsParamName).returns(TypeName.BOOLEAN);

            equalsBuilder.addStatement("if (this == $L) return true", equalsParamName)
                    .addStatement("if (! ($L instanceof $T)) return false", equalsParamName, resourceModelName)
                    .addStatement("$T $L = ($T) $L", resourceModelName, equalsCastVarName, resourceModelName, equalsParamName);


            String code = getters.stream().map(getter -> "$T.equals(" + getter + "(), " + equalsCastVarName + "." + getter + "())")
                    .collect(Collectors.joining(" && "));

            equalsBuilder.addStatement("return " + code, Collections.nCopies(getters.size(), Objects.class).toArray());

            builder.addMethod(equalsBuilder.build());


            // --> hashCode Methode überschreiben
            MethodSpec.Builder hashCodeBuilder = MethodSpec.methodBuilder("hashCode")
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC).returns(TypeName.INT);

            code = "$T.hash(" + fieldNames.stream().collect(Collectors.joining(", ")) + ")";

            hashCodeBuilder.addStatement("return " + code, Objects.class);

            builder.addMethod(hashCodeBuilder.build());

            specTypes.add(builder.build());
        }
        return specTypes;
    }

    private TypeName getFieldType(Set<ClassName> types, String fieldType) {
        TypeName type;
        try {
            type = GeneratorUtil.translateToJava(fieldType);
        } catch (Exception e) {
            Optional<ClassName> any = types.stream().filter(t -> t.simpleName().equalsIgnoreCase(fieldType + "Type")).findAny();

            if (!any.isPresent()) {
                throw e;
            }
            type = any.get();
        }
        return type;
    }

    private void writeGetterSetter(TypeSpec.Builder builder, TypeName fieldType, String name) {
        String methodName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name);
        MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder("get" + methodName)
                .returns(fieldType).addModifiers(Modifier.PUBLIC).addStatement("return this.$L", name);

        builder.addMethod(getterBuilder.build());
        MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder("set" + methodName)
                .returns(TypeName.VOID)
                .addModifiers(PUBLIC)
                .addParameter(ParameterSpec.builder(fieldType, name).build())
                .addStatement("this.$L = $L", name, name);
        builder.addMethod(setterBuilder.build());
    }
}
