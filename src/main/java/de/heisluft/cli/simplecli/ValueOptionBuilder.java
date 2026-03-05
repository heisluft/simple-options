package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public final class ValueOptionBuilder<E> extends OptionBuilder<E, ValueOptionBuilder<E>> implements TypedBuilder {
  private @Nullable Function<String, E> valueConverter;
  private @Nullable Consumer<E> valueCallback;

  public ValueOptionBuilder(@NotNull String name, @NotNull Class<E> type) {
    super(name);
    this.valueConverter = TypedBuilder.findConverter(type);
  }
  @SuppressWarnings("unchecked")
  public @NotNull <T> ValueOptionBuilder<T> mapValue(@NotNull Function<E, T> converter) {
    if(this.valueCallback != null) throw new IllegalStateException("Value callback already set");
    if(this.valueConverter == null) throw new IllegalStateException("Value converter not set");
    ValueOptionBuilder<T> self = (ValueOptionBuilder<T>) this;
    self.valueConverter = valueConverter.andThen(converter);
    return self;
  }

  public @NotNull ValueOptionBuilder<E> valueConverter(@Nullable Function<String, E> converter) {
    if(converter == null) throw new NullPointerException("converter cannot be null");
    this.valueConverter = converter;
    return this;
  }

  public @NotNull ValueOptionBuilder<E> callback(@Nullable Consumer<E> callback) {
    this.valueCallback = callback;
    return this;
  }

  @Override
  public @NotNull OptionDefinition<E> build() {
    if(valueConverter == null) throw new NullPointerException("value converter cannot be null");
    return new OptionDefinition<>(name, shorthand != 0 ? shorthand : name.charAt(0), valueCallback, callback, valueConverter, description);
  }

  public @NotNull ValueOptionBuilder<E> description(@Nullable String description, @Nullable String valHelpName) {
    if(description == null) throw new IllegalArgumentException("Option description cannot be null");
    if(valHelpName == null) throw new IllegalArgumentException("Option value help name cannot be null");
    this.description = new OptionDescription(description, valHelpName);
    return this;
  }
}
