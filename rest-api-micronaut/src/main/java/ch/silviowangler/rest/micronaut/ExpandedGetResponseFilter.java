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

import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.SubResource;
import ch.silviowangler.rest.model.CollectionExpand;
import ch.silviowangler.rest.model.CollectionModel;
import ch.silviowangler.rest.model.EntityExpand;
import ch.silviowangler.rest.model.EntityModel;
import ch.silviowangler.rest.model.Expand;
import ch.silviowangler.rest.model.Identifiable;
import ch.silviowangler.rest.model.ResourceModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRouteMatch;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Enables "Expanded GET" requests.
 *
 * <p>Without "Expanded Gets" a client has to execute at least two HTTP calls to read a person and
 * its addresses.
 *
 * <ul>
 *   <li>/persons/123 - to read the person
 *   <li>/persons/123/addresses/ - to read all addresses for that person
 * </ul>
 *
 * <p>With "Expanded Gets" a client can read the person and its addresses in only one request.
 *
 * <ul>
 *   <li>/persons/123?expands=addresses
 * </ul>
 *
 * <p>This filter modifies the response after {@link HateoasResponseFilter} has finished its work.
 *
 * @author Silvio Wangler
 */
@Filter("${restapi.hateoas.filter.uri}")
@Requires(property = "restapi.hateoas.filter.enabled")
public class ExpandedGetResponseFilter implements HttpServerFilter {

  private final ApplicationContext applicationContext;
  private final Router router;
  private static final String EXPAND_PARAM_NAME = "expands";
  private Map<Class, ResourceContract> contractStore;
  private static final Logger log = getLogger(ExpandedGetResponseFilter.class);
  private final ObjectMapper objectMapper;

  public ExpandedGetResponseFilter(
      ApplicationContext applicationContext, Router router, ObjectMapper objectMapper) {
    this.applicationContext = applicationContext;
    this.router = router;
    this.objectMapper = objectMapper;
    this.contractStore = new HashMap<>();
  }

