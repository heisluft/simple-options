package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.heisluft.cli.simplecli.OptionParseException.Reason.ARG_GROUPING_CONFLICT;
import static de.heisluft.cli.simplecli.OptionParseException.Reason.CONVERSION_ERROR;
import static de.heisluft.cli.simplecli.OptionParseException.Reason.DUPLICATE_OPTION;
import static de.heisluft.cli.simplecli.OptionParseException.Reason.INVALID_OPTION;
import static de.heisluft.cli.simplecli.OptionParseException.Reason.MISSING_ARGUMENT;
import static de.heisluft.cli.simplecli.OptionParseException.Reason.MISSING_VALUE;
import static de.heisluft.cli.simplecli.OptionParseException.Reason.NO_MATCHING_COMMAND;
import static de.heisluft.cli.simplecli.OptionParseException.Reason.NULL_VALUE;
import static de.heisluft.cli.simplecli.OptionParseException.Reason.TRAILING_ARGUMENTS;

/**
 * A simple Command Line Parser allowing both valued and unvalued options to be parsed by their
 * {@link OptionDefinition#name long} or {@link OptionDefinition#shorthand short} name. Short Option
 * chaining is supported. Users of the API have the option to declare a set of recognized subcommands
 * to the parser, which the parser will then validate the arg string against. Note: if the subcommand
 * set is empty or the parsed argument array does not contain a subcommand, the ParseResult will have
 * its subcommand set to the root command.
 */
public final class OptionParser {
  /** A filter matching the root command only. */
  public static final Predicate<Command> ROOT_COMMAND = command -> command.isRoot;
  /** A filter matching every command. */
  public static final Predicate<Command> ALL_COMMANDS = command -> true;
  /** The amount of padding applied between each help column. */
  private static final int PADDING = 2;

  /**
   * The root command, used if no other command is given.
   *
   * @since 0.4.0
   */
  public final @NotNull Command rootCommand;
  /**
   * Whether this parser is strict, i.e. whether trailing strings are disallowed.
   */
  public final boolean strict;
  /** The set of all recognised commands. */
  private final @NotNull List<Command> namedCommands;

  /**
   * Constructs a new, strict, OptionParser and hands to it a collection of available commands.
   * The root command will not have a description.
   *
   * @param namedCommands the collection of available commands. may be null or empty, in which case
   * only the root command will exist.
   */
  public OptionParser(@Nullable Command... namedCommands) {
    this(true, "", namedCommands);
  }

  /**
   * Constructs a new, strict, OptionParser and hands to it a collection of available commands.
   * The root command will not have a description.
   *
   * @param strict set to true if trailing arguments should error.
   * @param namedCommands the collection of available commands. may be null or empty, in which case
   * only the root command will exist.
   *
   * @since 0.4.0
   */
  public OptionParser(boolean strict, @Nullable Command... namedCommands) {
    this(strict, "", namedCommands);
  }

  /**
   * Constructs a new, strict, OptionParser and hands to it a collection of available commands.
   *
   * @param rootDescription the non-null description of the root command, for help formatting.
   * @param namedCommands the collection of available commands. may be null or empty, in which case
   * only the root command will exist.
   *
   * @since 0.4.0
   */
  public OptionParser(String rootDescription, @NotNull Command... namedCommands) {
    this(true, rootDescription, namedCommands);
  }

  /**
   * Constructs a new OptionParser and hands to it a collection of available commands.
   *
   * @param strict set to true if trailing arguments should error.
   * @param rootDescription the non-null description of the root command, for help formatting.
   * @param namedCommands the collection of available commands. may be null or empty, in which case
   * only the root command will exist.
   *
   * @since 0.4.0
   */
  public OptionParser(boolean strict, @NotNull String rootDescription, @Nullable Command... namedCommands) {
    this.namedCommands = Collections.unmodifiableList(
        namedCommands == null ? Collections.emptyList() : Arrays.asList(namedCommands)
    );
    rootCommand = new Command(rootDescription);
    this.strict = strict;
  }

  /**
   * Constructs a filter matching all commands whose names are in {@code names}.
   *
   * @param names the list of matched names
   * @return the respective filter.
   */
  public static Predicate<Command> eachOf(String... names) {
    return command -> Arrays.asList(names).contains(command.name);
  }

  /**
   * Get all commands, including the root command.
   *
   * @return the set of all currently registered commands. Never null.
   *
   * @since 0.4.0
   */
  public @NotNull Set<Command> getCommands() {
    Set<Command> commands = new HashSet<>(namedCommands);
    commands.add(rootCommand);
    return commands;
  }

