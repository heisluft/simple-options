package de.heisluft.cli.simpleopt.option;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  @NotNull
  public final String name;
  /** The shorthand of the option */
  public final char shorthand;
  /** If the Option takes a value, defined by the callback type supplied within the constructor */
  public final boolean takesValue;
  /** For valued options this callback is called if the option is set. The String argument contains the value */
  @Nullable
  public final Consumer<E> valueCallback;
  /** For non-valued options this callback is called if the option is set. */
  @Nullable
  public final Runnable onDefinedCallBack;
  @NotNull
  public final OptionDescription description;
  @Nullable
  public final Function<String, E> valueConverter;

  @NotNull
  @Contract(pure = true, value = "null,_ -> fail")
  public static OptionDefinition<String> withArg(@Nullable String name,
      @Nullable Consumer<String> valueCallback) {
    return new WithArgOptionBuilder<>(name, String.class).callback(valueCallback).get();
  }

  @NotNull
  @Contract(pure = true, value = "null,_,_ -> fail")
  public static OptionDefinition<String> withArg(@Nullable String name, char shorthand,
      @NotNull Consumer<String> valueCallback) {
    return new WithArgOptionBuilder<>(name, String.class).shorthand(shorthand)
        .callback(valueCallback).get();
  }

  @NotNull
  @Contract(pure = true, value = "null,_,_,_ -> fail")
  public static <T> OptionDefinition<T> withArg(@Nullable String name, char shorthand,
      @NotNull Class<T> type, @Nullable Consumer<T> valueCallback) {
    return new WithArgOptionBuilder<>(name, type).shorthand(shorthand).callback(valueCallback)
        .get();
  }

  @NotNull
  @Contract(pure = true, value = "null,_,_ -> fail")
  public static <T> OptionDefinition<T> withArg(@Nullable String name, @NotNull Class<T> type,
      @Nullable Consumer<T> valueCallback) {
    return new WithArgOptionBuilder<>(name, type).callback(valueCallback).get();
  }

  @NotNull
  @Contract(pure = true, value = "null,_ -> fail")
  public static <T> WithArgOptionBuilder<T> withArg(String name, @NotNull Class<T> type) {
    return new WithArgOptionBuilder<>(name, type);
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
  @NotNull
  @Contract(pure = true, value = "null,_,_ -> fail")
  public static OptionDefinition<Void> nonArg(@Nullable String name, char shorthand,
      @Nullable Runnable onSetCallback) {
    return new NonArgOptionBuilder(name).shorthand(shorthand).whenSet(onSetCallback).get();
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
  @NotNull
  @Contract(pure = true, value = "null,_ -> fail")
  public static OptionDefinition<Void> nonArg(@Nullable String name,
      @Nullable Runnable onSetCallback) {
    return new NonArgOptionBuilder(name).whenSet(onSetCallback).get();
  }

  @NotNull
  @Contract(pure = true, value = "null -> fail")
  public static NonArgOptionBuilder nonArg(@Nullable String name) {
    return new NonArgOptionBuilder(name);
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

  OptionDefinition(@NotNull String name, char shorthand, @Nullable Consumer<E> valueCallback,
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
  public boolean equals(Object obj) {
    return obj instanceof OptionDefinition && name.equals(((OptionDefinition<?>) obj).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
