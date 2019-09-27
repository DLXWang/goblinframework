package org.goblinframework.remote.core;

import org.goblinframework.api.core.GoblinException;

public class GoblinRemoteException extends GoblinException {
  private static final long serialVersionUID = 1691633674084083629L;

  public GoblinRemoteException() {
  }

  public GoblinRemoteException(String message) {
    super(message);
  }

  public GoblinRemoteException(String message, Throwable cause) {
    super(message, cause);
  }

  public GoblinRemoteException(Throwable cause) {
    super(cause);
  }
}