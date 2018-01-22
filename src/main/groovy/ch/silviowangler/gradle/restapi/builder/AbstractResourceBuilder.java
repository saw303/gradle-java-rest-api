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
import ch.silviowangler.gradle.restapi.LinkParser;
import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.Verb;
import com.google.gson.Gson;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author Silvio Wangler
 */
public abstract class AbstractResourceBuilder implements ResourceBuilder {

    private File specification;
    private ResourceContract resourceContract;
    private TypeSpec.Builder interfaceBuilder;
    private Verb currentVerb;
    private String currentPackageName;

    public Verb getCurrentVerb() {
        return currentVerb;
    }

    protected void setCurrentVerb(Verb currentVerb) {
        this.currentVerb = currentVerb;
    }

    public String getCurrentPackageName() {
        return currentPackageName;
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
            this.resourceContract = new Gson().fromJson(new FileReader(file), ResourceContract.class);
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

        if (this.interfaceBuilder == null) {
            this.interfaceBuilder = TypeSpec.interfaceBuilder(resourceName())
                    .addModifiers(PUBLIC)
                    .addAnnotation(createGeneratedAnnotation());
        }
        return this.interfaceBuilder;
    }

    @Override
    public void generateResourceMethods() {

        for (Verb verb : getModel().getVerbs()) {


            this.currentVerb = verb;

            if (GenerateRestApiTask.GET_ENTITY.equals(verb.getVerb())) {

                Map<String, ClassName> params = new HashMap<>();

                params.put("id", ClassName.get(String.class));


                interfaceBaseInstance().addMethod(

                        createInterfaceMethod(
                                "getEntity",
                                resourceModelName(),
                                params
                        ).build()
                );

            } else if (GenerateRestApiTask.GET_COLLECTION.equals(verb.getVerb())) {

            } else if (GenerateRestApiTask.POST.equals(verb.getVerb())) {

            } else if (GenerateRestApiTask.PUT.equals(verb.getVerb())) {

            } else if (GenerateRestApiTask.DELETE_COLLECTION.equals(verb.getVerb())) {

            } else if (GenerateRestApiTask.DELETE_ENTITY.equals(verb.getVerb())) {

            } else {
                throw new IllegalArgumentException(String.format("Verb %s is unknown", verb.getVerb()));
            }
            this.currentVerb = null;
        }
    }

    protected String getPath() {
        return new LinkParser(
                getModel().getGeneral().getxRoute(),
                getModel().getGeneral().getVersion().split("\\.")[0]
        ).toBasePath();
    }

    protected String getHttpMethod() {

        String v = Objects.requireNonNull(currentVerb).getVerb();

        if (GenerateRestApiTask.GET_ENTITY.equals(v) || GenerateRestApiTask.GET_COLLECTION.equals(v)) {
            return "GET";
        } else if (GenerateRestApiTask.DELETE_ENTITY.equals(v) || GenerateRestApiTask.DELETE_COLLECTION.equals(v)) {
            return "DELETE";
        } else if (GenerateRestApiTask.PUT.equals(v)) {
            return "PUT";
        } else if (GenerateRestApiTask.POST.equals(v)) {
            return "POST";
        } else if ("OPTIONS".equals(v)) {
            return "OPTIONS";
        } else {
            throw new IllegalArgumentException("Unknown verb " + v);
        }

    }
}
