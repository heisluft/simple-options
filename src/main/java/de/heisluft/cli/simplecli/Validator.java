package de.heisluft.cli.simplecli;

import org.jetbrains.annotations.NotNull;

/**
 * A function used to validate an options or arguments value after conversion. Option parsing will
 * fail if the value is invalid.
 *
 * @param <E> The values type
 * @since 0.4.0
 */
public interface Validator<E> {
  /**
   * Validate an options or arguments value after conversion. If valid, return
   * {@link ValidationResult#valid()} or else {@link ValidationResult#invalid(String)} with a
   * specified message.
   *
   * @param value the non-null converted value.
   * @return the validation result.
   */
  ValidationResult validate(@NotNull E value);
}
