package de.heisluft.cli.simpleopt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Commands indicate what the program should do. There is always a root command, invoked if
 * no command was specified by the parsed argument string. Different commands may accept different
 * options. For adding an option to a set of commands, use {@link OptionParser#forEachCommand(Consumer)}.
 */
public final class Command {
  /** The set of all recognised options. */
  final @NotNull Set<OptionDefinition<?>> optionDefinitions = new HashSet<>();
  /** The name of the command. Empty for the root command. */
  public final @NotNull String name;
  /** The given description for help formatting. May be empty. */
  public final @NotNull String description;
  /** Whether this is a root command. */
  public final boolean isRoot;

  /**
   * Construct a root command.
   *
   * @param rootDescription the optional description for help formatting. May be empty
   */
  Command(@NotNull String rootDescription) {
    name = "";
    description = rootDescription;
    isRoot = true;
  }

  /**
   * Construct a command.
   *
   * @param name The name of the command. Must be a non-null, non-empty String
   * @param description the optional description for help formatting. May be null
   */
  public Command(@NotNull String name, @Nullable String description) {
    if(name.isEmpty()) throw new IllegalArgumentException("name cannot be empty");
    this.name = name;
    this.description = description == null ? "" : description;
    isRoot = false;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return o != null && getClass() == o.getClass() && Objects.equals(name, ((Command) o).name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  /**
   * Makes this command accept all the given options. No option in the array may be null.
   *
   * @param options the non-null array of options to accept. All Elements must be non-null.
   */
  public void addOptions(@NotNull OptionDefinition<?>... options) {
    for(OptionDefinition<?> option : options) {
      if(option == null) throw new IllegalArgumentException("Null option supplied");
      if(option.takesValue && option.valueConverter == null)
        throw new IllegalArgumentException("Option " + option.name + " has no value converter");
      optionDefinitions.add(option);
    }
  }
}
