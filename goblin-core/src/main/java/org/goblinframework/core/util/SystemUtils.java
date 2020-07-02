package org.goblinframework.core.util;

import org.apache.commons.lang3.Validate;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

abstract public class SystemUtils extends org.apache.commons.lang3.SystemUtils {

  private static final boolean testRunnerFound;
  private static final boolean nettyFound;

  static {
    boolean found;
    try {
      ClassUtils.loadClass("org.goblinframework.test.listener.TestContextDelegator");
      found = true;
    } catch (ClassNotFoundException ex) {
      found = false;
    }
    testRunnerFound = found;

    try {
      _ClassUtilsKt.loadClass("io.netty.util.Version", false);
      found = true;
    } catch (ClassNotFoundException ex) {
      found = false;
    }
    nettyFound = found;
  }

  public static boolean isTestRunnerFound() {
    return testRunnerFound;
  }

  public static boolean isNettyFound() {
    return nettyFound;
  }

  public static int availableProcessors() {
    return Runtime.getRuntime().availableProcessors();
  }

  public static int estimateThreads() {
    int processors = Runtime.getRuntime().availableProcessors();
    return Math.max(processors, 2);
  }

  public static int estimateThreads(int threads) {
    Validate.isTrue(threads >= 0);
    return threads == 0 ? estimateThreads() : threads;
  }

  public static int getJvmPID() {
    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    String name = runtime.getName();
    return Integer.parseInt(name.substring(0, name.indexOf("@")));
  }
}
