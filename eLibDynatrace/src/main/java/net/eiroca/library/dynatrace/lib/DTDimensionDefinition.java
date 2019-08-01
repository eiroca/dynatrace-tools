package net.eiroca.library.dynatrace.lib;

public class DTDimensionDefinition extends DTObject {

  public DTDimensionDefinition(final String splitGroup) {
    id = splitGroup;
    name = splitGroup;
  }

  public static final DTDimensionDefinition EMPTY = new DTDimensionDefinition("");

}
