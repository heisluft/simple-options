package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Commands indicate what the program should do. There is always a root command, invoked if
 * no command was specified by the parsed argument string. Different commands may accept different
 * options. For adding an option to a set of commands, use
 * {@link OptionParser#addOptions(Predicate, OptionDefinition[])}.
 *
 * @since 0.4.0
 */
public final class Command {
  /** The name of the command. Empty for the root command. */
  public final @NotNull String name;
  /** The given description for help formatting. May be empty. */
  public final @NotNull String description;
  /** Whether this is a root command. */
  public final boolean isRoot;
  /** The set of all recognised options. */
  final @NotNull Set<OptionDefinition<?>> optionDefinitions = new HashSet<>();
  /** The set of all required arguments. */
  final @NotNull List<ArgDefinition<?>> requiredArguments = new ArrayList<>();

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

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(@Nullable Object o) {
    return o != null && getClass() == o.getClass() && Objects.equals(name, ((Command) o).name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  /**
   * Makes this command require all the given arguments in the given order.
   * No arguments in the array may be null.
   *
   * @param args the non-null array of arguments to accept. All Elements must be non-null.
   */
  public void addRequiredArgs(@NotNull ArgDefinition<?>... args) {
    for(ArgDefinition<?> arg : args) {
      if(arg == null) throw new IllegalArgumentException("Argument cannot be null");
      if(arg.valueConverter == null) throw new IllegalArgumentException("Argument value converter cannot be null");
      requiredArguments.add(arg);
    }
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
