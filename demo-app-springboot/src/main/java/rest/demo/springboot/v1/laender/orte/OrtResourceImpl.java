package rest.demo.springboot.v1.laender.orte;

import ch.silviowangler.rest.NotYetImplementedException;
import ch.silviowangler.rest.types.IdType;
import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrtResourceImpl extends OrtResource {
  @Override
  public ResponseEntity handleDeleteEntity(String id, String land) {
    throw new NotYetImplementedException();
  }

  @Override
  public Collection<OrtGetResourceModel> handleGetCollection(String land) {
    throw new NotYetImplementedException();
  }

  @Override
  public OrtGetResourceModel handleGetEntity(String id, String land) {
    throw new NotYetImplementedException();
  }

  @Override
  public ResponseEntity handleGetEntityJpeg(String id, String land) {
    throw new NotYetImplementedException();
  }

  @Override
  public IdType handleCreateEntity(OrtPostResourceModel model, String land) {
    throw new NotYetImplementedException();
  }

  @Override
  public IdType handleUpdateEntity(OrtPutResourceModel model, String id, String land) {
    throw new NotYetImplementedException();
  }
}
