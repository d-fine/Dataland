/**
 * A type operator that checks the crude equality of two objects
 * Does not work in every case.
 */
export type Equals<X, Y> = (<T>() => T extends X ? 1 : 2) extends <T>() => T extends Y ? 1 : 2 ? true : false;

/**
 * Utility function to assert that a condition cannot occur (i.e. for exhaustive switch statements)
 * Throws an error when called
 * @param input an impossible value
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
export function assertNever(input: never): never {
  throw new Error("This function should not be called as its input is never");
}

/**
 * Utility function to ensure that a given input is not null or undefined
 * @param input to be checked
 * @returns the input but checked that it is neither null nor undefined
 */
export function assertDefined<T>(input: T | undefined | null): T {
  if (input === undefined || input === null) {
    throw new Error("Assertion error: Input was supposed to be non-null but is.");
  } else {
    return input;
  }
}
