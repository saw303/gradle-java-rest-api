package rest.demo.springboot.v1;

import ch.silviowangler.rest.NotYetImplementedException;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RootResourceImpl extends RootResource {
  @Override
  public RootGetResourceModel handleGetEntity() {
	  RootGetResourceModel model = new RootGetResourceModel();
	  model.setId(UUID.nameUUIDFromBytes("Hello World".getBytes()).toString());
	  model.setName("Hello World");
	  return model;
  }
}
