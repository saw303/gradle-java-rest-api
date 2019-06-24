package demo.app.micronaut.v1.countries;

import ch.silviowangler.rest.NotYetImplementedException;
import ch.silviowangler.rest.types.IdType;
import demo.app.micronaut.v1.countries.CountriesGetResourceModel;
import io.micronaut.http.HttpResponse;

import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class CountriesResourceDelegate {

    private final Map<String, CountriesGetResourceModel> models;

    public CountriesResourceDelegate() {
        this.models = new HashMap<>();
        CountriesGetResourceModel model = new CountriesGetResourceModel();

        model.setId("CHE");
        model.setName("Switzerland");
        model.setFoundationDate(LocalDate.of(1291, 8, 1));
        model.setSurface(Integer.MAX_VALUE);

        this.models.put(model.getId(), model);

        model = new CountriesGetResourceModel();

        model.setId("DEU");
        model.setName("Germany");
        model.setFoundationDate(LocalDate.of(1949, 5, 23));
        model.setSurface(Integer.MIN_VALUE);

        this.models.put(model.getId(), model);
    }

    public Collection<CountriesGetResourceModel> getCollection() {
        return this.models.values();
    }

    public CountriesGetResourceModel getEntity(String id) {
        return this.models.entrySet().stream().filter(r -> r.getKey().equals(id)).map(r -> r.getValue()).findAny().orElse(null);
    }

    public HttpResponse getEntityPdf(String id) {
        throw new NotYetImplementedException();
    }

    public IdType createEntity(CountriesPostResourceModel model) {
        String id = UUID.randomUUID().toString();

        CountriesGetResourceModel entity = new CountriesGetResourceModel();
        entity.setId(id);
        entity.setSurface(model.getSurface());
        entity.setFoundationDate(model.getFoundationDate());
        entity.setCoordinates(model.getCoordinates());
        entity.setName(model.getName());

        this.models.put(id, entity);

        return new IdType(id, "/v1/countries/" + id);
    }

    public IdType updateEntity(CountriesPutResourceModel model, String id) {
        throw new NotYetImplementedException();
    }
}
