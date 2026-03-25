package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

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
  /** For valued options this callback is called if the option is set. The argument contains the value. */
  final Consumer<Object> valueCallback;
  /** A function invoked for parsing the arguments string value into its destination type. */
  final @Nullable Function<String, E> valueConverter;
  /** A function used to validate the parsed value. */
  final @NotNull Validator<E> validator;

  ArgDefinition(
      @NotNull String name,
      Consumer<Object> valueCallback,
      @NotNull String description,
      @Nullable Function<String, E> valueConverter,
      @Nullable Validator<E> validator) {
    this.name = name;
    this.valueCallback = valueCallback;
    this.description = description;
    this.valueConverter = valueConverter;
    this.validator = validator != null ? validator : v -> ValidationResult.valid();
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
