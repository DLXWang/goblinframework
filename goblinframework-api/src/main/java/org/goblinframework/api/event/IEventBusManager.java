package org.goblinframework.api.event;

import org.goblinframework.api.annotation.Internal;
import org.jetbrains.annotations.NotNull;

@Internal
public interface IEventBusManager {

  void subscribe(@NotNull GoblinEventListener listener);

  void subscribe(@NotNull String channel, @NotNull GoblinEventListener listener);

  void unsubscribe(@NotNull GoblinEventListener listener);

  void unsubscribe(@NotNull String channel, @NotNull GoblinEventListener listener);

  @NotNull
  GoblinEventFuture publish(@NotNull GoblinEvent event);

  @NotNull
  GoblinEventFuture publish(@NotNull String channel, @NotNull GoblinEvent event);

  @NotNull
  <E> GoblinCallbackFuture<E> execute(@NotNull GoblinCallback<E> callback);

  @NotNull
  static IEventBusManager instance() {
    IEventBusManager manager = EventBusManagerInstaller.INSTALLED;
    if (manager == null) {
      throw new GoblinEventException("No IEventBusManager installed");
    }
    return manager;
  }
}
