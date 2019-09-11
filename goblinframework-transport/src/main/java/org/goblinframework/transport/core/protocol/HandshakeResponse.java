package org.goblinframework.transport.core.protocol;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class HandshakeResponse implements Serializable {
  private static final long serialVersionUID = -3936150304695648862L;

  public boolean success;
  public LinkedHashMap<String, Object> extensions;

}