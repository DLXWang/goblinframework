package org.goblinframework.api.system;

import org.goblinframework.api.annotation.External;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@External
public interface IModule {

  @NotNull
  String name();

  @Nullable
  default String managementEntrance() {
    return null;
  }

  default void install(@NotNull ModuleInstallContext ctx) {
  }

  default void initialize(@NotNull ModuleInitializeContext ctx) {
  }

  default void finalize(@NotNull ModuleFinalizeContext ctx) {
  }
}