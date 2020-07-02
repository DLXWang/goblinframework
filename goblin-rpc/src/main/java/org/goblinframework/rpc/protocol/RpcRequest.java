package org.goblinframework.rpc.protocol;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class RpcRequest implements Serializable {
  private static final long serialVersionUID = 1108921507796393958L;

  public String serviceInterface;
  public String serviceVersion;
  public String methodName;
  public String[] parameterTypes;
  public String returnType;
  public Object[] arguments;
  public long timeout;
  public boolean jsonMode;
  public LinkedHashMap<String, Object> extensions;

}
