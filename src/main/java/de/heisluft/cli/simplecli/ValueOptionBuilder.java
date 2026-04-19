package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Objects;
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
  private final @NotNull LinkedList<@NotNull ValueConstructionStage<?>> valueConstructionStages = new LinkedList<>();

  /**
   * Construct a builder instance.
   *
   * @param name the options long name. Must not be {@code null} or empty, nor contain spaces.
   * @param type the type of this option. Used to find a converter. Must not be {@code null}.
   */
  ValueOptionBuilder(@NotNull String name, @NotNull Class<E> type) {
    super(name);
    Objects.requireNonNull(type, "Type must not be null");
    valueConstructionStages.add(new ValueConstructionStage<>(ValueConverters.findConverter(type)));
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
  public @NotNull <T> ValueOptionBuilder<T> mapValue(@NotNull Function<@NotNull E, @NotNull T> converter) {
    Objects.requireNonNull(converter, "Converter must not be null");
    if(valueConstructionStages.getLast().converter == null)
      throw new IllegalStateException("No way to obtain value at previous stage");
    valueConstructionStages.add(new ValueConstructionStage<>(converter));
    return (ValueOptionBuilder<T>) this;
  }

  /**
   * Set a custom function to be invoked when the string value of this option is to be converted
   * to the target value.
   *
   * @param converter the converting function. Takes the string argument. Must not produce {@code null}.
   * @return this
   */
  @SuppressWarnings("unchecked")
  public @NotNull ValueOptionBuilder<E> valueConverter(@NotNull Function<@NotNull String, @NotNull E> converter) {
    Objects.requireNonNull(converter, "Converter must not be null");
    if(valueConstructionStages.size() > 1)
      throw new IllegalStateException("Initial converter must be set before any map call");
    ((ValueConstructionStage<E>) valueConstructionStages.getLast()).converter = converter;
    return this;
  }

  /**
   * Set a callback to be invoked when the value of this argument is parsed and converted.
   * If null, no callback shall be performed.
   *
   * @param callback the receiving code, mmy be {@code null}.
   * @return this
   */
  @SuppressWarnings("unchecked")
  public @NotNull ValueOptionBuilder<E> callback(@Nullable Consumer<@NotNull E> callback) {
    ((ValueConstructionStage<E>) valueConstructionStages.getLast()).callback = callback;
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
  @SuppressWarnings("unchecked")
  public @NotNull ValueOptionBuilder<E> validatedBy(@Nullable Validator<@NotNull E> validator) {
    ((ValueConstructionStage<E>) valueConstructionStages.getLast()).validator = validator;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull OptionDefinition<E> build() {
    Objects.requireNonNull(
        valueConstructionStages.getFirst().converter,
        "Initial value converter must not be null"
    );
    return new OptionDefinition<>(
        name,
        shorthand != 0 ? shorthand : name.charAt(0),
        callback,
        description,
        valueConstructionStages
    );
  }

  /**
   * Set the description of this argument to be used in help formatting.
   *
   * @param description the non-null description
   * @param valHelpName the non-null name of the value
   * @return this
   */
  public @NotNull ValueOptionBuilder<E> description(@NotNull String description, @NotNull String valHelpName) {
    Objects.requireNonNull(description, "Option description must not be null");
    Objects.requireNonNull(valHelpName, "Option value help name must not be null");
    this.description = new OptionDescription(description, valHelpName);
    return this;
  }
}
