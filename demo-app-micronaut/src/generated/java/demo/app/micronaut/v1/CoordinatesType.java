package demo.app.micronaut.v1;

import java.io.Serializable;
import java.math.BigDecimal;

public class CoordinatesType implements Serializable {
  private BigDecimal longitude;

  private Integer latitude;

  public BigDecimal getLongitude() {
    return this.longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  public Integer getLatitude() {
    return this.latitude;
  }

  public void setLatitude(Integer latitude) {
    this.latitude = latitude;
  }
}
