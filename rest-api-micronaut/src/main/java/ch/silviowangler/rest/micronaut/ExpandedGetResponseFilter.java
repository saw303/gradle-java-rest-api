/**
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
import ch.silviowangler.rest.model.EntityModel;
import ch.silviowangler.rest.model.Expand;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Enables "Expanded GET" requests.
 * <p>
 * Without "Expanded Gets" a client has to execute at least two HTTP calls to read a person and its addresses.
 *
 * <ul>
 * <li>/persons/123 - to read the person</li>
 * <li>/persons/123/addresses/ - to read all addresses for that person</li>
 * </ul>
 * <p>
 * With "Expanded Gets" a client can read the person and its addresses in only one request.
 *
 * <ul>
 * <li>/persons/123?expands=addresses</li>
 * </ul>
 * <p>
 * This filter modifies the response after {@link HateoasResponseFilter} has finished its work.
 *
 * @author Silvio Wangler
 */
@Filter("${restapi.hateoas.filter.uri}")
@Requires(property = "restapi.hateoas.filter.enabled")
public class ExpandedGetResponseFilter implements HttpServerFilter {

	private final ApplicationContext applicationContext;
	private final Router router;
	private final static String EXPAND_PARAM_NAME = "expands";
	private Map<Class, ResourceContract> contractStore;
	private static final Logger log = getLogger(ExpandedGetResponseFilter.class);
	private final ObjectMapper objectMapper;
	private final UriPlaceholderReplacer uriPlaceholderReplacer;

	public ExpandedGetResponseFilter(ApplicationContext applicationContext, Router router, ObjectMapper objectMapper) {
		this.applicationContext = applicationContext;
		this.router = router;
		this.objectMapper = objectMapper;
		this.contractStore = new HashMap<>();
		this.uriPlaceholderReplacer = new UriPlaceholderReplacer();
	}

	@Override
	public int getOrder() {
		return FilterOrder.EXPANDED_GETS;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {

		return Flowable.fromPublisher(chain.proceed(request)).doOnNext(res -> {

			if (request.getMethod().equals(HttpMethod.GET) && request.getParameters().contains(EXPAND_PARAM_NAME)) {
				String expands = request.getParameters().get(EXPAND_PARAM_NAME);

				UriRouteMatch routeMatchCurrentResource = request.getAttributes().get(HttpAttributes.ROUTE_MATCH, UriRouteMatch.class).get();
				Object currentResource = applicationContext.getBean(routeMatchCurrentResource.getExecutableMethod().getDeclaringType());

				ResourceContract contract = fetchContract(currentResource).get();

				for (String expand : expands.split(",")) {

					Optional<SubResource> potSubResource = contract.getSubresources().stream().filter(subResource -> expand.equals(subResource.getName())).findAny();

					if (!potSubResource.isPresent()) {
						log.debug("Expand '{}' is not a sub resource of '{}'", expand, contract.getGeneral().getName());
						continue;
					}

					SubResource subResourceContract = potSubResource.get();

					if (!subResourceContract.isExpandable()) {
						log.debug("Sub resource '{}' is not expandable", subResourceContract.getName());
						continue;
					}

					String targetUri = uriPlaceholderReplacer.replacePlaceholders(subResourceContract.getHref(), routeMatchCurrentResource);

					Optional<UriRouteMatch<Object, Object>> routeMatch = router.GET(targetUri);

					if (routeMatch.isPresent()) {

						Object initialBody = res.body();

						if (initialBody instanceof EntityModel) {
							EntityModel entityModel = (EntityModel) initialBody;

							UriRouteMatch<Object, Object> routeMatchSubResource = routeMatch.get();
							ExecutableMethod<Object, Object> executableMethod = (ExecutableMethod<Object, Object>) routeMatchSubResource.getExecutableMethod();

							Class declaringType = executableMethod.getDeclaringType();

							Object bean = applicationContext.getBean(declaringType);

							Collection<Object> values = routeMatchCurrentResource.getVariableValues().values();

							Object result = executableMethod.invoke(bean, values.toArray());

							entityModel.getExpands().add(new Expand(expand, (List<ResourceModel>) result));

							((MutableHttpResponse) res).body(entityModel);
						}
					}
				}
			}
		});
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

		/**
		 * Replaces placeholder in a URI.
		 * <p>
		 * Given the following example:
		 *
		 * <pre>
		 * /v1/countries/{:country}/cities/  => /v1/countries/CHE/cities/
		 * </pre>
		 *
		 * @param uriWithPlaceholders
		 * @param uriRouteMatch
		 * @return a string with all placeholders resolved.
		 */
		public String replacePlaceholders(String uriWithPlaceholders, UriRouteMatch uriRouteMatch) {

			Map<String, Object> variableValues = uriRouteMatch.getVariableValues();
			for (String argumentName : uriRouteMatch.getArgumentNames()) {
				uriWithPlaceholders = uriWithPlaceholders.replaceFirst("\\{:\\w*\\}", String.valueOf(variableValues.get(argumentName)));
			}
			return uriWithPlaceholders;
		}
	}
}
