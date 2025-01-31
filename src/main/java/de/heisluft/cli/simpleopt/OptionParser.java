package de.heisluft.cli.simpleopt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.heisluft.cli.simpleopt.OptionParseException.Reason.*;

/**
 * A simple Command Line Parser allowing both valued and unvalued options to be parsed by their {@link OptionDefinition#name long}
 * or {@link OptionDefinition#shorthand short} name. Short Option chaining is supported.
 */
public class OptionParser {
  /** The set of all defined Options */
  private final Set<OptionDefinition<?>> optionDefinitions = new HashSet<>();

  /**
   * Add parseable Options to the set of defined Options
   *
   * @param options the Options to add
   */
  public void addOptions(OptionDefinition<?>... options) {
    for(OptionDefinition<?> option : options) {
      if(option.takesValue && option.valueConverter == null)
        throw new IllegalArgumentException("Option " + option.name + " has no value converter");
      optionDefinitions.add(option);
    }
  }

  /**
   * Parses the Command Line for all its defined Options, invoking callbacks of those who are set.
   * Fails with an error message and error code if something goes wrong;
   * 
   * Error code 1: An option was defined twice
   * 
   * Error code 2: An Option which requires an argument is not supplied with one
   * 
   * Error code 3: A chain of short options contains two or more options requiring an argument
   *
   * @param args the CLI arguments to parse
   */
  @SuppressWarnings("unchecked")
  public List<String> parse(String... args) {
    Set<OptionDefinition> setOptions = new HashSet<>(optionDefinitions.size());
    List<String> remainder = new ArrayList<>();
    argLoop:
    for(int i = 0; i < args.length; i++) {
      String arg = args[i];
      if(arg.startsWith("--")) {
        for(OptionDefinition o : optionDefinitions) {
          if(arg.substring(2).startsWith(o.name)) {
            if(!o.takesValue) {
              if(!o.name.equals(arg.substring(2))) continue;
              if(setOptions.contains(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              o.onDefinedCallBack.run();
            } else {
              String val = arg.substring(o.name.length() + 2);
              if(!val.startsWith("=") || val.length() == 1) throw new OptionParseException(MISSING_VALUE, o.name);
              if(setOptions.contains(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              o.valueCallback.accept(o.valueConverter.apply(val.substring(1)));
            }
            setOptions.add(o);
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
              if(setOptions.contains(o)) throw new OptionParseException(DUPLICATE_OPTION, o.name);
              if(o.takesValue) {
                if(argumentDefined) throw new OptionParseException(ARG_GROUPING_CONFLICT, o.name);
                if(args.length == i + 1) throw new OptionParseException(MISSING_VALUE, o.name);
                o.valueCallback.accept(o.valueConverter.apply(args[++i]));
                argumentDefined = true;
              } else o.onDefinedCallBack.run();
              setOptions.add(o);
              continue charLoop;
            }
          }
          System.out.println("Unknown short option supplied: '-" + c + "'");
        }
      } else remainder.add(arg);
    }
    return remainder;
  }

  public String printHelp(String header) {
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
