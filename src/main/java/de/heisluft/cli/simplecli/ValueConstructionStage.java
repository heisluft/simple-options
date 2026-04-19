package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

final class ValueConstructionStage<E> {
  @Nullable Function<?, E> converter;
  @Nullable Validator<E> validator;
  @Nullable Consumer<E> callback;

  ValueConstructionStage(@Nullable Function<?, E> converter) {
    this.converter = converter;
  }
}
