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

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents a resource link used in {@link EntityModel} or {@link CollectionModel}.
 *
 * @author Silvio Wangler
 */
public class ResourceLink implements Serializable {

  private String rel;
  private String method;
  private URI href;
  private List<LinkParameter> params;

  // for jackson
  public ResourceLink() {}

  public ResourceLink(URI href) {
    this.href = href;
    this.rel = "self";
    this.method = "GET";
    this.params = Collections.emptyList();
  }

  public ResourceLink(String rel, String method, URI href) {
    this.rel = rel;
    this.method = method;
    this.href = href;
    this.params = Collections.emptyList();
  }

  public ResourceLink(String rel, String method, URI href, List<LinkParameter> params) {
    this.rel = rel;
    this.method = method;
    this.href = href;
    this.params = Collections.unmodifiableList(params);
  }

  public String getRel() {
    return rel;
  }

  public void setRel(String rel) {
    this.rel = rel;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public URI getHref() {
    return href;
  }

  public void setHref(URI href) {
    this.href = href;
  }

  public List<LinkParameter> getParams() {
    return params;
  }

  public void setParams(List<LinkParameter> params) {
    this.params = params;
  }

  /**
   * Generates a self link from URI string representation.
   *
   * @param uri the URI
   * @return a self link with that URI.
   */
  public static ResourceLink selfLink(String uri) {
    return new ResourceLink(URI.create(uri));
  }

  public static ResourceLink relLink(String rel, String uri) {
    return new ResourceLink(rel, "GET", URI.create(uri));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResourceLink that = (ResourceLink) o;
    return Objects.equals(rel, that.rel)
        && Objects.equals(method, that.method)
        && Objects.equals(href, that.href)
        && Objects.equals(params, that.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rel, method, href, params);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ResourceLink.class.getSimpleName() + "[", "]")
        .add("rel='" + rel + "'")
        .add("method='" + method + "'")
        .add("href=" + href)
        .add("params=" + params)
        .toString();
  }
}
