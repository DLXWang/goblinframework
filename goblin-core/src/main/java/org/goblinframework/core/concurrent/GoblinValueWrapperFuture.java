package org.goblinframework.core.concurrent;

import org.goblinframework.api.concurrent.GoblinFuture;
import org.goblinframework.api.concurrent.GoblinFutureListener;
import org.goblinframework.api.function.ValueWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GoblinValueWrapperFuture<T> implements GoblinFuture<T>, ValueWrapper<T>, Serializable {
  private static final long serialVersionUID = -3131974041565144600L;

  @Nullable private final T value;

  public GoblinValueWrapperFuture(@Nullable T value) {
    this.value = value;
  }

  @Nullable
  @Override
  public T getValue() {
    return value;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    return getValue();
  }

  @Override
  public T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return getValue();
  }

  @Override
  public T getUninterruptibly() {
    return getValue();
  }

  @Override
  public T getUninterruptibly(long timeout, @NotNull TimeUnit unit) {
    return getValue();
  }

  @Override
  public GoblinFuture<T> awaitUninterruptibly() {
    return this;
  }

  @Override
  public GoblinFuture<T> awaitUninterruptibly(long timeout, @NotNull TimeUnit unit) {
    return this;
  }

  @Override
  public void addListener(@NotNull GoblinFutureListener<T> listener) {
    listener.futureCompleted(this);
  }

  @Override
  public void removeListener(@NotNull GoblinFutureListener<T> listener) {
  }

  @Override
  public GoblinFuture<T> complete(@Nullable T result) {
    return this;
  }

  @Override
  public GoblinFuture<T> complete(@Nullable T result, @Nullable Throwable cause) {
    return this;
  }
}
