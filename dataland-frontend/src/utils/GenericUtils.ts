/**
 * Returns all keys from the inputMap ordered alphabetically
 * @param inputMap the amp to sort the keys from
 * @returns an array of the sorted keys
 */
export function getKeysFromMapAndReturnAsAlphabeticallySortedArray<T>(inputMap: Map<string, T>): Array<string> {
  return Array.from(inputMap.keys()).sort((reportingPeriodA, reportingPeriodB) => {
    if (reportingPeriodA > reportingPeriodB) return -1;
    else return 0;
  });
}
