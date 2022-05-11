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
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Silvio Wangler
 */
public class GeneralDetails implements Serializable {

  private String name;
  private String description;
  private String version;
  private String xRoute;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getxRoute() {
    return xRoute;
  }

  public void setxRoute(String xRoute) {
    this.xRoute = xRoute;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GeneralDetails that = (GeneralDetails) o;
    return Objects.equals(getName(), that.getName())
        && Objects.equals(getDescription(), that.getDescription())
        && Objects.equals(getVersion(), that.getVersion())
        && Objects.equals(getxRoute(), that.getxRoute());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getDescription(), getVersion(), getxRoute());
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GeneralDetails.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .add("version='" + version + "'")
        .add("xRoute='" + xRoute + "'")
        .toString();
  }
}
