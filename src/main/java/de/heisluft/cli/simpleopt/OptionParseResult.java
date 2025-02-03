package de.heisluft.cli.simpleopt;

import de.heisluft.cli.simpleopt.option.OptionDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The option parse result is the result of option parsing. It holds all the set options and their
 * values as well as the matched subcommand, if any, and the list of additional cli args.
 *
 * @since 0.2.0
 */
public final class OptionParseResult {
  /** The unmodifiable values of all set options. Never {@code null}. */
  public final @NotNull Map<OptionDefinition<?>, Object> options;
  /** The matched subcommand, may be {@code null}. */
  public final @Nullable String subcommand;
  /**
   * The unmodifiable list of all args that followed the last option or the sub-command.
   * Never {@code null}.
   */
  public final @NotNull List<String> additional;

  /**
   * Construct and populate the parse result. Non-public api, users should use
   * {@link OptionParser#parse(String...)} instead.
   *
   * @param options a map of the set options and their values, never {@code null}.
   * @param subcommand the matched subcommand, may be {@code null}.
   * @param additional the list of additional cli args, never {@code null}.
   */
  OptionParseResult(@NotNull Map<OptionDefinition<?>, Object> options, @Nullable String subcommand,
      @NotNull List<String> additional) {
    this.options = Collections.unmodifiableMap(options);
    this.subcommand = subcommand;
    this.additional = Collections.unmodifiableList(additional);
  }

  /**
   * Retrieves the parsed and converted value of an option, failing for options that do not take
   * values.
   *
   * @param option the option definition to query
   * @param <T> the type of the option value
   *
   * @return the value of the option or {@code null} if the option did not have a parsed value.
   *
   * @since 0.1.0
   */
  @SuppressWarnings("unchecked")
  public <T> @Nullable T getValue(@NotNull OptionDefinition<T> option) {
    if(!options.containsKey(option)) throw new IllegalArgumentException("Option " + option.name + " was not set");
    if(!option.takesValue) throw new IllegalArgumentException("Option " + option.name + " does not take a value");
    return (T) options.get(option);
  }

  /**
   * Queries whether a given option has been set.
   *
   * @param option the option to query
   *
   * @return {@code true} the option has been set, {@code false} otherwise.
   *
   * @since 0.1.0
   */
  public boolean isSet(@NotNull OptionDefinition<?> option) {
    return options.containsKey(option);
  }
}
