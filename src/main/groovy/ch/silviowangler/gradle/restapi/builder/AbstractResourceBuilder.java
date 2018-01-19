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

import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.Verb;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.util.Optional;

import static ch.silviowangler.gradle.restapi.GenerateRestApiTask.GET_ENTITY;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author Silvio Wangler
 */
public abstract class AbstractResourceBuilder implements ResourceBuilder {

    private File specification;
    private ResourceContract resourceContract;
    private TypeSpec.Builder interfaceBuilder;

    @Override
    public ResourceBuilder withSpecification(File file) {
        this.specification = file;
        this.resourceContract = new ResourceContract();
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
            this.interfaceBuilder = TypeSpec.interfaceBuilder(resourceName(this.specification))
                    .addModifiers(PUBLIC)
                    .addAnnotation(createGeneratedAnnotation());
        }
        return this.interfaceBuilder;
    }

    @Override
    public void generateResourceMethods() {

        Optional<Verb> getEntityVerb = fetchVerb(this.resourceContract, GET_ENTITY);

        getEntityVerb.ifPresent(getEntity -> {
            // do some
        });


        System.out.println("");
    }
}
