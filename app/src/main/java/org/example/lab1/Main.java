package org.example.lab1;

import java.time.Duration;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    ThreadFactory factory = factory(false);
    var counters =
        IntStream.range(0, 10).mapToObj(String::valueOf).map(name -> new Counter(name)).toList();

    Runnable notifier =
        () -> {
          try {
            System.err.println("notifier thread start waiting");
            Thread.sleep(Duration.ofSeconds(5));
            System.err.println("notifier thread stops counters");
            counters.forEach(Counter::stop);
          } catch (InterruptedException ignored) {
            System.err.println("notifier thread got interrupted exiting");
            System.exit(1);
          }
        };

    var threads = counters.stream().map(factory::newThread).toList();

    threads.forEach(Thread::start);
    factory.newThread(notifier).start();

    for (var thread : threads) {
      thread.join();
    }
  }

  private static ThreadFactory factory(boolean isVirtual) {
    return isVirtual ? Thread.ofVirtual()::unstarted : Thread::new;
  }
}
