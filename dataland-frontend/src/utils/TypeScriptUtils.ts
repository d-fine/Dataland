/**
 * Utility function to ensure that a given input is not null or undefined
 * @param input to be checked
 */
export function assertDefined<T>(input: T | undefined | null): T {
  if (input === undefined || input === null) {
    throw new Error("Assertion error: Input was supposed to be non-null but is.");
  } else {
    return input;
  }
}
