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
package ch.silviowangler.rest.model;

import io.micronaut.serde.annotation.Serdeable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a URL query parameter in a {@link ResourceLink}
 *
 * @author Silvio Wangler
 */
@Serdeable
public class LinkParameter implements Serializable {
  private String name;
  private String type;
  private Boolean mandatory = Boolean.FALSE;

  public LinkParameter(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public LinkParameter(String name, String type, Boolean mandatory) {
    this.name = name;
    this.type = type;
    this.mandatory = mandatory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LinkParameter that = (LinkParameter) o;
    return Objects.equals(name, that.name)
        && Objects.equals(type, that.type)
        && Objects.equals(mandatory, that.mandatory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, mandatory);
  }

  public Boolean getMandatory() {
    return mandatory;
  }

  public void setMandatory(Boolean mandatory) {
    this.mandatory = mandatory;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("LinkParameter{");
    sb.append("name='").append(name).append('\'');
    sb.append(", type='").append(type).append('\'');
    sb.append(", mandatory=").append(mandatory);
    sb.append('}');
    return sb.toString();
  }
}
