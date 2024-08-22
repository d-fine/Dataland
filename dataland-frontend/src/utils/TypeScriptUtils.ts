import {REGEX_FOR_FILE_NAMES} from "@/utils/Constants";

/**
 * A type operator that checks the crude equality of two objects
 * Does not work in every case.
 * (See https://github.com/microsoft/TypeScript/issues/27024#issuecomment-421529650)
 */
export type Equals<X, Y> = (<T>() => T extends X ? 1 : 2) extends <T>() => T extends Y ? 1 : 2 ? true : false;

/**
 * Utility function to assert that a condition cannot occur (i.e. for exhaustive switch statements)
 * Throws an error when called
 * @param input an impossible value
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
export function assertNever(input: never): never {
  throw new Error('This function should not be called as its input is never');
}

/**
 * Utility function to ensure that a given input is not null or undefined
 * @param input to be checked
 * @returns the input but checked that it is neither null nor undefined
 */
export function assertDefined<T>(input: T | undefined | null): T {
  if (input === undefined || input === null) {
    throw new Error('Assertion error: Input was supposed to be non-null but is.');
  } else {
    return input;
  }
}

/**
 * Verifies that the input is a string array in a typescript typesafe manner
 * @param input the input to check
 * @returns true iff the input is a string array
 */
export function isStringArray(input: unknown): input is string[] {
  return Array.isArray(input) && input.every((it) => typeof it === 'string');
}

/**
 * Verifies that the input is a string in a typescript typesafe manner
 * @param input the input to check
 * @returns true iff the input is a string
 */
export function isString(input: unknown): input is string {
  return typeof input === 'string';
}

/**
 * Checks if the pages given as a String conform to the set rules:
 * - all numbers > 0
 * - first number <= second number
 * @param pageString
 */
export function checkIfPageStringIsConform(pageString: string | null | undefined): boolean {
  const regexSingleNumber = new RegExp('[1-9]\\d*' )
  const regexTwoNumbers = new RegExp('[1-9]\\d*\\-[1-9]\\d*')
  if(pageString == null){
    return false
  }else if (pageString.includes('-') && regexTwoNumbers.test(pageString)) {
    const dashNumber = pageString.indexOf('-')
    console.log('dashNmber is: ' + dashNumber)
    const smallerNumber = Number(pageString.substring(0, dashNumber - 1))
    console.log('smallnum: ' + smallerNumber)
    const biggerNumber = Number(pageString.substring(dashNumber + 1))
    console.log('bignum: ' + biggerNumber)

    return smallerNumber < biggerNumber
  } else {
    return regexSingleNumber.test(pageString)
  }
}