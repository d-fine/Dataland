export function assertDefined<T>(input: T | undefined | null): T {
  if (input === undefined || input === null) {
    throw new Error("Assertion error: Input was supposed to be non-null but is");
  }
  return input;
}