  @Override
  public int getOrder() {
    return FilterOrder.EXPANDED_GETS;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Publisher<MutableHttpResponse<?>> doFilter(
      HttpRequest<?> request, ServerFilterChain chain) {

    return Flowable.fromPublisher(chain.proceed(request))
        .doOnNext(
            res -> {
              if (request.getMethod().equals(HttpMethod.GET)
                  && request.getParameters().contains(EXPAND_PARAM_NAME)) {
                String expands = request.getParameters().get(EXPAND_PARAM_NAME);

                Optional<UriRouteMatch> uriRouteMatch =
                    request
                        .getAttributes()
                        .get(HttpAttributes.ROUTE_MATCH.toString(), UriRouteMatch.class);

                if (uriRouteMatch.isPresent()) {

                  UriRouteMatch routeMatchCurrentResource = uriRouteMatch.get();
                  Object currentResource =
                      applicationContext.getBean(
                          routeMatchCurrentResource.getExecutableMethod().getDeclaringType());

                  ResourceContract contract = fetchContract(currentResource).get();

                  // fetch all expandable resources
                  if (Objects.equals("*", expands)) {
                    expands =
                        contract.getSubresources().stream()
                            .filter(subResource -> subResource.isExpandable())
                            .map(subResource -> subResource.getName())
                            .collect(Collectors.joining());
                  }

                  Object initialBody = res.body();

                  if (initialBody instanceof EntityModel) {
                    attachExpandedGetsBody(
                        expands,
                        routeMatchCurrentResource,
                        contract,
                        (EntityModel) initialBody,
                        false);
                    ((MutableHttpResponse) res).body(initialBody);
                  } else if (initialBody instanceof CollectionModel) {
                    for (EntityModel model : ((CollectionModel) initialBody).getData()) {
                      attachExpandedGetsBody(
                          expands, routeMatchCurrentResource, contract, model, true);
                    }
                    ((MutableHttpResponse) res).body(initialBody);
                  } else {
                    log.debug(
                        "Return type '{}' and not as expected '{}'",
                        initialBody.getClass().getCanonicalName(),
                        EntityModel.class.getCanonicalName());
                  }
                }
              }
            });
  }

  @SuppressWarnings("unchecked")
  private void attachExpandedGetsBody(
      String expands,
      UriRouteMatch routeMatchCurrentResource,
      ResourceContract contract,
      EntityModel initialBody,
      boolean mustAddEntityId) {
    for (String expand : expands.trim().split(",")) {

      Optional<SubResource> potSubResource =
          contract.getSubresources().stream()
              .filter(subResource -> expand.equals(subResource.getName()))
              .findAny();

      if (!potSubResource.isPresent()) {
        log.debug(
            "Expand '{}' is not a sub resource of '{}'", expand, contract.getGeneral().getName());
        continue;
      }

      SubResource subResourceContract = potSubResource.get();

      if (!subResourceContract.isExpandable()) {
        log.debug("Sub resource '{}' is not expandable", subResourceContract.getName());
        continue;
      }

      String targetUri =
          UriPlaceholderReplacer.replacePlaceholders(
              subResourceContract.getHref(), routeMatchCurrentResource);

      Optional<UriRouteMatch<Object, Object>> routeMatch = router.GET(targetUri);

      if (routeMatch.isPresent()) {
        UriRouteMatch<Object, Object> routeMatchSubResource = routeMatch.get();
        ExecutableMethod<Object, Object> executableMethod =
            (ExecutableMethod<Object, Object>) routeMatchSubResource.getExecutableMethod();

        Class declaringType = executableMethod.getDeclaringType();

        Object bean = applicationContext.getBean(declaringType);

        List<Object> values =
            new ArrayList<>(routeMatchCurrentResource.getVariableValues().values());

        if (mustAddEntityId) {
          values.add(((Identifiable) initialBody.getData()).getId());
        }

        Object result = executableMethod.invoke(bean, values.toArray());

        Expand expandedData =
            result instanceof Collection
                ? new CollectionExpand(expand, (Collection<ResourceModel>) result)
                : new EntityExpand(expand, (ResourceModel) result);

        initialBody.getExpands().add(expandedData);
      }
    }
  }

  private Optional<ResourceContract> fetchContract(Object resourceBean) {

    Class<?> key = resourceBean.getClass();

    if (this.contractStore.containsKey(key)) {
      return Optional.of(this.contractStore.get(key));
    }

    try {
      Field contractField = resourceBean.getClass().getField("OPTIONS_CONTENT");
      String json = (String) contractField.get(resourceBean);

      ResourceContract contract = this.objectMapper.readValue(json, ResourceContract.class);

      this.contractStore.put(key, contract);

      return Optional.of(contract);

    } catch (IllegalAccessException | NoSuchFieldException | IOException ex) {
      log.error("Unable to read contract from class '{}'", resourceBean.getClass().getSimpleName());
      return Optional.empty();
    }
  }

  private static class UriPlaceholderReplacer {

    private UriPlaceholderReplacer() {
      // do not create instances of me please
    }

    /**
     * Replaces placeholder in a URI.
     *
     * <p>Given the following example:
     *
     * <pre>
     * /v1/countries/{:country}/cities/  => /v1/countries/CHE/cities/
     * </pre>
     *
     * @param uriWithPlaceholders URI template containing placeholders in path such as {@code
     *     {:country}}.
     * @param uriRouteMatch URI that matches the template.
     * @return a string with all placeholders resolved.
     */
    public static String replacePlaceholders(
        String uriWithPlaceholders, UriRouteMatch uriRouteMatch) {

      Map<String, Object> variableValues = uriRouteMatch.getVariableValues();
      StringBuilder sb = new StringBuilder(uriWithPlaceholders);

      for (String argumentName : uriRouteMatch.getArgumentNames()) {
        String regex = String.format("{:%s}", "id".equals(argumentName) ? "entity" : argumentName);

        int index = sb.indexOf(regex);
        sb.replace(index, index + regex.length(), String.valueOf(variableValues.get(argumentName)));
      }

      return sb.toString();
    }
  }
}
