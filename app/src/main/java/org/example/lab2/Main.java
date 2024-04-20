package org.example.lab2;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.example.ProfilingUtils;
import org.example.ProfilingUtils.ProfilingResult;

public class Main {
  public static void main(String[] args) {
    final int size = 300_000_000;
    final Random random = ThreadLocalRandom.current();

    int[] array =
        IntStream.generate(() -> random.nextInt(0, Integer.MAX_VALUE)).limit(size).toArray();

    array[random.nextInt(0, size)] = -1;

    {
      ProfilingResult<Integer> result =
          ProfilingUtils.profile(() -> Calculator.JavaParallel.INSTANCE.min(array));
      System.out.printf("JavaParallel Result %s, time %s%n", result.value(), result.time());
    }

    {
      ProfilingResult<Integer> result =
          ProfilingUtils.profile(() -> Calculator.Linear.INSTANCE.min(array));
      System.out.printf("Linear Result %s, time %s%n", result.value(), result.time());
    }

    {
      ProfilingResult<Integer> result =
          ProfilingUtils.profile(() -> Calculator.Parallel.newInstance(10).min(array));
      System.out.printf("Parallel Result %s, time %s%n", result.value(), result.time());
    }
  }
}
