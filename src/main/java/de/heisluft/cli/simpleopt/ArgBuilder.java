package de.heisluft.cli.simpleopt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ArgBuilder<E> {
  final @NotNull String name;
  private @Nullable Function<String, E> valueConverter;
  private @Nullable Consumer valueCallback;
  private @Nullable String description;

  public ArgBuilder(@NotNull String name, @NotNull Class<E> type) {
    if(name.isEmpty()) throw new IllegalArgumentException("Option name cannot be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Option name cannot contain spaces");
    this.name = name;
    this.valueConverter = TypedBuilder.findConverter(type);
  }

  public @NotNull ArgBuilder<E> description(@NotNull String description) {
    this.description = description;
    return this;
  }

  @SuppressWarnings("unchecked")
  public @NotNull <T> ArgBuilder<T> mapValue(@NotNull Function<E, T> converter) {
    if(this.valueCallback != null) throw new IllegalStateException("Value callback already set");
    if(this.valueConverter == null) throw new IllegalStateException("Value converter not set");
    ArgBuilder<T> self = (ArgBuilder<T>) this;
    self.valueConverter = valueConverter.andThen(converter);
    return self;
  }

  public @NotNull ArgBuilder<E> valueConverter(@Nullable Function<String, E> converter) {
    if(converter == null) throw new NullPointerException("converter cannot be null");
    this.valueConverter = converter;
    return this;
  }

  public @NotNull ArgBuilder<E> callback(@Nullable Consumer<E> callback) {
    this.valueCallback = callback;
    return this;
  }

  public @NotNull ArgDefinition<E> build() {
    return new ArgDefinition<>(name, valueCallback, description == null ? "" : description, valueConverter);
  }
}
