package de.heisluft.cli.simplecli;

/**
 * Represents a Result of validating an options or arguments value. Consists of two parts:
 * - Whether the value is valid
 * - If invalid, an error message to be displayed.
 *
 * @since 0.4.0
 */
public final class ValidationResult {

  /** The valid singleton. */
  private static final ValidationResult VALID = new ValidationResult(true, null);

  /** Whether the value was valid. */
  final boolean valid;
  /** The error message displayed for an invalid value. {@code null} if the value was valid. */
  final String message;

  /**
   * Private constructor, as this is an either, or type.
   *
   * @param valid whether the value vas valid.
   * @param message the error message displayed for an invalid value.
   */
  private ValidationResult(boolean valid, String message) {
    this.valid = valid;
    this.message = message;
  }

  /**
   * Indicate a valid value.
   *
   * @return a positive result
   */
  public static ValidationResult valid() {
    return VALID;
  }

  /**
   * Indicate and invalid value with the given error message.
   *
   * @param message the error message. May be {@code null}.
   * @return an instance indicating a negative result.
   */
  public static ValidationResult invalid(String message) {
    return new ValidationResult(false, message);
  }
}
