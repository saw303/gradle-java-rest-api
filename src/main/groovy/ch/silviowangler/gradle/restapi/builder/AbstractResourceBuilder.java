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

import ch.silviowangler.gradle.restapi.AnnotationTypes;
import ch.silviowangler.gradle.restapi.GenerateRestApiTask;
import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.LinkParser;
import ch.silviowangler.gradle.restapi.gson.GeneralDetailsDeserializer;
import ch.silviowangler.rest.contract.model.v1.GeneralDetails;
import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.Verb;
import com.google.gson.GsonBuilder;
import com.squareup.javapoet.*;
import io.github.getify.minify.Minify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static javax.lang.model.element.Modifier.*;

/**
 * @author Silvio Wangler
 */
public abstract class AbstractResourceBuilder implements ResourceBuilder {

    private File specification;
    private ResourceContract resourceContract;
    protected TypeSpec.Builder typeBuilder;
    private Verb currentVerb;
    private String currentPackageName;

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
    public ResourceBuilder withCurrentPackageName(String packageName) {
        this.currentPackageName = packageName;
        return this;
    }

    @Override
    public ResourceBuilder withSpecification(File file) {
        this.specification = Objects.requireNonNull(file, "file must not be null");

        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("File %s does not exist", file.getAbsolutePath()));
        }

        try {
            this.resourceContract = new GsonBuilder()
                    .registerTypeAdapter(GeneralDetails.class, new GeneralDetailsDeserializer())
                    .create().fromJson(new FileReader(file), ResourceContract.class);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to transform JSON file " + file.getAbsolutePath() + " to Java model", e);
        }
        return this;
    }


    @Override
    public ResourceContract getModel() {
        return this.resourceContract;
    }

    @Override
    public File getSpecification() {
        return this.specification;
    }

    protected TypeSpec.Builder interfaceBaseInstance() {

        if (this.typeBuilder == null) {
            this.typeBuilder = TypeSpec.interfaceBuilder(resourceName())
                    .addModifiers(PUBLIC)
                    .addAnnotation(createGeneratedAnnotation());
        }
        return this.typeBuilder;
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

    protected abstract AnnotationTypes getPathVariableAnnotationType();

    protected abstract void createOptionsMethod();

    @Override
    public void generateResourceMethods() {

        if (isResourceInterface()) {

            String content;
            try {
                content = new String(Files.readAllBytes(getSpecification().toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            content = Minify.minify(content).replaceAll("\"", "\\\\\"");

            FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.get(String.class), "OPTIONS_CONTENT").addModifiers(PUBLIC, STATIC, FINAL)
                    .initializer("$N", "\"" + content + "\"");

            this.typeBuilder.addField(fieldBuilder.build());

            createOptionsMethod();
        }

        Collections.sort(getModel().getVerbs(), Comparator.comparing(Verb::getVerb));

        for (Verb verb : getModel().getVerbs()) {

            MethodSpec.Builder methodBuilder;

            Map<String, ClassName> params = new HashMap<>();
            this.currentVerb = verb;

            verb.getParameters().forEach(p -> params.put(p.getName(), GeneratorUtil.translateToJava(p.getType())));

            if (GenerateRestApiTask.GET_COLLECTION.equals(verb.getVerb())) {

                methodBuilder = createMethod(
                        "getCollection",
                        resourceModelName(),
                        params
                );

            } else if (GenerateRestApiTask.GET_ENTITY.equals(verb.getVerb())) {


                methodBuilder = createMethod(
                        "getEntity",
                        resourceModelName(),
                        params
                );

                ParameterSpec.Builder param = ParameterSpec.builder(ClassName.get(String.class), "id");

                if (getArtifactType().equals(ArtifactType.RESOURCE)) {

                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put("value", "id");

                    param.addAnnotation(
                            createAnnotation(getPathVariableAnnotationType(), attrs)
                    ).build();
                }
                methodBuilder.addParameter(param.build());


            } else if (GenerateRestApiTask.POST.equals(verb.getVerb())) {

                methodBuilder = createMethod(
                        "createEntity",
                        resourceModelName(),
                        params
                );

            } else if (GenerateRestApiTask.PUT.equals(verb.getVerb())) {

                methodBuilder = createMethod(
                        "updateEntity",
                        resourceModelName(),
                        params
                );

            } else if (GenerateRestApiTask.DELETE_COLLECTION.equals(verb.getVerb())) {

                methodBuilder = createMethod(
                        "deleteCollection",
                        resourceModelName(),
                        params
                );

            } else if (GenerateRestApiTask.DELETE_ENTITY.equals(verb.getVerb())) {

                methodBuilder = createMethod(
                        "deleteEntity",
                        resourceModelName(),
                        params
                );

            } else {
                throw new IllegalArgumentException(String.format("Verb %s is unknown", verb.getVerb()));
            }
            this.currentVerb = null;
            this.typeBuilder.addMethod(methodBuilder.build());
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
        } else if (!hasDeleteEntityVerb()) {
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
        return getModel().getVerbs().stream().filter(v -> verb.equals(v.getVerb())).findAny().isPresent();
    }

    protected String getPath() {
        return new LinkParser(
                getModel().getGeneral().getxRoute(),
                getModel().getGeneral().getVersion().split("\\.")[0]
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
}
