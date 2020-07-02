package org.goblinframework.core.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.Validate;
import org.bson.types.ObjectId;
import org.goblinframework.api.spi.IRandomProvider;
import org.goblinframework.core.service.ServiceInstaller;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Random;

abstract public class RandomUtils {

  private static final Random RANDOM;

  static {
    Random random;
    IRandomProvider provider = ServiceInstaller.firstOrNull(IRandomProvider.class);
    if (provider != null) {
      random = provider.getRandom();
    } else {
      random = initializeRandom();
    }
    RANDOM = random;
  }

  private static Random initializeRandom() {
    try {
      SecureRandom ran = SecureRandom.getInstance("SHA1PRNG");
      byte[] seed = ran.generateSeed(20);
      return new SecureRandom(seed);
    } catch (Exception ex) {
      return new Random();
    }
  }

  @NotNull
  public static Random getRandom() {
    return RANDOM;
  }

  @NotNull
  public static String nextObjectId() {
    return new ObjectId().toHexString();
  }

  public static boolean nextBoolean() {
    return RANDOM.nextBoolean();
  }

  public static int nextInt() {
    return RANDOM.nextInt();
  }

  public static long nextLong() {
    return RANDOM.nextLong();
  }

  public static byte[] nextBytes(final int count) {
    Validate.isTrue(count >= 0, "Count cannot be negative.");
    final byte[] result = new byte[count];
    RANDOM.nextBytes(result);
    return result;
  }

  public static String randomAlphabetic(final int count) {
    return RandomStringUtils.random(count, 0, 0, true, false, null, RANDOM);
  }

  public static String randomAlphanumeric(final int count) {
    return RandomStringUtils.random(count, 0, 0, true, true, null, RANDOM);
  }
}
