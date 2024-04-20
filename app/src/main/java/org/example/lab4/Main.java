package org.example.lab4;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public final class Main {
  private static final int NUM_PHILOSOPHERS = 5;

  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(NUM_PHILOSOPHERS);

    Semaphore[] forks = new Semaphore[NUM_PHILOSOPHERS];
    for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
      forks[i] = new Semaphore(1);
    }

    for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
      executorService.execute(new Philosopher(i, forks));
    }

    executorService.shutdown();
  }

  public static class Philosopher implements Runnable {
    private final int id;
    private final Semaphore leftFork;
    private final Semaphore rightFork;

    public Philosopher(int id, Semaphore[] forks) {
      this.id = id;
      int leftForkIndex = id;
      int rightForkIndex = (id + 1) % NUM_PHILOSOPHERS;

      if (id == NUM_PHILOSOPHERS - 1) {
        this.leftFork = forks[rightForkIndex];
        this.rightFork = forks[leftForkIndex];
      } else {
        this.leftFork = forks[leftForkIndex];
        this.rightFork = forks[rightForkIndex];
      }
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
      }
    }

    private void think() throws InterruptedException {
      System.out.println("Philosopher " + id + " is thinking.");
      Thread.sleep((long) (Math.random() * 1000));
    }

    private void pickUpForks() throws InterruptedException {
      leftFork.acquire();
      System.out.println("Philosopher " + id + " picked up left fork.");

      rightFork.acquire();
      System.out.println("Philosopher " + id + " picked up right fork.");
    }

    private void eat() throws InterruptedException {
      System.out.println("Philosopher " + id + " is eating.");
      Thread.sleep((long) (Math.random() * 1000));
    }

    private void putDownForks() {
      rightFork.release();
      System.out.println("Philosopher " + id + " put down right fork.");

      leftFork.release();
      System.out.println("Philosopher " + id + " put down left fork.");
    }
  }
}
