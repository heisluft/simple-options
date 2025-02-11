package de.heisluft.cli.simpleopt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ArgOptionBuilder<E> extends OptionBuilder<E, ArgOptionBuilder<E>> {

  /** The unmodifiable map of all default converters */
  private static final @NotNull Map<Class<?>, Function<String, ?>> DEFAULT_CONVERTERS;

  static {
    Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
    converters.put(Boolean.class, Boolean::parseBoolean);
    converters.put(Byte.class, Byte::parseByte);
    converters.put(Integer.class, Integer::parseInt);
    converters.put(Long.class, Long::parseLong);
    converters.put(Float.class, Float::parseFloat);
    converters.put(Double.class, Double::parseDouble);
    converters.put(File.class, File::new);
    converters.put(Path.class, Paths::get);
    converters.put(String.class, Function.identity());
    DEFAULT_CONVERTERS = Collections.unmodifiableMap(converters);
  }

  private @Nullable Function<String, E> valueConverter;
  private @Nullable Consumer<E> valueCallback;

  public ArgOptionBuilder(@Nullable String name, @NotNull Class<E> type) {
    super(name);
    this.valueConverter = findConverter(type);
  }

  @SuppressWarnings("unchecked")
  private static <T> @Nullable Function<String, T> findConverter(@NotNull Class<T> type) {
    if(DEFAULT_CONVERTERS.containsKey(type)) return (Function<String, T>) DEFAULT_CONVERTERS.get(type);
    if(Enum.class.isAssignableFrom(type)) {
      return t -> {
        for(T enumConstant : type.getEnumConstants()) {
          if(enumConstant.toString().equals(t.toUpperCase(Locale.ROOT))) return enumConstant;
        }
        return null;
      };
    }
    return null;
  }

  public @NotNull ArgOptionBuilder<E> valueConverter(@Nullable Function<String, E> converter) {
    if(converter == null) throw new NullPointerException("converter cannot be null");
    this.valueConverter = converter;
    return this;
  }

  public @NotNull ArgOptionBuilder<E> callback(@Nullable Consumer<E> callback) {
    this.valueCallback = callback;
    return this;
  }

  @Override
  public @NotNull OptionDefinition<E> build() {
    if(valueConverter == null) throw new NullPointerException("value converter cannot be null");
    return new OptionDefinition<>(name, shorthand != 0 ? shorthand : name.charAt(0), valueCallback, callback, valueConverter, description, validator);
  }

  public @NotNull ArgOptionBuilder<E> description(@Nullable String description, @Nullable String valHelpName) {
    if(description == null) throw new IllegalArgumentException("Option description cannot be null");
    if(valHelpName == null) throw new IllegalArgumentException("Option value help name cannot be null");
    this.description = new OptionDescription(description, valHelpName);
    return this;
  }
}
