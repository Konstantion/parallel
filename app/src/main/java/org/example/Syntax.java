package org.example;

import java.util.function.IntConsumer;

public final class Syntax {
  private Syntax() {}

  public static void _for(int to, IntConsumer asd) {
    for (int i = 0; i < to; i++) {
      asd.accept(i);
    }
  }
}