  /**
   * Add options to all commands matching {@code filter}. For the general contract see
   * {@link Command#addOptions(OptionDefinition...)}.
   *
   * @param filter the filter to apply
   * @param options the options to add
   */
  public void addOptions(@NotNull Predicate<Command> filter, @NotNull OptionDefinition<?>... options) {
    getCommands().forEach(command -> {
      if(filter.test(command)) command.addOptions(options);
    });
  }

  /**
   * Add required arguments to all commands matching {@code filter}. For the general contract see
   * {@link Command#addRequiredArgs(ArgDefinition...)}.
   *
   * @param filter the filter to apply
   * @param requiredArgs the arguments to add
   */
  public void addRequiredArgs(@NotNull Predicate<Command> filter, @NotNull ArgDefinition<?>... requiredArgs) {
    getCommands().forEach(command -> {
      if(filter.test(command)) command.addRequiredArgs(requiredArgs);
    });
  }

  /**
   * Parses the Command Line for all its defined Options, invoking callbacks of those who are set.
   * Fails with an error message if something goes wrong. Parsing is stopped after the first
   * non-option argument is encountered. If the user has declared a set of available subcommands to
   * the parser, this string will then be matched against said set. If not, the string will be added
   * to the remainder list of the parse result. All following args will be added to the remainder
   * list of the parse result.
   *
   * @param args the CLI arguments to parse
   *
   * @return the parse result
   *
   * @throws OptionParseException if an error occurs during parsing, such as an option being defined
   * twice, an option not having a value when it requires one or a grouping conflict.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public @NotNull OptionParseResult parse(@NotNull String... args) {
    List<String> remainder = new ArrayList<>();
    Set<OptionDefinition> allOptions = new HashSet<>();
    getCommands().forEach(c -> allOptions.addAll(c.optionDefinitions));
    Map<OptionDefinition, String> rawOptions = new HashMap<>();
    Map<ArgDefinition<?>, Object> arguments = new HashMap<>();
    Command command = rootCommand;
    argLoop:
    for(int i = 0; i < args.length; i++) {
      String arg = args[i];
      if(arg.startsWith("--")) {
        for(OptionDefinition o : allOptions) {
          if(arg.substring(2).startsWith(o.name)) {
            if(!o.takesValue) {
              if(!o.name.equals(arg.substring(2))) continue;
              if(rawOptions.containsKey(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              rawOptions.put(o, null);
            } else {
              String val = arg.substring(o.name.length() + 2);
              if(!val.startsWith("=") || val.length() == 1) throw new OptionParseException(MISSING_VALUE, o.name);
              if(rawOptions.containsKey(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              rawOptions.put(o, val.substring(1));
            }
            continue argLoop;
          }
        }
        throw new OptionParseException(INVALID_OPTION, "--" + arg);
      } else if(arg.startsWith("-")) {
        boolean argumentDefined = false;
        charLoop:
        for(char c : arg.substring(1).toCharArray()) {
          for(OptionDefinition o : allOptions) {
            if(o.shorthand == c) {
              if(rawOptions.containsKey(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              if(o.takesValue) {
                if(argumentDefined) throw new OptionParseException(ARG_GROUPING_CONFLICT, arg);
                if(args.length == i + 1) throw new OptionParseException(MISSING_VALUE, o.name);
                rawOptions.put(o, args[++i]);
                argumentDefined = true;
              } else rawOptions.put(o, null);
              continue charLoop;
            }
          }
          throw new OptionParseException(INVALID_OPTION, "-" + c);
        }
      } else {
        if(!namedCommands.isEmpty()) {
          Command c = namedCommands.stream()
              .filter(cmd -> cmd.name.equals(arg))
              .findFirst().orElse(null);
          if(c == null) throw new OptionParseException(NO_MATCHING_COMMAND, arg);
          command = c;
        }
        int argIdx = 0;
        for(int j = i + (namedCommands.isEmpty() ? 0 : 1); j < args.length; j++) {
          if(argIdx < command.requiredArguments.size()) {
            ArgDefinition argDef = command.requiredArguments.get(argIdx++);
            Object value = args[j];
            try {
              value = argDef.valueConverter != null ? argDef.valueConverter.apply(value) : value;
            } catch(Exception e) {
              throw new OptionParseException(CONVERSION_ERROR, value.toString(),
                  "argument " + argDef.name, e);
            }
            if(value == null) throw new OptionParseException(NULL_VALUE, args[j], "argument " + argDef.name);
            arguments.put(argDef, value);
          } else remainder.add(args[j]);
        }
        if(argIdx < command.requiredArguments.size())
          throw new OptionParseException(MISSING_ARGUMENT, command.requiredArguments.get(argIdx).name);
        if(strict && !remainder.isEmpty())
          throw new OptionParseException(TRAILING_ARGUMENTS, remainder.toString());
        // arg chain is supposed to be continuous
        break;
      }
    }
    Map<OptionDefinition<?>, Object> optionValues = new HashMap<>();
    Command finalCommand = command;
    rawOptions.forEach((k, v) -> {
      if(!finalCommand.optionDefinitions.contains(k))
        throw new OptionParseException(INVALID_OPTION, "--" + k.name);
      Object value;
      try {
        value = k.valueConverter != null ? k.valueConverter.apply(v) : v;
      } catch(Exception e) {
        throw new OptionParseException(CONVERSION_ERROR, v, "option --" + k.name, e);
      }
      if(value == null && k.takesValue) throw new OptionParseException(NULL_VALUE, v, "option " + k.name);
      optionValues.put(k, value);
    });
    arguments.forEach((k, v) -> {
      if(k.valueCallback != null) k.valueCallback.accept(v);
    });
    optionValues.forEach((k, v) -> {
      if(k.onDefinedCallBack != null) k.onDefinedCallBack.run();
      if(k.valueCallback != null) k.valueCallback.accept(v);
    });
    return new OptionParseResult(optionValues, arguments, command.isRoot ? null : command.name, remainder);
  }

  private static @NotNull StringBuilder wrapIndent(@NotNull StringBuilder out, int indent, int max) {
    int remain = out.length();
    int lastWrap = 0;
    while(remain > max) {
      int i = max + lastWrap;
      while(out.charAt(i) != ' ') i--;
      out.setCharAt(i, '\n');
      for(int j = 0; j < indent; j++) out.insert(i + 1, ' ');
      lastWrap = i;
      remain = out.length() - lastWrap;
    }
    return out;
  }

  /**
   * Formats and gives back a help string to print for user help, explaining all args with their
   * descriptions, if such descriptions have been set as well as naming values, if such a name
   * has been set.
   *
   * @param header An optional header message, not including a newline.
   * @param maxWidth the maximum line width
   *
   * @return the formatted help string.
   *
   * @since 0.3.0
   */
  // TODO: Think of root command description meaning as well as computation of "valid for" appendices.
  public @NotNull String formatHelp(@Nullable String header, int maxWidth) {
    StringBuilder sb = header != null && !header.isEmpty() ? new StringBuilder(header).append('\n')
        : new StringBuilder();
    if(!namedCommands.isEmpty()) {
      sb.append("Available commands:\n");
      for(Command sc : namedCommands) {
        sb.append("  ").append(sc.name).append(":\n")
            .append(wrapIndent(new StringBuilder("    ").append(sc.description), 4, maxWidth)).append("\n");
      }
    }
    List<OptionDefinition<?>> optionDefinitions = getCommands()
        .stream()
        .flatMap(c -> c.optionDefinitions.stream())
        .distinct()
        .sorted(Comparator.comparing(s -> s.name))
        .collect(Collectors.toList());
    sb.append("Options:\nOption");
    int maxLongLen = Math.max(
        optionDefinitions.stream()
            .mapToInt(o -> 2 + o.name.length() + (o.takesValue ? 1 + o.description.argName.length() : 0))
            .max().orElse(0),
        "Option".length());
    int maxShortLen = Math.max(
        optionDefinitions.stream()
            .mapToInt(o -> o.takesValue ? 3 + o.description.argName.length() : 2)
            .max().orElse(0),
        "Shorthand".length());
    for(int i = 0; i < maxLongLen + PADDING - "Option".length(); i++) sb.append(' ');
    sb.append("Shorthand");
    for(int i = 0; i < maxShortLen + PADDING - "Shorthand".length(); i++) sb.append(' ');
    int descriptionIndent = sb.length() - sb.lastIndexOf("\n") - 1;
    sb.append("Description\n");
    for(OptionDefinition<?> o : optionDefinitions) {
      StringBuilder sb2 = new StringBuilder();
      OptionDescription desc = o.description;
      sb2.append("--").append(o.name);
      int longLen = 2 + o.name.length() + (o.takesValue ? 1 + desc.argName.length() : 0);
      if(o.takesValue) sb2.append('=').append(desc.argName);
      for(int j = 0; j < maxLongLen - longLen + PADDING; j++) sb2.append(' ');
      sb2.append('-').append(o.shorthand);
      if(o.takesValue) sb2.append(' ').append(desc.argName);
      int shortLen = o.takesValue ? 3 + desc.argName.length() : 2;
      for(int j = 0; j < maxShortLen - shortLen + PADDING; j++) sb2.append(' ');
      sb2.append(desc.text);
      sb.append(wrapIndent(sb2, descriptionIndent, maxWidth)).append("\n\n");
    }
    return sb.toString();
  }
}
