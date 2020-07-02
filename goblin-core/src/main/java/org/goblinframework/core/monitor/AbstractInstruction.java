package org.goblinframework.core.monitor;

import org.goblinframework.core.event.EventBus;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

abstract public class AbstractInstruction implements Instruction {

  private final Id id;
  private final Mode mode;
  private final String threadName;

  private Instant startTime;
  private Instant stopTime;
  private Instant completeTime;

  public AbstractInstruction(@NotNull Id id, @NotNull Mode mode, boolean autoStart) {
    if (mode == Mode.DOT) {
      throw new IllegalArgumentException();
    }
    this.id = id;
    this.mode = mode;
    this.threadName = Thread.currentThread().getName();
    if (autoStart) {
      start();
    }
  }

  @Override
  public Id id() {
    return id;
  }

  @Override
  public Mode mode() {
    return mode;
  }

  @Override
  public Instant startTime() {
    return startTime;
  }

  @Override
  public Instant stopTime() {
    return stopTime;
  }

  @Override
  public Instant completeTime() {
    switch (mode) {
      case SYN:
        return stopTime;
      case ASY:
        return completeTime;
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public long actualDurationMillis() {
    if (startTime == null || stopTime == null) {
      throw new IllegalStateException();
    }
    return stopTime.toEpochMilli() - startTime.toEpochMilli();
  }

  @Override
  public long effectiveDurationMillis() {
    switch (mode) {
      case SYN:
        return actualDurationMillis();
      case ASY: {
        if (startTime == null || completeTime == null) {
          throw new IllegalStateException();
        }
        return completeTime.toEpochMilli() - startTime.toEpochMilli();
      }
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public boolean isStarted() {
    return startTime != null;
  }

  @Override
  public boolean isStopped() {
    return stopTime != null;
  }

  @Override
  public boolean isCompleted() {
    if (mode == Mode.SYN) {
      return isStopped();
    }
    return completeTime != null;
  }

  @Override
  public void start() {
    if (startTime == null) {
      startTime = Instant.now();
      IFlightMonitor monitor = FlightRecorder.getFlightMonitor();
      if (monitor != null) {
        FlightId flightId = FlightRecorder.currentThreadFlightId();
        monitor.attachFlight(flightId, this);
      }
    }
  }

  @Override
  public void stop() {
    if (stopTime == null) {
      stopTime = Instant.now();
      if (mode == Mode.SYN) {
        InstructionEvent event = new InstructionEvent(this);
        EventBus.publish(event);
      }
    }
  }

  @Override
  public void complete() {
    if (mode == Mode.SYN) {
      stop();
      return;
    }
    if (completeTime == null) {
      completeTime = Instant.now();
      InstructionEvent event = new InstructionEvent(this);
      EventBus.publish(event);
    }
  }

  @NotNull
  @Override
  public String asLongText() {
    switch (mode) {
      case SYN:
        return String.format("%s:%s %sms {%s}",
            id(), mode(), effectiveDurationMillis(), threadName);
      case ASY: {
        if (isCompleted()) {
          return String.format("%s:%s %sms (%sms) {%s}",
              id(), mode(), actualDurationMillis(), effectiveDurationMillis(), threadName);
        } else {
          return String.format("%s:%s %sms (UNCOMPLETED) {%s}",
              id(), mode(), actualDurationMillis(), threadName);
        }
      }
      default:
        throw new UnsupportedOperationException();
    }
  }

  @NotNull
  @Override
  public String asShortText() {
    switch (mode) {
      case SYN:
        return String.format("%s:%s(%s)", id(), mode(), effectiveDurationMillis());
      case ASY: {
        if (isCompleted()) {
          return String.format("%s:%s(%s.%s)", id(), mode(), actualDurationMillis(), effectiveDurationMillis());
        } else {
          return String.format("%s:%s(%s.UC)", id(), mode(), actualDurationMillis());
        }
      }
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public void close() {
    stop();
  }
}
