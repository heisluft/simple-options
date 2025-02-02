package de.heisluft.cli.simpleopt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.heisluft.cli.simpleopt.OptionParseException.Reason.*;

/**
 * A simple Command Line Parser allowing both valued and unvalued options to be parsed by their
 * {@link OptionDefinition#name long} or {@link OptionDefinition#shorthand short} name. Short Option
 * chaining is supported.
 *
 * @since 0.0.1
 */
public final class OptionParser {
  /** The set of all recognised options. */
  private final Set<OptionDefinition<?>> optionDefinitions = new HashSet<>();
  /**
   * A map of all options mapped to their parsed values.
   * @since 0.1.0
   */
  private final Map<OptionDefinition<?>, Object> optionValues = new HashMap<>();

  /**
   * Add Option definitions to the set of recognized options.
   *
   * @param options the options to add
   */
  public void addOptions(OptionDefinition<?>... options) {
    for(OptionDefinition<?> option : options) {
      if(option.takesValue && option.valueConverter() == null)
        throw new IllegalArgumentException("Option " + option.name + " has no value converter");
      optionDefinitions.add(option);
    }
  }

  /**
   * Retrieves the parsed and converted value of an option, failing for options that do not take
   * values.
   *
   * @param option the option definition to query
   * @param <T> the type of the option value
   *
   * @return the value of the option or {@code null} if the option did not have a parsed value.
   *
   * @since 0.1.0
   */
  @SuppressWarnings("unchecked")
  public <T> T getValue(OptionDefinition<T> option) {
    if(!optionDefinitions.contains(option)) throw new IllegalArgumentException("Option " + option.name + " is not recognised by this parser");
    if(!option.takesValue) throw new IllegalArgumentException("Option " + option.name + " does not take a value");
    return optionValues.containsKey(option) ? (T) optionValues.get(option) : null;
  }

  /**
   * Queries whether a given option has been set.
   *
   * @param option the option to query
   *
   * @return {@code true} the option has been set, {@code false} otherwise.
   *
   * @since 0.1.0
   */
  public boolean isSet(OptionDefinition<?> option) {
    if(!optionDefinitions.contains(option)) throw new IllegalArgumentException("Option " + option.name + " is not recognised by this parser");
    return optionValues.containsKey(option);
  }

  /**
   * Parses the Command Line for all its defined Options, invoking callbacks of those who are set.
   * Fails with an error message if something goes wrong.
   *
   * @param args the CLI arguments to parse
   *
   * @return a list of all strings (in parse order) that
   * could not be matched to an option definition.
   *
   * @throws OptionParseException if an error occurs during parsing, such as an option being defined
   * twice, an option not having a value when it requires one or a grouping conflict.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public List<String> parse(String... args) {
    List<String> remainder = new ArrayList<>();
    argLoop:
    for(int i = 0; i < args.length; i++) {
      String arg = args[i];
      if(arg.startsWith("--")) {
        for(OptionDefinition o : optionDefinitions) {
          if(arg.substring(2).startsWith(o.name)) {
            if(!o.takesValue) {
              if(!o.name.equals(arg.substring(2))) continue;
              if(optionValues.containsKey(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              o.onDefinedCallBack.run();
              optionValues.put(o, true);
            } else {
              String val = arg.substring(o.name.length() + 2);
              if(!val.startsWith("=") || val.length() == 1) throw new OptionParseException(MISSING_VALUE, o.name);
              if(optionValues.containsKey(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              Object value = o.valueConverter().apply(val.substring(1));
              optionValues.put(o, value);
              o.valueCallback.accept(value);
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
                if(argumentDefined) throw new OptionParseException(ARG_GROUPING_CONFLICT, o.name);
                if(args.length == i + 1) throw new OptionParseException(MISSING_VALUE, o.name);
                Object value = o.valueConverter().apply(args[++i]);
                o.valueCallback.accept(value);
                argumentDefined = true;
              } else {
                o.onDefinedCallBack.run();
                optionValues.put(o, true);
              }
              continue charLoop;
            }
          }
          System.out.println("Unknown short option supplied: '-" + c + "'");
        }
      } else remainder.add(arg);
    }
    return remainder;
  }

  /**
   * Formats and gives back a help string to print for user help, explaining all args with their
   * descriptions, if such descriptions have been set as well as naming values, if such a name
   * has been set.
   *
   * @param header An optional header message, not including a newline.
   *
   * @return the formatted help string.
   *
   * @see OptionDefinition#valueHelpName(String)
   * @see OptionDefinition#description(String)
   */
  public String formatHelp(String header) {
    StringBuilder sb = new StringBuilder(header).append("\nOptions:\nOption");
    int maxLongLen = Math.max(optionDefinitions.stream().mapToInt(o -> 2 + o.name.length() + (o.takesValue ?  1 + o.valueHelpName().length() : 0)).max().orElse(0), "Option".length());
    int maxShortLen = Math.max(optionDefinitions.stream().mapToInt(o -> o.takesValue ? 3 + o.valueHelpName().length() : 2).max().orElse(0), "Shorthand".length());
    for(int i = 0; i < maxLongLen + 2 - "Option".length(); i++) sb.append(' ');
    sb.append("Shorthand");
    for(int i = 0; i < maxShortLen + 2 - "Shorthand".length(); i++) sb.append(' ');
    sb.append("Description\n");
    List<OptionDefinition<?>> sorted = new ArrayList<>(optionDefinitions);
    sorted.sort(Comparator.comparing(s -> s.name));
    for(OptionDefinition<?> o : sorted) {
      sb.append("--").append(o.name);
      int longLen = 2 + o.name.length() + (o.takesValue ? 1 + o.valueHelpName().length() : 0);
      if(o.takesValue) sb.append('=').append(o.valueHelpName());
      for(int j = 0; j < maxLongLen - longLen + 2; j++) sb.append(' ');
      sb.append('-').append(o.shorthand);
      if(o.takesValue) sb.append(' ').append(o.valueHelpName());
      int shortLen = o.takesValue ? 3 + o.valueHelpName().length() : 2;
      for(int j = 0; j < maxShortLen - shortLen + 2; j++) sb.append(' ');
      sb.append(o.description()).append("\n\n");
    }
    return sb.toString();
  }
}
