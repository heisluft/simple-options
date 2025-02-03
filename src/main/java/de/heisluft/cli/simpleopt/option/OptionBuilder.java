package de.heisluft.cli.simpleopt.option;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class OptionBuilder<E, T extends OptionBuilder<E, T>> implements Supplier<OptionDefinition<E>> {
  @NotNull
  final String name;
  @NotNull
  OptionDescription description = new OptionDescription("", "");
  char shorthand;
  @Nullable
  Runnable callback;

  @Contract("null -> fail")
  protected OptionBuilder(@Nullable String name) {
    if(name == null || name.isEmpty()) throw new IllegalArgumentException("Option name cannot be empty");
    if(name.contains(" ")) throw new IllegalArgumentException("Option name cannot contain spaces");
    this.name = name;
  }

  @NotNull
  public T shorthand(char shorthand) {
    if(shorthand == ' ') throw new IllegalArgumentException("Option shorthand cannot be a space");
    this.shorthand = shorthand;
    return (T) this;
  }

  @NotNull
  public T whenSet(@Nullable Runnable callback) {
    this.callback = callback;
    return (T) this;
  }

  @Contract("null -> fail")
  @NotNull
  public T description(@Nullable OptionDescription description) {
    if(description == null) throw new IllegalArgumentException("Option description cannot be null");
    this.description = description;
    return (T) this;
  }
}
