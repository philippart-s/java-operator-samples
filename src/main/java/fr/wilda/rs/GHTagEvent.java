package fr.wilda.rs;

import java.io.Serializable;

public class GHTagEvent implements Serializable {
  private String ref;
  private String ref_type;
  
  public String getRef() {
    return ref;
  }
  public void setRef(String ref) {
    this.ref = ref;
  }
  public String getRef_type() {
    return ref_type;
  }
  public void setRef_type(String ref_type) {
    this.ref_type = ref_type;
  }
}