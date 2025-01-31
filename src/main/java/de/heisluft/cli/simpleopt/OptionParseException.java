package de.heisluft.cli.simpleopt;

public class OptionParseException extends RuntimeException {
  public enum Reason {
    DUPLICATE_OPTION("Option '{0}' is defined twice"),
    MISSING_VALUE("Option '{0}' requires an argument, but none is given"),
    ARG_GROUPING_CONFLICT("Multiple options with required arguments defined in the same group");
    private final String msgTemplate;
    private Reason(String msgTemplate) {
      this.msgTemplate = msgTemplate;
    }
    String getMessage(String option) {
      return msgTemplate.replace("{0}", option);
    }
  }
  public final Reason reason;
  public final String option;

  public OptionParseException(Reason reason, String option) {
    super(reason.getMessage(option));
    this.reason = reason;
    this.option = option;
  }
}
