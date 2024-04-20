package org.example;

public final class ProfilingUtils {
  private ProfilingUtils() {}

  public static <T> ProfilingResult<T> profile(Profilable<T> block) {
    var start = System.currentTimeMillis();
    T result = block.run();
    return new ProfilingResult<>(result, System.currentTimeMillis() - start);
  }

  public static record ProfilingResult<T>(T value, long time) {}

  public interface Profilable<T> {
    T run();
  }
}
