package de.heisluft.cli.simpleopt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class SubCommand {
  @NotNull
  public final String name;
  @NotNull
  public final String description;

  public SubCommand(@Nullable String name, @Nullable String description) {
    if (name == null) throw new NullPointerException("name must not be null");
    this.name = name;
    this.description = description == null ? "" : description;
  }

  @Override
  public boolean equals(Object o) {
    return o != null && getClass() == o.getClass() && Objects.equals(name, ((SubCommand) o).name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
