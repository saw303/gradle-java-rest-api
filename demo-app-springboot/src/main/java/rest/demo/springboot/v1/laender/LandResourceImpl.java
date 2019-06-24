package rest.demo.springboot.v1.laender;

import ch.silviowangler.rest.NotYetImplementedException;
import ch.silviowangler.rest.types.IdType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
public class LandResourceImpl extends LandResource {

	private final List<LandGetResourceModel> countries;

	public LandResourceImpl() {
		this.countries = new ArrayList<>();

		for (int i=0; i < 10; i++) {
			this.countries.add(buildCountry(String.format("Country %s", i), LocalDate.now().minusYears(i)));
		}
	}

	private LandGetResourceModel buildCountry(String name, LocalDate foundationDate) {
		LandGetResourceModel country = new LandGetResourceModel();
		country.setFlaeche(Integer.MAX_VALUE);
		country.setGruendungsDatum(foundationDate);
		country.setName(name);
		country.setId(UUID.nameUUIDFromBytes(name.getBytes()).toString());
		return country;
	}


	@Override
	public Collection<LandGetResourceModel> handleGetCollection() {
		return this.countries;
	}

	@Override
	public LandGetResourceModel handleGetEntity(String id) {
		return this.countries.stream()
				.filter(c -> id.equals(c.getId()))
				.findAny()
				.orElse(null);
	}

	@Override
	public ResponseEntity handleGetEntityPdf(String id) {
		throw new NotYetImplementedException();
	}

	@Override
	public IdType handleCreateEntity(LandPostResourceModel model) {
		throw new NotYetImplementedException();
	}

	@Override
	public IdType handleUpdateEntity(LandPutResourceModel model, String id) {
		throw new NotYetImplementedException();
	}
}
