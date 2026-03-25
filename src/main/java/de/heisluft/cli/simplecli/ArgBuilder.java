package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This Builder class builds an ArgDefinition. All methods mutate the builder,
 * using the builder, especially after invoking {@link #mapValue(Function)}, WILL produce undefined
 * behaviour. Instances can be obtained by invoking the "arg" methods within {@link ArgDefinition}.
 *
 * @param <E> the type of the argument value
 *
 * @since 0.4.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class ArgBuilder<E> {
  /** The name of the argument used in help formatting. */
  final @NotNull String name;
  /** This function is invoked with the raw string CLI argument. It must not return null. */
  private @Nullable Function<String, E> valueConverter;
  /** This callback is called with the converted value after successful CLI parsing. MUST be raw. */
  private @Nullable Consumer valueCallback;
  /** The arguments description, used in help formatting. */
  private @Nullable String description;
  /** A function used to validate the parsed value. */
  private @Nullable Validator<E> validator;

  /**
   * This internal constructor sets the final name as well as an initial type.
   * For the given type, a default converter function is used if found.
   *
   * @param name the final, not-null name of the resulting argument
   * @param type the initial type the value will be converted to.
   */
  ArgBuilder(@NotNull String name, @NotNull Class<E> type) {
    if(name.isEmpty()) throw new IllegalArgumentException("Option name cannot be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Option name cannot contain spaces");
    this.name = name;
    this.valueConverter = ValueConverters.findConverter(type);
  }

  /**
   * Set the description of this argument to be used in help formatting.
   *
   * @param description the non-null description
   *
   * @return this
   */
  public @NotNull ArgBuilder<E> description(@NotNull String description) {
    this.description = description;
    return this;
  }

  /**
   * Map the converted value of this argument to another, converting this to an ArgBuilder&lt;T&gt;.
   * This method will fail if a validator or a callback have been set previously.
   *
   * @param converter the converting function. Takes the converted argument.
   * Must not produce {@code null}
   * @param <T> the type of the conversion result.
   *
   * @return this
   */
  @SuppressWarnings("unchecked")
  public @NotNull <T> ArgBuilder<T> mapValue(@NotNull Function<E, T> converter) {
    if(this.valueCallback != null) throw new IllegalStateException("Value callback already set");
    if(this.validator != null) throw new IllegalStateException("Validator already set");
    if(this.valueConverter == null) throw new IllegalStateException("Value converter not set");
    ArgBuilder<T> self = (ArgBuilder<T>) this;
    self.valueConverter = valueConverter.andThen(converter);
    return self;
  }

  /**
   * Set a custom function to be invoked when the string value of this argument is to be converted
   * to the target value.
   *
   * @param converter the converting function. Takes the string argument. Must not produce {@code null}.
   * @return this
   */
  public @NotNull ArgBuilder<E> valueConverter(@Nullable Function<String, E> converter) {
    if(converter == null) throw new NullPointerException("converter cannot be null");
    this.valueConverter = converter;
    return this;
  }

  /**
   * Set a callback to be invoked when the value of this argument is parsed and converted.
   * If null, no callback shall be performed.
   *
   * @param callback the receiving code, mmy be {@code null}.
   * @return this
   */
  public @NotNull ArgBuilder<E> callback(@Nullable Consumer<E> callback) {
    this.valueCallback = callback;
    return this;
  }

  /**
   * Change the validation function for this arguments value after conversion.
   * If null, no validation shall be performed.
   *
   * @param validator the validation function, may be {@code null}.
   * @return this
   */
  public @NotNull ArgBuilder<E> validatedBy(@Nullable Validator<E> validator) {
    this.validator = validator;
    return this;
  }

  /**
   * Build and return the resulting ArgDefinition, finalizing the parameters set previously.
   *
   * @return the built definition
   */
  public @NotNull ArgDefinition<E> build() {
    return new ArgDefinition<>(name, valueCallback, description == null ? "" : description, valueConverter, validator);
  }
}
