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

import ch.silviowangler.rest.model.EntityModel;
import ch.silviowangler.rest.model.Expand;
import ch.silviowangler.rest.model.ResourceModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Enables "Expanded GET" requests.
 *
 * Without "Expanded Gets" a client has to execute at least two HTTP calls to read a person and its addresses.
 *
 * <ul>
 *     <li>/persons/123 - to read the person</li>
 *     <li>/persons/123/addresses/ - to read all addresses for that person</li>
 * </ul>
 *
 * With "Expanded Gets" a client can read the person and its addresses in only one request.
 *
 * <ul>
 *     <li>/persons/123?expands=addresses</li>
 * </ul>
 *
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

	public ExpandedGetResponseFilter(ApplicationContext applicationContext, Router router) {
		this.applicationContext = applicationContext;
		this.router = router;
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

				/*
				TODO expands are comma separated. iterate each expand
				TODO make sure the route is read from the resource spec section sub resources
				 */
				Optional<UriRouteMatch<Object, Object>> routeMatch = router.GET(request.getPath() + "/" + expands);

				if (routeMatch.isPresent()) {

					Object initialBody = res.body();

					if (initialBody instanceof EntityModel) {
						EntityModel entityModel = (EntityModel) initialBody;

						UriRouteMatch<Object, Object> perfectMatch = routeMatch.get();
						ExecutableMethod<Object, Object> executableMethod = (ExecutableMethod<Object, Object>) perfectMatch.getExecutableMethod();

						Class declaringType = executableMethod.getDeclaringType();

						Object bean = applicationContext.getBean(declaringType);

						Collection<Object> values = request.getAttributes().get("micronaut.http.route.match", UriRouteMatch.class).get().getVariableValues().values();

						Object result = executableMethod.invoke(bean, values.iterator().next().toString());

						entityModel.getExpands().add(new Expand("huhu", (List<ResourceModel>) result));

						((MutableHttpResponse) res).body(entityModel);
					}
				}
			}
		});
	}
}
