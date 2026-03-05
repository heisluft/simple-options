package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;

public final class FlagOptionBuilder extends OptionBuilder<Void, FlagOptionBuilder> {

  @Override
  public @NotNull OptionDefinition<Void> build() {
    return new OptionDefinition<>(name, shorthand != 0 ? shorthand : name.charAt(0), callback, description);
  }

  public FlagOptionBuilder(String name) {
    super(name);
  }
}
