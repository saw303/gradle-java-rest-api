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
package ch.silviowangler.gradle.restapi;

import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Silvio Wangler
 */
public class GeneratedSpecContainer {

  private String packageName;
  private TypeSpec restInterface;
  private TypeSpec restImplementation;
  private Collection<TypeSpec> models = new ArrayList<>();
  private Collection<TypeSpec> types = new ArrayList<>();

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public TypeSpec getRestInterface() {
    return restInterface;
  }

  public void setRestInterface(TypeSpec restInterface) {
    this.restInterface = restInterface;
  }

  public TypeSpec getRestImplementation() {
    return restImplementation;
  }

  public void setRestImplementation(TypeSpec restImplementation) {
    this.restImplementation = restImplementation;
  }

  public Collection<TypeSpec> getModels() {
    return models;
  }

  public void setModels(Collection<TypeSpec> models) {
    this.models = models;
  }

  public Collection<TypeSpec> getTypes() {
    return types;
  }

  public void setTypes(Collection<TypeSpec> types) {
    this.types = types;
  }

  /**
   * Collects all generated types including the resource itself.
   *
   * @return see description above.
   */
  public Iterable<TypeSpec> collectGeneratedTypes() {

    List<TypeSpec> all = new ArrayList<>();

    if (this.restInterface != null) {
      all.add(this.restInterface);
    }
    all.addAll(this.models);
    all.addAll(this.types);
    return all;
  }
}
