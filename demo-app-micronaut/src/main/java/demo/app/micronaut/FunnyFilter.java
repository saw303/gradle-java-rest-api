package demo.app.micronaut;

import ch.silviowangler.rest.micronaut.JsonModelFilter;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

/**
 * @author Silvio Wangler
 */
@Filter("/v1**")
public class FunnyFilter implements HttpServerFilter {

	private final JsonModelFilter jsonModelFilter;

	public FunnyFilter() {
		this.jsonModelFilter = new JsonModelFilter();
	}

	@Override
	public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
		return jsonModelFilter.doFilter(request, chain);
	}
}
