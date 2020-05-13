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
package ch.silviowangler.rest.micronaut;

import ch.silviowangler.rest.model.ResourceLink;
import ch.silviowangler.rest.model.ResourceModel;
import io.micronaut.web.router.UriRouteMatch;
import java.util.List;

/** Used to add additional ResourceLink to an HATEOAS response. */
public interface LinkProvider {

  /**
   * All link providers are called with getLinks on every routeMatch from the HATEOAS filter {@link
   * HateoasResponseFilter}, and the resulting links are added to the response in the links of the
   * {@link ch.silviowangler.rest.model.EntityModel}. If a baseUrl is configured under
   * `restapi.hateaos.filter.base` then it is prepended to all resourceLinks.
   *
   * @param routeMatch the current routeMatch in the filter
   * @param model the resource model with the data of the response
   * @return links to be added to the response. Return empty if no links are to be added.
   */
  List<ResourceLink> getLinks(UriRouteMatch routeMatch, ResourceModel model);
}
