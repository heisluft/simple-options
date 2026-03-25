package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An OptionBuilder capable of building OptionDefinitions taking an argument.
 * NOTE: OptionBuilders are stateful. Mapping the value after a validator or a callback have been set
 * will thrown an exception.
 *
 * @param <E> the type of the options value once built.
 *
 * @since 0.2.0
 */
public final class ValueOptionBuilder<E> extends OptionBuilder<E, ValueOptionBuilder<E>> {
  /** A function invoked for parsing the option argument into its value. */
  private @Nullable Function<String, E> valueConverter;
  /** A consumer invoked on the final, validated value. */
  private @Nullable Consumer<E> valueCallback;
  /** A function used to validate the parsed value. */
  private @Nullable Validator<E> validator;

  /**
   * Construct a builder instance.
   *
   * @param name the options long name. Must not be {@code null} or empty, nor contain spaces.
   * @param type the type of this option. Used to find a converter. Must not be {@code null}.
   */
  ValueOptionBuilder(@NotNull String name, @NotNull Class<E> type) {
    super(name);
    this.valueConverter = ValueConverters.findConverter(type);
  }

  /**
   * Map the converted value of this argument to another, converting this to an ValueOptionBuilder&lt;T&gt;.
   * This method will fail if a validator or a callback have been set previously.
   *
   * @param converter the converting function. Takes the converted argument.
   * Must not produce {@code null}
   * @param <T> the type of the conversion result.
   *
   * @return this
   */
  @SuppressWarnings("unchecked")
  public @NotNull <T> ValueOptionBuilder<T> mapValue(@NotNull Function<E, T> converter) {
    if(this.valueCallback != null) throw new IllegalStateException("Value callback already set");
    if(this.validator != null) throw new IllegalStateException("Value validator already set");
    if(this.valueConverter == null) throw new IllegalStateException("Value converter not set");
    ValueOptionBuilder<T> self = (ValueOptionBuilder<T>) this;
    self.valueConverter = valueConverter.andThen(converter);
    return self;
  }

  /**
   * Set a custom function to be invoked when the string value of this option is to be converted
   * to the target value.
   *
   * @param converter the converting function. Takes the string argument. Must not produce {@code null}.
   * @return this
   */
  public @NotNull ValueOptionBuilder<E> valueConverter(@Nullable Function<String, E> converter) {
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
  public @NotNull ValueOptionBuilder<E> callback(@Nullable Consumer<E> callback) {
    this.valueCallback = callback;
    return this;
  }

  /**
   * Change the validation function for this options value after conversion.
   * If null, no validation shall be performed.
   *
   * @param validator the validation function, may be {@code null}.
   * @return this
   * @since 0.4.0
   */
  public @NotNull ValueOptionBuilder<E> validatedBy(@Nullable Validator<E> validator) {
    this.validator = validator;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull OptionDefinition<E> build() {
    if(valueConverter == null) throw new NullPointerException("value converter cannot be null");
    return new OptionDefinition<>(
        name,
        shorthand != 0 ? shorthand : name.charAt(0),
        valueCallback,
        callback,
        valueConverter,
        description,
        validator
    );
  }

  /**
   * Set the description of this argument to be used in help formatting.
   *
   * @param description the non-null description
   * @param valHelpName the non-null name of the value
   * @return this
   */
  public @NotNull ValueOptionBuilder<E> description(@Nullable String description, @Nullable String valHelpName) {
    if(description == null) throw new IllegalArgumentException("Option description cannot be null");
    if(valHelpName == null) throw new IllegalArgumentException("Option value help name cannot be null");
    this.description = new OptionDescription(description, valHelpName);
    return this;
  }
}
