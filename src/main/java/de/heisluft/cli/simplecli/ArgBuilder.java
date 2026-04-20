package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Objects;
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
@SuppressWarnings("unchecked")
public final class ArgBuilder<E> {
  /** The name of the argument used in help formatting. */
  final @NotNull String name;
  /** This function is invoked with the raw string CLI argument. It must not return null. */
  private final @NotNull LinkedList<ValueConstructionStage<?>> valueConstructionStages = new LinkedList<>();
  /** The arguments description, used in help formatting. */
  private @Nullable String description;

  /**
   * This internal constructor sets the final name as well as an initial type.
   * For the given type, a default converter function is used if found.
   *
   * @param name the final, not-null name of the resulting argument
   * @param type the initial type the value will be converted to.
   */
  ArgBuilder(@NotNull String name, @NotNull Class<E> type) {
    Objects.requireNonNull(name, "Argument name must not be null");
    Objects.requireNonNull(type, "Type must not be null");
    if(name.isEmpty()) throw new IllegalArgumentException("Argument name must not be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Argument name must not contain spaces");
    this.name = name;
    valueConstructionStages.add(new ValueConstructionStage<>(ValueConverters.findConverter(type)));
  }

  /**
   * Set the description of this argument to be used in help formatting.
   *
   * @param description the non-null description
   *
   * @return this
   */
  public @NotNull ArgBuilder<E> description(@NotNull String description) {
    Objects.requireNonNull(description, "Description must not be null");
    this.description = description;
    return this;
  }

  /**
   * Map the converted value of this argument to another, converting this to an ArgBuilder&lt;T&gt;.
   *
   * @param converter the converting function. Takes the converted argument.
   * Must not produce {@code null}
   * @param <T> the type of the conversion result.
   *
   * @return this
   */
  @SuppressWarnings("unchecked")
  public @NotNull <T> ArgBuilder<T> mapValue(@NotNull Function<@NotNull E, @NotNull T> converter) {
    Objects.requireNonNull(converter, "Converter must not be null");
    if(valueConstructionStages.getLast().converter == null)
      throw new IllegalStateException("No way to obtain value at previous stage");
    valueConstructionStages.add(new ValueConstructionStage<>(converter));
    return (ArgBuilder<T>) this;
  }

  /**
   * Set a custom function to be invoked when the string value of this argument is to be converted
   * to the target value.
   *
   * @param converter the converting function. Takes the string argument. Must not produce {@code null}.
   * @return this
   */
  @SuppressWarnings("unchecked")
  public @NotNull ArgBuilder<E> valueConverter(@NotNull Function<@NotNull String, @NotNull E> converter) {
    Objects.requireNonNull(converter, "Converter must not be null");
    if(valueConstructionStages.size() > 1)
      throw new IllegalStateException("Initial converter must be set before any map call");
    ((ValueConstructionStage<E>) valueConstructionStages.getLast()).converter = converter;
    return this;
  }

  /**
   * Change the validation function for this arguments value after conversion.
   * If null, no validation shall be performed.
   *
   * @param validator the validation function, may be {@code null}.
   * @return this
   */
  public @NotNull ArgBuilder<E> validatedBy(@Nullable Validator<@NotNull E> validator) {
    ((ValueConstructionStage<E>) valueConstructionStages.getLast()).validator = validator;
    return this;
  }

  /**
   * Build and return the resulting ArgDefinition, finalizing the parameters set previously.
   *
   * @return the built definition
   */
  public @NotNull ArgDefinition<E> build() {
    return build(null);
  }

  public @NotNull ArgDefinition<E> build(@Nullable Consumer<@NotNull E> consumer) {
    return new ArgDefinition<>(name, description == null ? "" : description, valueConstructionStages, consumer);
  }
}
