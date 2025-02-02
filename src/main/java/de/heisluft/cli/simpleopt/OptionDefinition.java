package de.heisluft.cli.simpleopt;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An OptionDefinition represents a CLI option, consisting of its name, shorthand and its callback
 * to be run if set. It also is able to auto-convert its cli string value to a specified type.
 *
 * @param <E> the type of this options value. {@link Void} for options that do not take values.
 *
 * @since 0.1.0
 */
//TODO: document further
public final class OptionDefinition<E> {

  /** The unmodifiable map of all default converters */
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

  /** The name of the option */
  public final String name;
  /** The shorthand of the option */
  public final char shorthand;
  /** If the Option takes a value, defined by the callback type supplied within the constructor */
  public final boolean takesValue;
  /** For valued options this callback is called if the option is set. The String argument contains the value */
  final Consumer<E> valueCallback;
  /** For non-valued options this callback is called if the option is set. */
  final Runnable onDefinedCallBack;
  private String description = "";
  private String valueHelpName = "VALUE";
  private Function<String, E> valueConverter;

  Function<String, E> valueConverter() {
    return valueConverter;
  }

  public static OptionDefinition<String> withArg(String name, Consumer<String> valueCallback) {
    return withArg(name, String.class, valueCallback);
  }

  public static OptionDefinition<String> withArg(String name, char shorthand, Consumer<String> valueCallback) {
    return withArg(name, shorthand, String.class, valueCallback);
  }

  public static <T> OptionDefinition<T> withArg(String name, char shorthand, Class<T> type, Consumer<T> valueCallback) {
    if(name == null || name.isEmpty()) throw new IllegalArgumentException("Option name cannot be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Option name cannot contain spaces");
    if(shorthand == ' ') throw new IllegalArgumentException("Option shorthand cannot be a space");
    return new OptionDefinition<>(name, shorthand, valueCallback, null, type);
  }

  public static <T> OptionDefinition<T> withArg(String name, Class<T> type, Consumer<T> valueCallback) {
    if(name == null || name.isEmpty()) throw new IllegalArgumentException("Option name cannot be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Option name cannot contain spaces");
    return new OptionDefinition<>(name, name.charAt(0), valueCallback, null, type);
  }

  @SuppressWarnings("unchecked")
  private static <T> Function<String, T> findConverter(Class<T> type) {
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

  private OptionDefinition(String name, char shorthand, Consumer<E> valueCallback, Runnable onDefinedCallBack, Class<E> type) {
    if(valueCallback == null && onDefinedCallBack == null) throw new IllegalArgumentException("Option must have a non-null callback");
    this.name = name;
    this.shorthand = shorthand;
    this.takesValue = valueCallback != null;
    this.onDefinedCallBack = onDefinedCallBack;
    this.valueCallback = valueCallback;
    this.valueConverter = takesValue ? findConverter(type) : null;
    if(!takesValue) valueHelpName = "";
  }

  /**
   * Defines an Option that does not take a value
   *
   * @param name
   *     the options name
   * @param shorthand
   *     the options shorthand
   * @param onSetCallback
   *     the callback to be run if the option is set
   */
  public static OptionDefinition<Void> nonArg(String name, char shorthand, Runnable onSetCallback) {
    if(name == null || name.isEmpty())
      throw new IllegalArgumentException("Option name cannot be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Option name cannot contain spaces");
    if(shorthand == ' ') throw new IllegalArgumentException("Option shorthand cannot be a space");
    return new OptionDefinition<>(name, shorthand, null, onSetCallback, Void.class);
  }

  /**
   * Defines an Option that does not take a value. The shorthand will be set to the first character
   * of the long name.
   *
   * @param name
   *     the options name
   * @param onSetCallback
   *     the callback to be run if the option is set
   */
  public static OptionDefinition<Void> nonArg(String name, Runnable onSetCallback) {
    if(name == null || name.isEmpty())
      throw new IllegalArgumentException("Option name cannot be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Option name cannot contain spaces");
    return new OptionDefinition<>(name, name.charAt(0), null, onSetCallback, Void.class);
  }

  public OptionDefinition<E> description(String description) {
    this.description = description;
    return this;
  }

  public String description() {
    return description;
  }

  public OptionDefinition<E> valueHelpName(String name) {
    this.valueHelpName = name;
    return this;
  }

  public String valueHelpName() {
    return valueHelpName;
  }

  public OptionDefinition<E> valuesConvertedBy(Function<String, E> converter) {
    this.valueConverter = converter;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof OptionDefinition && name.equals(((OptionDefinition<?>) obj).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
