package de.heisluft.cli.simpleopt.option;

import org.jetbrains.annotations.Contract;
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

public final class WithArgOptionBuilder<E> extends OptionBuilder<E, WithArgOptionBuilder<E>> {

  /** The unmodifiable map of all default converters */
  @NotNull
  private static final Map<Class<?>, Function<String, ?>> DEFAULT_CONVERTERS;

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

  @Nullable
  private Function<String, E> valueConverter;
  @Nullable
  private Consumer<E> valueCallback;

  @Contract(pure = true, value = "null, _ -> fail")
  public WithArgOptionBuilder(@Nullable String name, @NotNull Class<E> type) {
    super(name);
    this.valueConverter = findConverter(type);
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Contract(pure = true)
  private static <T> Function<String, T> findConverter(@NotNull Class<T> type) {
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

  @Contract(value = "null -> fail")
  @NotNull
  public WithArgOptionBuilder<E> valueConverter(@Nullable Function<String, E> converter) {
    if(converter == null) throw new NullPointerException("converter cannot be null");
    this.valueConverter = converter;
    return this;
  }

  @NotNull
  public WithArgOptionBuilder<E> callback(@Nullable Consumer<E> callback) {
    this.valueCallback = callback;
    return this;
  }

  @Override
  @NotNull
  @Contract(pure = true, value = "-> new")
  public OptionDefinition<E> get() {
    return new OptionDefinition<>(name, shorthand != 0 ? shorthand : name.charAt(0), valueCallback, callback, valueConverter, description);
  }
}
