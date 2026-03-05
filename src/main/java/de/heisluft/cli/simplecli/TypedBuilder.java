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

interface TypedBuilder {

  /** The unmodifiable map of all default converters */
  static @NotNull Map<Class<?>, Function<String, ?>> DEFAULT_CONVERTERS = getDefaultConverters();

  static Map<Class<?>, Function<String, ?>> getDefaultConverters() {
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
    return Collections.unmodifiableMap(converters);
  }

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