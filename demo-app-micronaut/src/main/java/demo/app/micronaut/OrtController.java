package demo.app.micronaut;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

/**
 * @author Silvio Wangler
 */
@Controller("/api/v1/laender/{land}/orte")
public class OrtController {

	@Get(uri = "/{id}")
	public String get(String id, String land) {
		return String.format("ID %s, land %s", id, land);
	}
}
