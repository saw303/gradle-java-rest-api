package ch.silviowangler.gradle.restapi.builder;

import com.squareup.javapoet.TypeSpec;

import java.io.File;

import static javax.lang.model.element.Modifier.PUBLIC;

public abstract class AbstractResourceBuilder implements ResourceBuilder {

    private File specification;

    @Override
    public ResourceBuilder withSpecification(File file) {
        this.specification = file;
        return this;
    }

    public File getSpecification() {
        return this.specification;
    }

    protected TypeSpec.Builder buildInterfaceBase() {
        return TypeSpec.interfaceBuilder(resourceName(this.specification))
                .addModifiers(PUBLIC)
                .addAnnotation(createGeneratedAnnotation());
    }


}
