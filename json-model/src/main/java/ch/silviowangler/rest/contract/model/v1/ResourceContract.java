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
package ch.silviowangler.rest.contract.model.v1;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Silvio Wangler
 */
public class ResourceContract implements Serializable {

  private GeneralDetails general;
  private List<Verb> verbs = Collections.emptyList();
  private List<ResourceField> fields = Collections.emptyList();
  private List<SubResource> subresources = Collections.emptyList();
  private List<ResourceTypes> types = Collections.emptyList();

  public GeneralDetails getGeneral() {
    return general;
  }

  public void setGeneral(GeneralDetails general) {
    this.general = general;
  }

  public List<Verb> getVerbs() {
    return verbs;
  }

  public void setVerbs(List<Verb> verbs) {
    this.verbs = verbs;
  }

  public List<ResourceField> getFields() {
    return fields;
  }

  public void setFields(List<ResourceField> fields) {
    this.fields = fields;
  }

  public List<SubResource> getSubresources() {
    return subresources;
  }

  public void setSubresources(List<SubResource> subresources) {
    this.subresources = subresources;
  }

  public List<ResourceTypes> getTypes() {
    return types;
  }

  public void setTypes(List<ResourceTypes> types) {
    this.types = types;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResourceContract that = (ResourceContract) o;
    return Objects.equals(general, that.general)
        && Objects.equals(verbs, that.verbs)
        && Objects.equals(fields, that.fields)
        && Objects.equals(subresources, that.subresources)
        && Objects.equals(types, that.types);
  }

  @Override
  public int hashCode() {
    return Objects.hash(general, verbs, fields, subresources, types);
  }

  public String toString() {

    if (general != null) {
      return this.general.getName();
    } else {
      return super.toString();
    }
  }
}
