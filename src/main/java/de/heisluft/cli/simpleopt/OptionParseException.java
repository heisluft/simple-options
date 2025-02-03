package de.heisluft.cli.simpleopt;

import org.jetbrains.annotations.NotNull;

/**
 * This Exception is thrown whenever the {@link OptionParser} fails to parse a command line string
 * unambiguously. It always has a reason attached for easier handling.
 *
 * @since 0.1.0
 */
public class OptionParseException extends RuntimeException {
  /**
   * Denotes the reason why option parsing failed.
   */
  public enum Reason {
    /** An option was defined twice. */
    DUPLICATE_OPTION("Option '{0}' is defined twice"),
    /** An option requiring a value did not have its value set. */
    MISSING_VALUE("Option '{0}' requires an argument, but none is given"),
    /** Multiple options declaring consumption of a value defined in same short option grouping. */
    ARG_GROUPING_CONFLICT("Multiple options with required arguments defined in the same group '{0}'"),
    /**
     * The given subcommand is not recognized by this parser.
     *
     * @since 0.2.0
     */
    NO_MATCHING_SUBCOMMAND("'{0}' is not a valid subcommand");
    /**
     * The template for constructing an exception message. {@code {0}} is substituted for the
     * offending options long name.
     */
    @NotNull
    private final String msgTemplate;

    /**
     * Constructor for the reason.
     *
     * @param msgTemplate The template for constructing an exception message. {@code {0}} is
     * substituted for the string representation of the offending entity in
     * {@link #getMessage(String)}.
     */
    private Reason(@NotNull String msgTemplate) {
      this.msgTemplate = msgTemplate;
    }

    /**
     * Generates an error message based on this enum values template as well as the given input
     * string. The pattern {@code {0}} of the template is substituted with the input string.
     *
     * @param offender a string representation of the offending entity.
     *
     * @return the error message string.
     */
    @NotNull
    String getMessage(@NotNull String offender) {
      return msgTemplate.replace("{0}", offender);
    }
  }
  /** The reason why parsing failed. */
  @NotNull
  public final Reason reason;
  /**
   * The string representation of the entity that caused parsing to fail.
   *
   * @since 0.2.0
   */
  @NotNull
  public final String offender;

  /**
   * Constructs a new instance. The message will be auto-generated by
   * {@link Reason#getMessage(String)}.
   *
   * @param reason the reason why parsing failed.
   * @param offender the string representation of the entity that caused parsing to fail.
   */
  OptionParseException(@NotNull Reason reason, @NotNull String offender) {
    super(reason.getMessage(offender));
    this.reason = reason;
    this.offender = offender;
  }
}
