package org.example;

import java.util.function.IntConsumer;

public final class Syntax {
  private Syntax() {}

  public static void _for(int to, IntConsumer block) {
    for (int i = 0; i < to; i++) {
      block.accept(i);
    }
  }

  public static void _for(int from, int to, IntConsumer block) {
    for (int i = from; i < to; i++) {
      block.accept(i);
    }
  }
}
