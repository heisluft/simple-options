package de.heisluft.cli.simpleopt.option;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class NonArgOptionBuilder extends OptionBuilder<Void, NonArgOptionBuilder> {

  @Override
  @Contract(pure = true, value = "-> new")
  @NotNull
  public OptionDefinition<Void> get() {
    return new OptionDefinition<>(name, shorthand != 0 ? shorthand : name.charAt(0), callback, description);
  }

  @Contract(pure = true, value = "null -> fail")
  public NonArgOptionBuilder(String name) {
    super(name);
  }
}
