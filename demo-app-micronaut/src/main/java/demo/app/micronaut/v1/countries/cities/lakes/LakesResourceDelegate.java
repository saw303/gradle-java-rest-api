package demo.app.micronaut.v1.countries.cities.lakes;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Singleton
public class LakesResourceDelegate {

    private static List<LakesGetResourceModel> models;

    static {
        LakesGetResourceModel model = new LakesGetResourceModel();

        model.setId(UUID.randomUUID().toString());
        model.setName("A Lake");

        models = List.of(model);
    }


    public Collection<LakesGetResourceModel> getCollection(String country, String city) {
        return models;
    }

    public LakesGetResourceModel getEntity(String id, String country, String city) {
        return models.get(0);
    }
}
