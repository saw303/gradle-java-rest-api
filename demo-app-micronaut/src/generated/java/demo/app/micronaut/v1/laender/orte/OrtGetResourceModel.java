package demo.app.micronaut.v1.laender.orte;

import ch.silviowangler.rest.model.ResourceModel;
import demo.app.micronaut.v1.CoordinatesType;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Generated;

@Generated(
    date = "2019-02-11T14:56:02.325Z",
    comments = "Specification filename: land.ort.v1.json",
    value = "ch.silviowangler.restapi"
)
public class OrtGetResourceModel implements Serializable, ResourceModel {
  public static final String TYPE = "application/ch.silviowangler.ort";

  private String id;

  private String name;

  private CoordinatesType koordinaten;

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CoordinatesType getKoordinaten() {
    return this.koordinaten;
  }

  public void setKoordinaten(CoordinatesType koordinaten) {
    this.koordinaten = koordinaten;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (! (other instanceof OrtGetResourceModel)) return false;
    OrtGetResourceModel that = (OrtGetResourceModel) other;
    return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getKoordinaten(), that.getKoordinaten());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, koordinaten);
  }
}
