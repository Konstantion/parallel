package org.example.lab1;

class Counter implements Runnable {
  private final String name;
  private volatile boolean stopped = false;
  private long count = 0L;

  public Counter(String name) {
    this.name = name;
  }

  @Override
  public void run() {
    System.out.printf("Counter %s started%n", name);

    while (!stopped) {
      count += 1;
    }

    System.out.printf("Counter %s counted to %s%n", name, count);
  }

  public void stop() {
    stopped = true;
  }

  public long count() {
    return count;
  }
}
