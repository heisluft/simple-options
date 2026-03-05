package de.heisluft.cli.simpleopt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ArgDefinition<E> {
  /** The name of the option */
  public final @NotNull String name;
  final @NotNull String description;
  /** For valued options this callback is called if the option is set. The String argument contains the value */
  final Consumer<Object> valueCallback;
  final @Nullable Function<String, E> valueConverter;

  public static @NotNull ArgBuilder<String> arg(@NotNull String name) {
    return new ArgBuilder<>(name, String.class);
  }
  public static @NotNull ArgDefinition<String> of(@NotNull String name,
      @Nullable Consumer<String> valueCallback) {
    return new ArgBuilder<>(name, String.class).callback(valueCallback).build();
  }

  public static <T> @NotNull ArgBuilder<T> arg(@NotNull String name, @NotNull Class<T> type) {
    return new ArgBuilder<>(name, type);
  }

  public static <T> @NotNull ArgDefinition<T> of(@NotNull String name,
      @NotNull Class<T> type, @Nullable Consumer<T> valueCallback) {
    return new ArgBuilder<>(name, type).callback(valueCallback).build();
  }

  public ArgDefinition(@NotNull String name, Consumer<Object> valueCallback, @NotNull String description,
      @Nullable Function<String, E> valueConverter) {
    this.name = name;
    this.valueCallback = valueCallback;
    this.description = description;
    this.valueConverter = valueConverter;
  }

  @Override
  public boolean equals(Object o) {
    if(!(o instanceof ArgDefinition)) return false;
    ArgDefinition<?> requiredArg = (ArgDefinition<?>) o;
    return name.equals(requiredArg.name) && description.equals(requiredArg.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description);
  }
}
