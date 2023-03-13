export function getKeysFromMapAndReturnAsAlphabeticallySortedArray(inputMap: Map<string, any>): Array<string> {
  return Array.from(inputMap.keys()).sort((reportingPeriodA, reportingPeriodB) => {
    if (reportingPeriodA > reportingPeriodB) return -1;
    else return 0;
  });
}
