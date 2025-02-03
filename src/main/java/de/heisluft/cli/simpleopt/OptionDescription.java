package de.heisluft.cli.simpleopt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final class OptionDescription {
  final @NotNull String text;
  final @NotNull String argName;

  OptionDescription(@Nullable String text, @Nullable String argName) {
    this.text = text == null ? "" : text;
    this.argName = argName == null ? "VALUE" : argName;
  }

  @Override
  public boolean equals(Object o) {
    if(o == null || getClass() != o.getClass()) return false;
    OptionDescription that = (OptionDescription) o;
    return Objects.equals(text, that.text) && Objects.equals(argName, that.argName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, argName);
  }
}
