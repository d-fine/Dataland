/**
 * Returns all keys from the inputMap ordered alphabetically
 *
 * @param inputMap the amp to sort the keys from
 * @returns an array of the sorted keys
 */
export function getKeysFromMapAndReturnAsAlphabeticallySortedArray<T>(inputMap: Map<string, T>): Array<string> {
  return Array.from(inputMap.keys()).sort((reportingPeriodA, reportingPeriodB) => {
    if (reportingPeriodA > reportingPeriodB) return -1;
    else return 0;
  });
}

/**
 * Returns all keys from the inputMap ordered alphabetically
 *
 * @param baseObject object to be updated
 * @param objectWithNewData object to be updated
 */
export function updateObject(
  baseObject: { [key: string]: string },
  objectWithNewData: { [key: string]: string }
): void {
  for (const key in objectWithNewData) {
    if (typeof objectWithNewData[key] === "object") {
      updateObject(
        baseObject[key] as unknown as { [key: string]: string },
        objectWithNewData[key] as unknown as { [key: string]: string }
      );
    } else {
      if (Object.prototype.hasOwnProperty.call(baseObject, key)) {
        baseObject[key] = objectWithNewData[key];
      }
    }
  }
}
