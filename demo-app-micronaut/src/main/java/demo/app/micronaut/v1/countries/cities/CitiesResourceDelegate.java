package demo.app.micronaut.v1.countries.cities;

import ch.silviowangler.rest.NotYetImplementedException;
import ch.silviowangler.rest.types.IdType;
import demo.app.micronaut.v1.CoordinatesType;
import io.micronaut.http.HttpResponse;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class CitiesResourceDelegate {

    private Map<String, List<CitiesGetResourceModel>> store;


    public CitiesResourceDelegate() {
        this.store = new HashMap<>();

        this.store.put("CHE", new ArrayList<>());

        this.store.get("CHE").add(buildOrt("ZH", "Zurich"));
        this.store.get("CHE").add(buildOrt("BE", "Berne"));
        this.store.get("CHE").add(buildOrt("GE", "Geneva"));
        this.store.get("CHE").add(buildOrt("LVA", "Lugano"));
    }

    private CitiesGetResourceModel buildOrt(String id, String name) {
        CitiesGetResourceModel model = new CitiesGetResourceModel();
        model.setId(id);
        model.setName(name);
        CoordinatesType koordinaten = new CoordinatesType();
        koordinaten.setLatitude(Integer.MAX_VALUE);
        koordinaten.setLongitude(BigDecimal.TEN);
        model.setCoordinates(koordinaten);
        return model;
    }

    public HttpResponse deleteEntity(String id, String land) {

        return null;
    }

    public Collection<CitiesGetResourceModel> getCollection(String land) {
        return this.store.getOrDefault(land, Collections.EMPTY_LIST);
    }

    public CitiesGetResourceModel getEntity(String id, String land) {
        return this.store.get(land).stream().filter(o -> o.getId().equals(id)).findAny().orElse(null);
    }

    public HttpResponse getEntityJpeg(String id, String land) {
        throw new NotYetImplementedException();
    }

    public IdType createEntity(CitiesPostResourceModel model, String land) {
        throw new NotYetImplementedException();
    }

    public IdType updateEntity(CitiesPutResourceModel model, String id, String land) {
        throw new NotYetImplementedException();
    }
}
