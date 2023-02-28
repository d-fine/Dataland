// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type Procedure = (...args: any[]) => void;

/**
 * Source: https://github.com/zxcvbn-ts/zxcvbn/blob/5c5a74ab2b77c58bfd1c3821d813fd59474e08e7/packages/libraries/main/src/debounce.ts
 *
 * @param func needs to implement a function which is debounced
 * @param wait how long do you want to wait till the previous declared function is executed
 * @param isImmediate defines if you want to execute the function on the first execution or the last execution inside the time window. `true` for first and `false` for last.
 * @returns a debounced version of the input function
 */
export default <F extends Procedure>(
  func: F,
  wait: number,
  isImmediate?: boolean
): ((this: ThisParameterType<F>, ...args: Parameters<F>) => void) => {
  let timeout: ReturnType<typeof setTimeout> | undefined;
  return function debounce(this: ThisParameterType<F>, ...args: Parameters<F>) {
    // eslint-disable-next-line @typescript-eslint/no-this-alias
    const context = this;
    const later = (): void => {
      timeout = undefined;
      if (!isImmediate) {
        func.apply(context, args);
      }
    };
    const shouldCallNow = isImmediate && !timeout;
    if (timeout !== undefined) {
      clearTimeout(timeout);
    }
    timeout = setTimeout(later, wait);
    if (shouldCallNow) {
      return func.apply(context, args);
    }
    return undefined;
  };
};
