package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A stateful builder for {@link OptionDefinition OptionDefinitions}. All parameters except the name
 * are changeable. Every method except {@link #build()} returns this instance to allow for chaining.
 * The build method is to be called for finalizing the parameters, yielding an OptionDefinition of
 * the given value type.
 *
 * @param <E> the option values type.
 * @param <T> the OptionBuilder type used for chaining.
 *
 * @since 0.2.0
 */
@SuppressWarnings("unchecked")
public abstract class OptionBuilder<E, T extends OptionBuilder<E, T>> {
  /** The options long name. */
  final @NotNull String name;
  /** The options description. */
  @NotNull OptionDescription description = new OptionDescription("", "");
  /** The short option name. */
  char shorthand;
  /** A callback to be invoked if the option was set. */
  @Nullable Runnable callback;

  /**
   * Construct an instance with a given name.
   *
   * @param name the options long name. Must not be {@code null} or empty, nor contain spaces.
   */
  OptionBuilder(@NotNull String name) {
    if(name.isEmpty()) throw new IllegalArgumentException("Option name cannot be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Option name cannot contain spaces");
    this.name = name;
  }

  /**
   * Set this options shorthand name.
   *
   * @param shorthand the new shorthand name. Must not be a space.
   * @return this
   */
  public final @NotNull T shorthand(char shorthand) {
    if(shorthand == ' ') throw new IllegalArgumentException("Option shorthand cannot be a space");
    this.shorthand = shorthand;
    return (T) this;
  }

  /**
   * Set a callback to be invoked once this options value is finalized.
   * If {@code null}, no callback will be invoked.
   *
   * @param callback the callback code. May be {@code null.}
   * @return this
   */
  public final @NotNull T whenSet(@Nullable Runnable callback) {
    this.callback = callback;
    return (T) this;
  }

  /**
   * Set this options description as used in the {@link OptionParser#formatHelp(String, int)} method.
   * If the option takes a value, it will show as {@code VALUE}.
   *
   * @param description the options description. Must not be {@code null}.
   * @return this
   */
  public final @NotNull T description(@NotNull String description) {
    this.description = new OptionDescription(description, "VALUE");
    return (T) this;
  }

  /**
   * Build and return the resulting OptionDefinition, finalizing the parameters set previously.
   *
   * @return the built definition
   */
  public abstract @NotNull OptionDefinition<E> build();
}
