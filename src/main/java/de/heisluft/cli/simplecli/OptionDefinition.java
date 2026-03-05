package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An OptionDefinition represents a CLI option, consisting of its name, shorthand and its callback
 * to be run if set. It also is able to auto-convert its cli string value to a specified type.
 *
 * @param <E> the type of this options value. {@link Void} for options that do not take values.
 *
 * @since 0.1.0
 */
//TODO: document further
public final class OptionDefinition<E> {

  /** The name of the option */
  public final @NotNull String name;
  /** The shorthand of the option */
  public final char shorthand;
  /** If the Option takes a value, defined by the callback type supplied within the constructor */
  public final boolean takesValue;
  /** For valued options this callback is called if the option is set. The String argument contains the value */
  final @Nullable Consumer<Object> valueCallback;
  /** This callback is called when the option is set. */
  final @Nullable Runnable onDefinedCallBack;
  final @NotNull OptionDescription description;
  final @Nullable Function<String, E> valueConverter;

  public static @NotNull ValueOptionBuilder<String> valued(@NotNull String name) {
    return new ValueOptionBuilder<>(name, String.class);
  }

  public static @NotNull ValueOptionBuilder<String> valued(@NotNull String name,
      char shorthand) {
    return new ValueOptionBuilder<>(name, String.class).shorthand(shorthand);
  }

  public static @NotNull OptionDefinition<String> valued(@NotNull String name,
      @Nullable Consumer<String> valueCallback) {
    return new ValueOptionBuilder<>(name, String.class).callback(valueCallback).build();
  }

  public static @NotNull OptionDefinition<String> valued(@NotNull String name, char shorthand,
      @NotNull Consumer<String> valueCallback) {
    return new ValueOptionBuilder<>(name, String.class).shorthand(shorthand)
        .callback(valueCallback).build();
  }

  public static <T> @NotNull ValueOptionBuilder<T> valued(@NotNull String name, @NotNull Class<T> type) {
    return new ValueOptionBuilder<>(name, type);
  }

  public static <T> @NotNull ValueOptionBuilder<T> valued(@NotNull String name, char shorthand,
      @NotNull Class<T> type) {
    return new ValueOptionBuilder<>(name, type).shorthand(shorthand);
  }

  public static <T> @NotNull OptionDefinition<T> valued(@NotNull String name,
      @NotNull Class<T> type, @Nullable Consumer<T> valueCallback) {
    return new ValueOptionBuilder<>(name, type).callback(valueCallback).build();
  }

  public static <T> @NotNull OptionDefinition<T> valued(@NotNull String name, char shorthand,
      @NotNull Class<T> type, @Nullable Consumer<T> valueCallback) {
    return new ValueOptionBuilder<>(name, type).shorthand(shorthand).callback(valueCallback)
        .build();
  }

  public static @NotNull FlagOptionBuilder flag(@NotNull String name) {
    return new FlagOptionBuilder(name);
  }

  public static @NotNull FlagOptionBuilder flag(@NotNull String name, char shorthand) {
    return new FlagOptionBuilder(name).shorthand(shorthand);
  }

  /**
   * Defines an Option that does not take a value. The shorthand will be set to the first character
   * of the long name.
   *
   * @param name
   *     the options name
   * @param onSetCallback
   *     the callback to be run if the option is set
   */
  public static @NotNull OptionDefinition<Void> flag(@NotNull String name,
      @Nullable Runnable onSetCallback) {
    return new FlagOptionBuilder(name).whenSet(onSetCallback).build();
  }

  /**
   * Defines an Option that does not take a value
   *
   * @param name
   *     the options name
   * @param shorthand
   *     the options shorthand
   * @param onSetCallback
   *     the callback to be run if the option is set
   */
  public static @NotNull OptionDefinition<Void> flag(@NotNull String name, char shorthand,
      @Nullable Runnable onSetCallback) {
    return new FlagOptionBuilder(name).shorthand(shorthand).whenSet(onSetCallback).build();
  }

  OptionDefinition(@NotNull String name, char shorthand, @Nullable Runnable callback,
      @NotNull OptionDescription description) {
    this.name = name;
    this.shorthand = shorthand;
    this.takesValue = false;
    this.description = description;
    this.onDefinedCallBack = callback;
    this.valueCallback = null;
    this.valueConverter = null;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  OptionDefinition(@NotNull String name, char shorthand, @Nullable Consumer valueCallback,
      @Nullable Runnable onDefinedCallBack, @NotNull Function<String, E> valueConverter,
      @NotNull OptionDescription description) {
    this.name = name;
    this.shorthand = shorthand;
    this.takesValue = true;
    this.onDefinedCallBack = onDefinedCallBack;
    this.valueCallback = valueCallback;
    this.valueConverter = valueConverter;
    this.description = description;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if(o == null || getClass() != o.getClass()) return false;
    OptionDefinition<?> that = (OptionDefinition<?>) o;
    return shorthand == that.shorthand &&
        takesValue == that.takesValue &&
        Objects.equals(name, that.name) &&
        Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, shorthand, takesValue, description);
  }
}
