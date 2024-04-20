package org.example;

public final class Unit {
  private static final Unit INSTANCE = new Unit();

  private Unit() {}

  public static Unit value() {
    return INSTANCE;
  }
}
