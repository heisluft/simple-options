package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;

/**
 * This Builder class builds an OptionDefinition which does not take a value. All methods mutate the builder.
 * Instances can be obtained by invoking the "flag" methods within {@link OptionDefinition}.
 */
public final class FlagOptionBuilder extends OptionBuilder<Void, FlagOptionBuilder> {

  /**
   * This internal constructor sets the final name.
   *
   * @param name the final, not-null name of the resulting option
   */
  FlagOptionBuilder(@NotNull String name) {
    super(name);
  }

  @Override
  public @NotNull OptionDefinition<Void> build() {
    return new OptionDefinition<>(name, shorthand != 0 ? shorthand : name.charAt(0), callback, description);
  }
}
