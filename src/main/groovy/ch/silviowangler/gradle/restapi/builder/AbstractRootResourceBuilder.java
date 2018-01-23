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
package ch.silviowangler.gradle.restapi.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import io.github.getify.minify.Minify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static javax.lang.model.element.Modifier.*;

public abstract class AbstractRootResourceBuilder extends AbstractResourceBuilder implements RootResourceBuilder {

    @Override
    public RootResourceBuilder withCurrentPackageName(String packageName) {
        super.withCurrentPackageName(packageName);
        return this;
    }

    @Override
    public RootResourceBuilder withSpecification(File file) {
        super.withSpecification(file);
        return this;
    }

    public void generateResourceMethodsWithOptions() {

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

        super.generateResourceMethods();
    }

    protected abstract void createOptionsMethod();
}
