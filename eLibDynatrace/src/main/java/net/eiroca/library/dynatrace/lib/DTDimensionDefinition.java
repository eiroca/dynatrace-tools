package net.eiroca.library.dynatrace.lib;

public class DTDimensionDefinition extends DTObject {

  public static final DTDimensionDefinition EMPTY = new DTDimensionDefinition("", "");

  public DTDimensionDefinition(String splitGroup, String splitName) {
    id = splitGroup;
    name = splitName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

}
