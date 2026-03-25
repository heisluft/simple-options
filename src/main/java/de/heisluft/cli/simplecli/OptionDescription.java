package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Representation of an options description as used by {@link OptionParser#formatHelp(String, int)}.
 */
final class OptionDescription {
  /** The description of the option displayed by the help message. */
  final @NotNull String text;
  /** The name of the options value displayed by the help message. */
  final @NotNull String argName;

  /**
   * Construct an instance with text and argName string
   * @param text May be {@code null}, defaults to "".
   * @param argName May be {@code null}, defaults to "VALUE".
   */
  OptionDescription(@Nullable String text, @Nullable String argName) {
    this.text = text == null ? "" : text;
    this.argName = argName == null ? "VALUE" : argName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if(o == null || getClass() != o.getClass()) return false;
    OptionDescription that = (OptionDescription) o;
    return Objects.equals(text, that.text) && Objects.equals(argName, that.argName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(text, argName);
  }
}
