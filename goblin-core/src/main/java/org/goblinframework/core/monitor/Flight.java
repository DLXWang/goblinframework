package org.goblinframework.core.monitor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public interface Flight {

  @NotNull
  FlightId flightId();

  @NotNull
  default StartPoint startPoint() {
    return location().startPoint();
  }

  @NotNull
  FlightLocation location();

  @NotNull
  Instant startTime();

  @Nullable
  Instant stopTime();

  default long durationMillis() {
    Instant startTime = startTime();
    Instant stopTime = stopTime();
    if (stopTime == null) {
      throw new IllegalStateException();
    }
    return stopTime.toEpochMilli() - startTime.toEpochMilli();
  }

  @Nullable
  Object attribute(@NotNull String name);

  enum StartPoint {
    UTM,    // UnitTestMethod
    CTK,    // CronTask
    PRG     // Programmatic
  }

  interface Aware {
    void setFlight(@NotNull Flight flight);
  }

}
