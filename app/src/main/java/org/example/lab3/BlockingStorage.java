package org.example.lab3;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.function.IntFunction;

import com.google.common.base.Preconditions;
import static org.example.Syntax._for;
import static org.example.Throw.suppress;

public final class BlockingStorage<T> {
  private final Semaphore space;
  private final Semaphore items;
  private final Queue<T> storage;

  public BlockingStorage(int size) {
    space = new Semaphore(size);
    items = new Semaphore(0);
    storage = new LinkedList<>();
  }

  public void add(T element) {
    space.acquireUninterruptibly();
    storage.add(element);
    items.release();
  }

  public T get() {
    items.acquireUninterruptibly();
    T element = storage.poll();
    space.release();
    return element;
  }

  public static <T> Thread consumer(int count, BlockingStorage<T> storage, Phaser phaser) {
    Preconditions.checkArgument(storage != null);
    Preconditions.checkArgument(phaser != null);
    Preconditions.checkArgument(!phaser.isTerminated());

    return new Thread(
        () -> {
          phaser.register();
          _for(
              count,
              i -> {
                System.out.printf("Consumer tries to take element %s%n", i);
                var element = storage.get();
                System.out.printf("Consumer successfully took element %s%n", element);
                if (i == 5) {
                  suppress(() -> Thread.sleep(1_000));
                } else {
                  suppress(() -> Thread.sleep(200));
                }
              });

          System.out.println("Consumer finished");
          phaser.arrive();
        });
  }

  public static <T> Thread producer(
      int count, BlockingStorage<T> storage, IntFunction<T> elementFactory, Phaser phaser) {
    Preconditions.checkArgument(storage != null);
    Preconditions.checkArgument(phaser != null);
    Preconditions.checkArgument(!phaser.isTerminated());

    return new Thread(
        () -> {
          phaser.register();
          _for(
              count,
              i -> {
                System.out.printf("Producer tries to add element %s%n", i);
                storage.add(elementFactory.apply(i));
                System.out.printf("Producer successfully added element %s%n", i);
                if (i == 3) {
                  suppress(() -> Thread.sleep(3_000));
                } else {
                  suppress(() -> Thread.sleep(1_000));
                }
              });

          System.out.println("Producer finished");
          phaser.arrive();
        });
  }
}
