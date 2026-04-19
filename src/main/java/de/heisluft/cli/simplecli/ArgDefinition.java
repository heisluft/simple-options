package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An ArgumentDefinition represents a CLI argument, consisting of its name, shorthand and its callback
 * to be run if set. It also is able to auto-convert its cli string value to a specified type.
 *
 * @param <E> the type of this options value.
 *
 * @since 0.4.0
 */
//TODO: document further
public final class ArgDefinition<E> {
  /** The name of the option. */
  public final @NotNull String name;
  /** The arguments description. Used in help formatting. */
  final @NotNull String description;
  final @NotNull List<@NotNull ValueConstructionStage<?>> valueConstructionStages;

  ArgDefinition(
      @NotNull String name,
      @NotNull String description,
      @NotNull List<@NotNull ValueConstructionStage<?>> valueConstructionStages) {
    this.name = name;
    this.description = description;
    this.valueConstructionStages = valueConstructionStages;
  }

  public static <T> @NotNull ArgDefinition<T> of(
      @NotNull String name,
      @NotNull Class<T> type,
      @Nullable Consumer<T> valueCallback
  ) {
    return new ArgBuilder<>(name, type).callback(valueCallback).build();
  }

  public static @NotNull ArgDefinition<String> of(
      @NotNull String name,
      @Nullable Consumer<String> valueCallback
  ) {
    return new ArgBuilder<>(name, String.class).callback(valueCallback).build();
  }

  public static @NotNull ArgBuilder<String> arg(@NotNull String name) {
    return new ArgBuilder<>(name, String.class);
  }

  public static <T> @NotNull ArgBuilder<T> arg(@NotNull String name, @NotNull Class<T> type) {
    return new ArgBuilder<>(name, type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if(!(o instanceof ArgDefinition)) return false;
    ArgDefinition<?> requiredArg = (ArgDefinition<?>) o;
    return name.equals(requiredArg.name) && description.equals(requiredArg.description);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(name, description);
  }
}
