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

import static java.nio.charset.StandardCharsets.UTF_8;

import ch.silviowangler.gradle.restapi.GeneratedSpecContainer;
import ch.silviowangler.gradle.restapi.RestApiExtension;
import ch.silviowangler.gradle.restapi.gson.CustomTypeFieldDeserializer;
import ch.silviowangler.gradle.restapi.gson.GeneralDetailsDeserializer;
import ch.silviowangler.gradle.restapi.gson.HeaderDeserializer;
import ch.silviowangler.gradle.restapi.gson.RepresentationDeserializer;
import ch.silviowangler.gradle.restapi.gson.ResourceFieldDeserializer;
import ch.silviowangler.rest.contract.model.v1.CustomTypeField;
import ch.silviowangler.rest.contract.model.v1.GeneralDetails;
import ch.silviowangler.rest.contract.model.v1.Header;
import ch.silviowangler.rest.contract.model.v1.Representation;
import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.ResourceField;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Silvio Wangler
 */
public class SpecGenerator {

  private final Set<ClassName> resourceTypeCache;
  private final Gson gson;

  public SpecGenerator() {
    this.resourceTypeCache = new HashSet<>();
    this.gson =
        new GsonBuilder()
            .registerTypeAdapter(GeneralDetails.class, new GeneralDetailsDeserializer())
            .registerTypeAdapter(ResourceField.class, new ResourceFieldDeserializer())
            .registerTypeAdapter(CustomTypeField.class, new CustomTypeFieldDeserializer())
            .registerTypeAdapter(Representation.class, new RepresentationDeserializer())
            .registerTypeAdapter(Header.class, new HeaderDeserializer())
            .create();
  }

  /**
   * Generates a Java classes for a specific resource specification and provides them in a {@link
   * GeneratedSpecContainer}.
   *
   * @param specFile the specification of the resource.
   * @param extension additional context information.
   * @return all generated Java types for the resource specification.
   */
  public GeneratedSpecContainer generateJavaTypesForSpecification(
      File specFile, RestApiExtension extension) {

    ResourceContractContainer resourceContractContainer =
        parseResourceContract(specFile, extension.getResponseEncoding());

    String packageName =
        String.format(
                "%s.%s",
                extension.getPackageName(),
                generatePackageName(resourceContractContainer.getResourceContract()))
            .toLowerCase();

    GeneratedSpecContainer result = new GeneratedSpecContainer();
    result.setPackageName(packageName);

    ResourceBuilder resourceBuilder =
        ResourceBuilderFactory.getRootResourceBuilder(extension)
            .withResourceContractContainer(resourceContractContainer)
            .withCurrentPackageName(packageName)
            .withTimestampInGeneratedAnnotation(extension.isGenerateDateAttribute());

    if (extension.getResponseEncoding() != null) {
      resourceBuilder.withResponseEncoding(extension.getResponseEncoding());
    }

    Set<TypeSpec> types = resourceBuilder.buildResourceTypes(resourceTypeCache, packageName);

    for (TypeSpec type : types) {
      resourceTypeCache.add(ClassName.get(packageName, type.name));
    }

    if (extension.getGenerationMode().isApiCodeGenerationRequired()) {
      result.setModels(resourceBuilder.buildResourceModels(resourceTypeCache));
      result.setTypes(types);
    }

    if (extension.getGenerationMode().isServerCodeGenerationRequired()) {
      result.setRestInterface(resourceBuilder.buildResource());
      result.setRestImplementation(resourceBuilder.buildResourceImpl());
    }

    if (extension.getGenerationMode().isClientCodeGenerationRequired()) {
      result.setRestInterface(resourceBuilder.buildClient());
    }

    return result;
  }

  public ResourceContractContainer parseResourceContract(File file) {
    return parseResourceContract(file, null);
  }

  public ResourceContractContainer parseResourceContract(File file, Charset encoding) {
    Objects.requireNonNull(file, "file must not be null");

    if (!file.exists()) {
      throw new IllegalArgumentException(
          String.format("File %s does not exist", file.getAbsolutePath()));
    }

    try {
      ResourceContract resourceContract =
          gson.fromJson(new FileReader(file), ResourceContract.class);

      if (encoding != null) {
        resourceContract.getVerbs().stream()
            .flatMap(verb -> verb.getRepresentations().stream())
            .filter(representation -> "json".equals(representation.getMimetype().getSubType()))
            .forEach(
                representation ->
                    representation.getMimetype().setParameter("charset", encoding.toString()));
      }

      String plainText = new String(Files.readAllBytes(file.toPath()), UTF_8);
      return new ResourceContractContainer(resourceContract, plainText, file.getName());
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "Unable to transform JSON file " + file.getAbsolutePath() + " to Java model", e);
    }
  }

  private String generatePackageName(ResourceContract resourceContract) {

    GeneralDetails general = resourceContract.getGeneral();
    String version = readVersion(general.getVersion());
    String route = general.getxRoute().replace(":version", version);

    List<String> tokens =
        Arrays.stream(route.split("\\/"))
            .filter(r -> !r.startsWith(":") && r.length() > 0)
            .collect(Collectors.toList());

    if (tokens.size() <= 1) {
      return version;
    } else {
      return String.join(".", tokens.toArray(new String[0]));
    }
  }

  private String readVersion(String versionString) {
    return String.format("v%s", versionString.split("\\.")[0]);
  }
}
