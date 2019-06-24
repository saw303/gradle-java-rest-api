package demo.app.micronaut.v1;

import ch.silviowangler.rest.NotYetImplementedException;
import javax.inject.Singleton;

@Singleton
public class RootResourceDelegate {
  public RootGetResourceModel getEntity() {
    throw new NotYetImplementedException();
  }
}
