package de.heisluft.cli.simpleopt.option;

import org.jetbrains.annotations.NotNull;

public final class NonArgOptionBuilder extends OptionBuilder<Void, NonArgOptionBuilder> {

  @Override
  public @NotNull OptionDefinition<Void> get() {
    return new OptionDefinition<>(name, shorthand != 0 ? shorthand : name.charAt(0), callback, description);
  }

  public NonArgOptionBuilder(String name) {
    super(name);
  }
}
