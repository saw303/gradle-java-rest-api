package demo.app.micronaut.v1;

import ch.silviowangler.rest.NotYetImplementedException;
import jakarta.inject.Singleton;

@Singleton
public class RootResourceDelegate {
  public RootGetResourceModel getEntity() {
    throw new NotYetImplementedException();
  }
}
