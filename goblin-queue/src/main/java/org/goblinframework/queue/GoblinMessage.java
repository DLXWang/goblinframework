package org.goblinframework.queue;

import org.goblinframework.core.util.RandomUtils;

public class GoblinMessage {

  private String id;

  private Object data;

  private GoblinMessageSerializer serializer;

  private GoblinMessage(String id) {
    this.id = id;
  }

  public static GoblinMessage create() {
    return new GoblinMessage(RandomUtils.nextObjectId());
  }

  public GoblinMessage data(Object data) {
    this.data = data;
    return this;
  }

  public GoblinMessage serializer(GoblinMessageSerializer serializer) {
    this.serializer = serializer;
    return this;
  }

  public enum GoblinMessageSerializer {
    HESSIAN2,
    JSON,
    FST,
    JAVA,
  }
}