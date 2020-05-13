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
package ch.silviowangler.rest.types;

import ch.silviowangler.rest.model.Identifiable;
import ch.silviowangler.rest.model.ResourceLink;
import ch.silviowangler.rest.model.ResourceModel;
import ch.silviowangler.rest.model.SelfLinkProvider;
import java.io.Serializable;
import java.util.Optional;

/**
 * Java type that is use when only an ID is return in the response. (POST/PUT)
 *
 * @author Silvio Wangler
 */
public class IdType implements ResourceModel, Identifiable<String>, SelfLinkProvider, Serializable {

  private String id;
  private transient String path;

  public IdType() {
    this(null);
  }

  public IdType(String id) {
    this(id, null);
  }

  public IdType(String id, String path) {
    this.id = id;
    this.path = path;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Optional<ResourceLink> selfLink() {

    ResourceLink selfLink = null;

    if (this.path != null) {
      selfLink = ResourceLink.selfLink(this.path);
    }

    return Optional.ofNullable(selfLink);
  }
}
