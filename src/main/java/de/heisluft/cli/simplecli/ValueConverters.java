package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility Class for finding default converters of option and argument values.
 *
 * @since 0.4.0
 */
final class ValueConverters {

  /** The unmodifiable map of all default converters. */
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

  /** Do not instantiate. */
  private ValueConverters() {}

  /**
   * Find the default value converter for the given type, represented by its class.
   * If none is found, this function may return {@code null}.
   *
   * @param type the class of the type to map to. Must not be {@code null}.
   * @param <T> the destination type to map to.
   * @return a matching value converter or {@code null} if none is found.
   */
  @SuppressWarnings("unchecked")
  static <T> @Nullable Function<String, T> findConverter(@NotNull Class<T> type) {
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
}
