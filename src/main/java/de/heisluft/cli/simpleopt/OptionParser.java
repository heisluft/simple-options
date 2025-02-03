package de.heisluft.cli.simpleopt;

import de.heisluft.cli.simpleopt.option.OptionDefinition;
import de.heisluft.cli.simpleopt.option.OptionDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static de.heisluft.cli.simpleopt.OptionParseException.Reason.*;

/**
 * A simple Command Line Parser allowing both valued and unvalued options to be parsed by their
 * {@link OptionDefinition#name long} or {@link OptionDefinition#shorthand short} name. Short Option
 * chaining is supported. Users of the API have the option to declare a set of recognized subcommands
 * to the parser, which the parser will then validate the arg string against. Note: if the subcommand
 * set is empty or the parsed argument array does not contain a subcommand, the ParseResult will have
 * its subcommand set to {@code null.}
 *
 * @since 0.0.1
 */
public final class OptionParser {
  /** The set of all recognised options. */
  @NotNull
  private final Set<OptionDefinition<?>> optionDefinitions = new HashSet<>();
  /** The set of all recognised subcommands. */
  @NotNull
  private final List<SubCommand> subcommands;

  /**
   * Add Option definitions to the set of recognized options.
   *
   * @param options the options to add
   */
  public final void addOptions(@NotNull OptionDefinition<?>... options) {
    for(OptionDefinition<?> option : options) {
      if(option.takesValue && option.valueConverter == null)
        throw new IllegalArgumentException("Option " + option.name + " has no value converter");
      optionDefinitions.add(option);
    }
  }

  /**
   * Constructs a new OptionParser and hands to it a collection of available subcommands.
   *
   * @param subcommands the collection of available subcommands. may be null or empty, indicating to
   * the parser that no subcommand matching shall be done
   */
  public OptionParser(@Nullable SubCommand... subcommands) {
    this.subcommands = Collections.unmodifiableList(subcommands == null ? Collections.emptyList() : Arrays.asList(subcommands));
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
    Map<OptionDefinition<?>, Object> optionValues = new HashMap<>();
    String subcommand = null;
    argLoop:
    for(int i = 0; i < args.length; i++) {
      String arg = args[i];
      if(arg.startsWith("--")) {
        for(OptionDefinition o : optionDefinitions) {
          if(arg.substring(2).startsWith(o.name)) {
            if(!o.takesValue) {
              if(!o.name.equals(arg.substring(2))) continue;
              if(optionValues.containsKey(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              if(o.onDefinedCallBack != null) o.onDefinedCallBack.run();
              optionValues.put(o, true);
            } else {
              String val = arg.substring(o.name.length() + 2);
              if(!val.startsWith("=") || val.length() == 1) throw new OptionParseException(MISSING_VALUE, o.name);
              if(optionValues.containsKey(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              Object value = o.valueConverter.apply(val.substring(1));
              optionValues.put(o, value);
              if(o.valueCallback != null) o.valueCallback.accept(value);
              if(o.onDefinedCallBack != null) o.onDefinedCallBack.run();
            }
            continue argLoop;
          }
        }
        System.out.println("Unknown long option supplied: '--" + arg + "'");
      } else if(arg.startsWith("-")) {
        boolean argumentDefined = false;
        charLoop:
        for(char c : arg.substring(1).toCharArray()) {
          for(OptionDefinition o : optionDefinitions) {
            if(o.shorthand == c) {
              if(optionValues.containsKey(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              if(o.takesValue) {
                if(argumentDefined) throw new OptionParseException(ARG_GROUPING_CONFLICT, arg);
                if(args.length == i + 1) throw new OptionParseException(MISSING_VALUE, o.name);
                Object value = o.valueConverter.apply(args[++i]);
                if(o.valueCallback != null) o.valueCallback.accept(value);
                if(o.onDefinedCallBack != null) o.onDefinedCallBack.run();
                argumentDefined = true;
              } else {
                if(o.onDefinedCallBack != null) o.onDefinedCallBack.run();
                optionValues.put(o, true);
              }
              continue charLoop;
            }
          }
          System.out.println("Unknown short option supplied: '-" + c + "'");
        }
      } else {
        if(!subcommands.isEmpty()) {
          if(!subcommands.contains(new SubCommand(arg, null))) throw new OptionParseException(NO_MATCHING_SUBCOMMAND, arg);
          subcommand = arg;
        }
        for(int j = i + (subcommands.isEmpty() ? 0 : 1); j < args.length; j++) remainder.add(args[j]);
        // arg chain is supposed to be continuous
        break;
      }
    }
    return new OptionParseResult(optionValues, subcommand, remainder);
  }

  /**
   * Formats and gives back a help string to print for user help, explaining all args with their
   * descriptions, if such descriptions have been set as well as naming values, if such a name
   * has been set.
   *
   * @param header An optional header message, not including a newline.
   *
   * @return the formatted help string.
   */
  public @NotNull String formatHelp(@Nullable String header) {
    StringBuilder sb = header != null ? new StringBuilder(header).append('\n') : new StringBuilder();
    if(!subcommands.isEmpty()) {
      sb.append("Available subcommands:\n");
      for(SubCommand sc : subcommands)
        sb.append("  ").append(sc.name).append(":\n").append(sc.description).append("\n");
    }
    sb.append("Options:\nOption");
    int maxLongLen = Math.max(optionDefinitions.stream().mapToInt(o -> 2 + o.name.length() + (o.takesValue ?  1 + o.description.argName.length() : 0)).max().orElse(0), "Option".length());
    int maxShortLen = Math.max(optionDefinitions.stream().mapToInt(o -> o.takesValue ? 3 + o.description.argName.length() : 2).max().orElse(0), "Shorthand".length());
    for(int i = 0; i < maxLongLen + 2 - "Option".length(); i++) sb.append(' ');
    sb.append("Shorthand");
    for(int i = 0; i < maxShortLen + 2 - "Shorthand".length(); i++) sb.append(' ');
    sb.append("Description\n");
    List<OptionDefinition<?>> sorted = new ArrayList<>(optionDefinitions);
    sorted.sort(Comparator.comparing(s -> s.name));
    for(OptionDefinition<?> o : sorted) {
      OptionDescription desc = o.description;
      sb.append("--").append(o.name);
      int longLen = 2 + o.name.length() + (o.takesValue ? 1 + desc.argName.length() : 0);
      if(o.takesValue) sb.append('=').append(desc.argName);
      for(int j = 0; j < maxLongLen - longLen + 2; j++) sb.append(' ');
      sb.append('-').append(o.shorthand);
      if(o.takesValue) sb.append(' ').append(desc.argName);
      int shortLen = o.takesValue ? 3 + desc.argName.length() : 2;
      for(int j = 0; j < maxShortLen - shortLen + 2; j++) sb.append(' ');
      sb.append(desc.text).append("\n\n");
    }
    return sb.toString();
  }
}
