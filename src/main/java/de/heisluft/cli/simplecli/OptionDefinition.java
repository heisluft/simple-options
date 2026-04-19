package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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

  /** The name of the option. */
  public final @NotNull String name;
  /** The shorthand of the option. */
  public final char shorthand;
  /** If the Option takes a value, defined by the callback type supplied within the constructor. */
  public final boolean takesValue;
  /** This callback is called when the option is set. */
  final @Nullable Runnable onDefinedCallBack;
  /** The options description. Used in help formatting. */
  final @NotNull OptionDescription description;
  final @NotNull List<ValueConstructionStage<?>> valueConstructionStages;

  OptionDefinition(@NotNull String name, char shorthand, @Nullable Runnable callback,
      @NotNull OptionDescription description) {
    this.name = name;
    this.shorthand = shorthand;
    this.takesValue = false;
    this.description = description;
    this.onDefinedCallBack = callback;
    this.valueConstructionStages = new ArrayList<>();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  OptionDefinition(
      @NotNull String name,
      char shorthand,
      @Nullable Runnable onDefinedCallBack,
      @NotNull OptionDescription description,
      @NotNull List<ValueConstructionStage<?>> valueConstructionStages
  ) {
    this.name = name;
    this.shorthand = shorthand;
    this.takesValue = true;
    this.onDefinedCallBack = onDefinedCallBack;
    this.description = description;
    this.valueConstructionStages = valueConstructionStages;
  }

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
   *
   *  @return the resulting OptionDefinition
   */
  public static @NotNull OptionDefinition<Void> flag(@NotNull String name,
      @Nullable Runnable onSetCallback) {
    return new FlagOptionBuilder(name).whenSet(onSetCallback).build();
  }

  /**
   * Defines an Option that does not take a value.
   *
   * @param name
   *     the options name
   * @param shorthand
   *     the options shorthand
   * @param onSetCallback
   *     the callback to be run if the option is set
   *
   * @return the resulting OptionDefinition
   */
  public static @NotNull OptionDefinition<Void> flag(@NotNull String name, char shorthand,
      @Nullable Runnable onSetCallback) {
    return new FlagOptionBuilder(name).shorthand(shorthand).whenSet(onSetCallback).build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if(o == null || getClass() != o.getClass()) return false;
    OptionDefinition<?> that = (OptionDefinition<?>) o;
    return shorthand == that.shorthand
        && takesValue == that.takesValue
        && name.equals(that.name)
        && description.equals(that.description);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(name, shorthand, takesValue, description);
  }
}
