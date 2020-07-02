package org.goblinframework.embedded.server;

import org.goblinframework.embedded.setting.ServerSetting;
import org.jetbrains.annotations.NotNull;

public interface EmbeddedServerFactory {

  @NotNull
  EmbeddedServerMode mode();

  @NotNull
  EmbeddedServer createEmbeddedServer(@NotNull ServerSetting setting);

}
