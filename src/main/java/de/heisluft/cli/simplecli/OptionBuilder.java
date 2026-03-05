package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public abstract class OptionBuilder<E, T extends OptionBuilder<E, T>> {
  final @NotNull String name;
  @NotNull OptionDescription description = new OptionDescription("", "");
  char shorthand;
  @Nullable Runnable callback;

  OptionBuilder(@NotNull String name) {
    if(name.isEmpty()) throw new IllegalArgumentException("Option name cannot be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Option name cannot contain spaces");
    this.name = name;
  }

  public final @NotNull T shorthand(char shorthand) {
    if(shorthand == ' ') throw new IllegalArgumentException("Option shorthand cannot be a space");
    this.shorthand = shorthand;
    return (T) this;
  }

  public final @NotNull T whenSet(@Nullable Runnable callback) {
    this.callback = callback;
    return (T) this;
  }

  public final @NotNull T description(@NotNull String description) {
    this.description = new OptionDescription(description, "VALUE");
    return (T) this;
  }

  public abstract @NotNull OptionDefinition<E> build();
}
