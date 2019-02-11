package demo.app.micronaut.v1.laender.orte;

import ch.silviowangler.rest.NotYetImplementedException;
import ch.silviowangler.rest.types.IdType;
import io.micronaut.http.HttpResponse;

import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class OrtResourceDelegate {
  public HttpResponse deleteEntity(String id, String land) {
    throw new NotYetImplementedException();
  }

  public Collection<OrtGetResourceModel> getCollection(String land) {
    throw new NotYetImplementedException();
  }

  public OrtGetResourceModel getEntity(String id, String land) {
    throw new NotYetImplementedException();
  }

  public HttpResponse getEntityJpeg(String id, String land) {
    throw new NotYetImplementedException();
  }

  public IdType createEntity(OrtPostResourceModel model, String land) {
    throw new NotYetImplementedException();
  }

  public IdType updateEntity(OrtPutResourceModel model, String id, String land) {
    throw new NotYetImplementedException();
  }
}
