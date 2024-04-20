package org.example.lab3;

import java.util.concurrent.Phaser;
import java.util.stream.Stream;

public final class Main {
  public static void main(String[] args) throws InterruptedException {
    final int count = 10;
    final String mask = "element %s";
    final var storage = new BlockingStorage<String>(4);
    final var phaser = new Phaser(1);

    final Thread consumer = BlockingStorage.consumer(count, storage, phaser);
    final Thread producer = BlockingStorage.producer(count, storage, mask::formatted, phaser);

    Stream.of(consumer, producer).forEach(Thread::start);

    phaser.arriveAndAwaitAdvance();
  }
}
