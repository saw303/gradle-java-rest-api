package demo.app.micronaut;

import javax.inject.Singleton;
import java.util.UUID;

/**
 * @author Silvio Wangler
 */
@Singleton
public class LanderResourceDelegate {

	public Land yolo() {
		final Land land = new Land();
		land.setId(UUID.randomUUID().toString());
		land.setName("Switzerland");
		return land;
	}
}
