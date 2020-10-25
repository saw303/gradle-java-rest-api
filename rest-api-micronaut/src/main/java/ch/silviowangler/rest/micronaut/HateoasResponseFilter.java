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

import ch.silviowangler.rest.model.CollectionModel;
import ch.silviowangler.rest.model.EntityModel;
import ch.silviowangler.rest.model.Identifiable;
import ch.silviowangler.rest.model.PaginationCollectionModel;
import ch.silviowangler.rest.model.ResourceLink;
import ch.silviowangler.rest.model.ResourceModel;
import ch.silviowangler.rest.model.SelfLinkProvider;
import ch.silviowangler.rest.model.pagination.Page;
import ch.silviowangler.rest.model.pagination.Slice;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.web.router.UriRouteMatch;
import io.reactivex.Flowable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
@Requires(property = "restapi.hateoas.filter.enabled", value = "true")
public class HateoasResponseFilter implements HttpServerFilter {
  private final List<LinkProvider> linkProviderList;
  private final String baseUrl;

  public HateoasResponseFilter(
      List<LinkProvider> linkProviderList,
      @Value("${restapi.hateoas.filter.base:}") String baseUrl) {
    this.linkProviderList = linkProviderList;
    this.baseUrl = baseUrl;
    validateBaseUrl(baseUrl);
  }

