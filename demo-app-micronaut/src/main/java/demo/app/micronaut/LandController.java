package demo.app.micronaut;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Options;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;

import javax.annotation.Generated;
import javax.inject.Inject;

/**
 * @author Silvio Wangler
 */
@Generated("hjdh")
@Controller("/api/v1/laender")
public class LandController {

	private final LanderResourceDelegate delegate;

	@Inject
	public LandController(LanderResourceDelegate delegate) {
		this.delegate = delegate;
	}

	@Options(uri = "/{id}/hello")
	@Consumes("application/json")
	@Produces("application/json")
	public String options(String id) {
		return "options";
	}

	@Get
	@Consumes("application/json")
	@Produces("application/json")
	public Land yolo() {
		return delegate.yolo();
	}
}
