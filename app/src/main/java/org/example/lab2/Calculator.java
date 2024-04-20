package org.example.lab2;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public sealed interface Calculator {
  public int min(int[] arr);

  public final class Parallel implements Calculator {
    private static final Map<Integer, Calculator> CACHE = new ConcurrentHashMap<>();
    private static final ThreadFactory FACTORY = Thread::new;
    private final int preferableCount;
    private final ThreadFactory factory;

    private Parallel(int preferableCount, ThreadFactory factory) {
      this.preferableCount = preferableCount;
      this.factory = factory;
    }

    public static Calculator newInstance(int count) {
      return CACHE.computeIfAbsent(count, _key -> newInstance(count, FACTORY));
    }

    public static Calculator newInstance(int count, ThreadFactory factory) {
      Objects.requireNonNull(factory);
      Preconditions.checkArgument(count > 0, "Count should be 1 or greater");

      if (count == 1) return Linear.INSTANCE;

      if (count > 100) return JavaParallel.INSTANCE;

      return new Parallel(count, factory);
    }

    @Override
    public int min(int[] arr) {
      Preconditions.checkArgument(arr.length != 0, "Array should not be empty");

      final Min<Integer> min = new Min<>(arr[0]);
      final int threadCount = Math.min(arr.length, preferableCount);
      final int step = arr.length / threadCount;
      final int rest = arr.length % threadCount;
      final CountDownLatch latch = new CountDownLatch(threadCount);

      for (int i = 0; i < threadCount - 1; i++) {
        final int _i = i;
        factory
            .newThread(
                () -> {
                  int start = _i * step;
                  final int end = (_i + 1) * step;

                  int localMin = arr[start];

                  for (; start < end; start++) {
                    if (arr[start] < localMin) localMin = arr[start];
                  }

                  min.compareAndSet(localMin);
                  latch.countDown();
                })
            .start();
      }

      factory
          .newThread(
              () -> {
                int start = (threadCount - 1) * step;
                final int end = (threadCount * step) + rest;

                int localMin = arr[start];

                for (; start < end; start++) {
                  if (arr[start] < localMin) localMin = arr[start];
                }

                min.compareAndSet(localMin);
                latch.countDown();
              })
          .start();

      try {
        latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      return min.value();
    }

    private static class Min<T extends Comparable<T>> {
      private final AtomicReference<T> value;

      public Min(T initialValue) {
        this.value = new AtomicReference<>(initialValue);
      }

      public void compareAndSet(T newValue) {
        value.updateAndGet(current -> newValue.compareTo(current) < 0 ? newValue : current);
      }

      public T value() {
        return value.get();
      }
    }
  }

  public final class Linear implements Calculator {
    public static final Linear INSTANCE = new Linear();

    private Linear() {}

    @Override
    public int min(int[] arr) {
      if (arr.length == 0) throw new IllegalArgumentException("Array should not be empty");

      int min = arr[0];

      for (var el : arr) {
        if (el < min) min = el;
      }

      return min;
    }
  }

  public final class JavaParallel implements Calculator {
    public static final JavaParallel INSTANCE = new JavaParallel();

    private JavaParallel() {}

    @Override
    public int min(int[] arr) {
      return Arrays.stream(arr)
          .parallel()
          .min()
          .orElseThrow(() -> new IllegalArgumentException("Array should not be empty"));
    }
  }
}
