package org.goblinframework.registry.listener;

import org.I0Itec.zkclient.IZkDataListener;
import org.jetbrains.annotations.NotNull;

final public class ZookeeperDataListener implements IZkDataListener {

  @NotNull private final RegistryDataListener listener;

  public ZookeeperDataListener(@NotNull RegistryDataListener listener) {
    this.listener = listener;
  }

  @Override
  public void handleDataChange(String dataPath, Object data) throws Exception {
    listener.onDataChanged(dataPath, data);
  }

  @Override
  public void handleDataDeleted(String dataPath) throws Exception {
    listener.onDataDeleted(dataPath);
  }
}
