package org.example.lab4;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public final class Main2 {

  private static final int NUM_PHILOSOPHERS = 5;

  private static final Semaphore[] forks = new Semaphore[NUM_PHILOSOPHERS];

  static {
    for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
      forks[i] = new Semaphore(1);
    }
  }

  private static final Semaphore waiter1 = new Semaphore(2);
  private static final Semaphore waiter2 = new Semaphore(2);

  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(NUM_PHILOSOPHERS);

    for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
      executorService.execute(new Philosopher(i));
    }

    executorService.shutdown();
  }

  public static class Philosopher implements Runnable {
    private final int id;

    public Philosopher(int id) {
      this.id = id;
    }

    @Override
    public void run() {
      try {
        while (true) {
          think();
          pickUpForks();
          eat();
          putDownForks();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }

    private void think() throws InterruptedException {
      System.out.println("Philosopher " + id + " is thinking.");
      Thread.sleep((long) (Math.random() * 1000));
    }

    private void pickUpForks() throws InterruptedException {
      waiter1.acquire();
      waiter2.acquire();

      forks[id].acquire();
      System.out.println("Philosopher " + id + " picked up left fork.");

      forks[(id + 1) % NUM_PHILOSOPHERS].acquire();
      System.out.println("Philosopher " + id + " picked up right fork.");
    }

    private void eat() throws InterruptedException {
      System.out.println("Philosopher " + id + " is eating.");
      Thread.sleep((long) (Math.random() * 1000));
    }

    private void putDownForks() {
      forks[(id + 1) % NUM_PHILOSOPHERS].release();
      System.out.println("Philosopher " + id + " put down right fork.");

      forks[id].release();
      System.out.println("Philosopher " + id + " put down left fork.");

      waiter1.release();
      waiter2.release();
    }
  }
}
