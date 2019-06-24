package demo.app.micronaut.v1.countries.cities.districts;

import ch.silviowangler.rest.NotYetImplementedException;
import ch.silviowangler.rest.types.IdType;
import io.micronaut.http.HttpResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class DistrictsResourceDelegate {

  private Map<String, DistrictsGetResourceModel> districts;

  public DistrictsResourceDelegate() {
    this.districts = new HashMap<>();

    DistrictsGetResourceModel albisrieden = new DistrictsGetResourceModel();
    albisrieden.setId("ZH9");
    albisrieden.setName("Albisrieden");
    this.districts.put(albisrieden.getId(), albisrieden);
  }

  public void deleteEntity(String id, String country, String city) {
    throw new NotYetImplementedException();
  }

  public Collection<DistrictsGetResourceModel> getCollection(String country, String city) {
    return this.districts.values();
  }

  public DistrictsGetResourceModel getEntity(String id, String country, String city) {
    return this.districts.get(id);
  }

  public HttpResponse getEntityJpeg(String id, String country, String city) {
    throw new NotYetImplementedException();
  }

  public IdType createEntity(DistrictsPostResourceModel model, String country, String city) {
    throw new NotYetImplementedException();
  }

  public IdType updateEntity(DistrictsPutResourceModel model, String id, String country,
      String city) {
    throw new NotYetImplementedException();
  }
}