  private void validateBaseUrl(String baseUrl) {
    if (!baseUrl.isEmpty()) {
      try {
        new URI(baseUrl);
      } catch (URISyntaxException e) {
        throw new RuntimeException(
            String.format("Invalid URI for restapi.hateaos.filter.base: [%s]", baseUrl));
      }
    }
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
            response -> {
              Optional<UriRouteMatch> potUriRouteMatch =
                  response
                      .getAttributes()
                      .get(HttpAttributes.ROUTE_MATCH.toString(), UriRouteMatch.class);

              if (potUriRouteMatch.isPresent()) {
                UriRouteMatch uriRouteMatch = potUriRouteMatch.get();

                if (uriRouteMatch.getProduces().contains(MediaType.APPLICATION_JSON_TYPE)) {

                  if (response.body() instanceof ResourceModel) {

                    ResourceModel resourceModel = (ResourceModel) response.body();
                    EntityModel entityModel = new EntityModel(resourceModel);

                    addProviderLinks(uriRouteMatch, resourceModel, entityModel);

                    if (!hasLink(entityModel, "self")) {
                      if (resourceModel instanceof SelfLinkProvider) {
                        ((SelfLinkProvider) resourceModel)
                            .selfLink()
                            .ifPresent(
                                selfLink -> entityModel.getLinks().add(addBaseUrl(selfLink)));
                      } else {
                        entityModel
                            .getLinks()
                            .add(addBaseUrl(ResourceLink.selfLink(uriRouteMatch.getUri())));
                      }
                    }

                    ((MutableHttpResponse) response).body(entityModel);

                  } else if (response.body() instanceof Collection
                      || response.body() instanceof Slice) {

                    Iterable models;
                    CollectionModel collectionModel;

                    if (response.body() instanceof Page) {
                      collectionModel = new PaginationCollectionModel((Page) response.body());
                      models = ((Page) response.body()).getContent();
                    } else if (response.body() instanceof Slice) {
                      collectionModel = new PaginationCollectionModel((Slice) response.body());
                      models = ((Slice) response.body()).getContent();
                    } else {
                      collectionModel = new CollectionModel();
                      models = (Collection) response.body();
                    }

                    ResourceLink collectionSelfLink = ResourceLink.selfLink(uriRouteMatch.getUri());

                    if (collectionModel instanceof PaginationCollectionModel
                        && response.body() instanceof Slice) {

                      String params =
                          StreamSupport.stream(request.getParameters().spliterator(), false)
                              .filter(
                                  p -> !p.getKey().equals("page") && !p.getKey().equals("limit"))
                              .filter(p -> !p.getValue().isEmpty())
                              .map(
                                  p ->
                                      p.getValue().stream()
                                          .map(
                                              v -> {
                                                String value;
                                                try {
                                                  value =
                                                      URLEncoder.encode(
                                                          v, StandardCharsets.UTF_8.toString());
                                                } catch (UnsupportedEncodingException e) {
                                                  value = v;
                                                }
                                                return String.format("%s=%s", p.getKey(), value);
                                              })
                                          .collect(Collectors.joining("&")))
                              .collect(Collectors.joining("&"));

                      Slice slice = (Slice) response.body();

                      collectionSelfLink =
                          ResourceLink.selfLink(
                              uriRouteMatch.getUri()
                                  + "?page="
                                  + slice.getPageNumber()
                                  + "&limit="
                                  + slice.getSize()
                                  + addExistingParams(params));

                      collectionModel
                          .getLinks()
                          .add(
                              addBaseUrl(
                                  ResourceLink.relLink(
                                      "first",
                                      uriRouteMatch.getUri()
                                          + "?page=0&limit="
                                          + slice.getSize()
                                          + addExistingParams(params))));

                      if (slice instanceof Page) {

                        Page page = (Page) slice;

                        if (page.getPageNumber() > 0
                            && page.getTotalPages() >= page.getPageNumber()) {
                          collectionModel
                              .getLinks()
                              .add(
                                  addBaseUrl(
                                      ResourceLink.relLink(
                                          "previous",
                                          uriRouteMatch.getUri()
                                              + "?page="
                                              + (slice.getPageNumber() - 1)
                                              + "&limit="
                                              + slice.getSize()
                                              + addExistingParams(params))));
                        }

                        if (!page.isLastPage()) {
                          collectionModel
                              .getLinks()
                              .add(
                                  addBaseUrl(
                                      ResourceLink.relLink(
                                          "next",
                                          uriRouteMatch.getUri()
                                              + "?page="
                                              + (slice.getPageNumber() + 1)
                                              + "&limit="
                                              + slice.getSize()
                                              + addExistingParams(params))));
                        }

                        collectionModel
                            .getLinks()
                            .add(
                                addBaseUrl(
                                    ResourceLink.relLink(
                                        "last",
                                        uriRouteMatch.getUri()
                                            + "?page="
                                            + (page.getTotalPages() - 1)
                                            + "&limit="
                                            + slice.getSize()
                                            + addExistingParams(params))));
                      }
                    }

                    collectionModel.getLinks().add(addBaseUrl(collectionSelfLink));

                    for (Object model : models) {
                      if (model instanceof ResourceModel) {
                        ResourceModel resourceModel = (ResourceModel) model;
                        EntityModel entityModel = new EntityModel(resourceModel);

                        addProviderLinks(uriRouteMatch, resourceModel, entityModel);

                        // this self link is only added if the linkProviders are not already
                        // defining one
                        if (model instanceof Identifiable && !hasLink(entityModel, "self")) {
                          ResourceLink selfLink =
                              ResourceLink.selfLink(
                                  uriRouteMatch.getUri()
                                      + "/"
                                      + ((Identifiable) resourceModel).getId());
                          entityModel.getLinks().add(addBaseUrl(selfLink));
                        }

                        collectionModel.getData().add(entityModel);
                      }
                    }
                    ((MutableHttpResponse) response).body(collectionModel);
                  }
                }
              }
            });
  }

  private void addProviderLinks(
      UriRouteMatch uriRouteMatch,
      ResourceModel resourceModel,
      EntityModel<? extends ResourceModel> entityModel) {
    linkProviderList.forEach(
        provider -> {
          List<ResourceLink> links = provider.getLinks(uriRouteMatch, resourceModel);
          links = addBaseUrl(links);
          entityModel.getLinks().addAll(links);
        });
  }

  private ResourceLink addBaseUrl(ResourceLink link) {
    if (baseUrl.isEmpty() || link.getHref().isAbsolute()) {
      return link;
    }
    return new ResourceLink(
        link.getRel(), link.getMethod(), URI.create(baseUrl + link.getHref()), link.getParams());
  }

  private List<ResourceLink> addBaseUrl(List<ResourceLink> links) {
    return links.stream().map(this::addBaseUrl).collect(Collectors.toList());
  }

  private boolean hasLink(EntityModel<? extends ResourceModel> entityModel, String relName) {
    if (entityModel.getLinks() == null) {
      return false;
    }
    return entityModel.getLinks().stream()
        .map(ResourceLink::getRel)
        .anyMatch(it -> Objects.equals(relName, it));
  }

  private String addExistingParams(String params) {
    if (params == null || params.length() == 0) {
      return "";
    }
    return "&" + params;
  }
}
