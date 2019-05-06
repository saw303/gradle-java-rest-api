/*
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2019 Silvio Wangler (silvio.wangler@gmail.com)
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

import ch.silviowangler.rest.model.CollectionModel;
import ch.silviowangler.rest.model.EntityModel;
import ch.silviowangler.rest.model.Identifiable;
import ch.silviowangler.rest.model.ResourceLink;
import ch.silviowangler.rest.model.ResourceModel;
import ch.silviowangler.rest.model.SelfLinkProvider;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.web.router.UriRouteMatch;
import io.reactivex.Flowable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.reactivestreams.Publisher;

/**
 * Transforms a {@link ResourceModel} into a {@link EntityModel} or a {@link CollectionModel}.
 *
 * <p>The JSON structure of an {@link EntityModel} will look like this:
 *
 * <pre>
 * {
 *   "data": {
 *     "id": "CHE",
 *     "name": "Switzerland",
 *     "foundationDate": "1291-08-01",
 *     "surface": 41285
 *   },
 *   "links": [
 *     {
 *       "rel": "self",
 *       "method": "GET",
 *       "href": "/v1/countries/CHE"
 *     }
 *   ]
 * }
 * </pre>
 *
 * @author Silvio Wangler
 */
@Filter("${restapi.hateoas.filter.uri}")
@Requires(property = "restapi.hateoas.filter.enabled")
public class HateoasResponseFilter implements HttpServerFilter {
  private final List<LinkProvider> linkProviderList;

  public HateoasResponseFilter(List<LinkProvider> linkProviderList) {
    this.linkProviderList = linkProviderList;
  }

  @Override
  public int getOrder() {
    return FilterOrder.HATEOAS_MODEL_CREATION;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Publisher<MutableHttpResponse<?>> doFilter(
      HttpRequest<?> request, ServerFilterChain chain) {

    return Flowable.fromPublisher(chain.proceed(request))
        .doOnNext(
            res -> {
              Optional<UriRouteMatch> potUriRouteMatch =
                  res.getAttributes()
                      .get(HttpAttributes.ROUTE_MATCH.toString(), UriRouteMatch.class);

              if (potUriRouteMatch.isPresent()) {
                UriRouteMatch uriRouteMatch = potUriRouteMatch.get();

                if (uriRouteMatch.getProduces().contains(MediaType.APPLICATION_JSON_TYPE)) {

                  if (res.body() instanceof ResourceModel) {

                    ResourceModel resourceModel = (ResourceModel) res.body();
                    EntityModel entityModel = new EntityModel(resourceModel);

                    if (resourceModel instanceof SelfLinkProvider) {
                      ((SelfLinkProvider) resourceModel)
                          .selfLink()
                          .ifPresent(selfLink -> entityModel.getLinks().add(selfLink));
                    } else {
                      entityModel.getLinks().add(ResourceLink.selfLink(uriRouteMatch.getUri()));
                    }

                    linkProviderList.forEach(
                        provider ->
                            entityModel.getLinks().addAll(provider.getLinks(uriRouteMatch)));

                    ((MutableHttpResponse) res).body(entityModel);

                  } else if (res.body() instanceof Collection) {

                    Collection models = (Collection) res.body();

                    CollectionModel collectionModel = new CollectionModel();
                    collectionModel.getLinks().add(ResourceLink.selfLink(uriRouteMatch.getUri()));

                    for (Object model : models) {
                      if (model instanceof ResourceModel) {
                        ResourceModel resourceModel = (ResourceModel) model;
                        EntityModel entityModel = new EntityModel(resourceModel);

                        linkProviderList.forEach(
                            provider ->
                                entityModel.getLinks().addAll(provider.getLinks(uriRouteMatch)));

                        if (model instanceof Identifiable) {
                          entityModel
                              .getLinks()
                              .add(
                                  ResourceLink.selfLink(
                                      uriRouteMatch.getUri()
                                          + "/"
                                          + ((Identifiable) resourceModel).getId()));
                        }

                        collectionModel.getData().add(entityModel);
                      }
                    }
                    ((MutableHttpResponse) res).body(collectionModel);
                  }
                }
              }
            });
  }
}
