package org.example;

public final class Throw {
  private Throw() {}

  public static <T, E extends Throwable> T suppress(CheckedBlock<T, E> block) {
    try {
      return block.run();
    } catch (Throwable e) {
      return sneakyThrow(e);
    }
  }

  public static <E extends Throwable> void suppress(CheckedRunnable<E> runnable) {
    suppress(runnable.block());
  }

  @SuppressWarnings("unchecked")
  public static <T, E extends Throwable> T sneakyThrow(Throwable t) throws E {
    throw (E) t;
  }

  public static interface CheckedBlock<T, E extends Throwable> {
    T run() throws E;
  }

  public static interface CheckedRunnable<E extends Throwable> {
    void run() throws E;

    default CheckedBlock<Unit, E> block() {
      return () -> {
        this.run();
        return Unit.value();
      };
    }
  }
}
