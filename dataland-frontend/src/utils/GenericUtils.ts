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

export function calculateSha256HashFromFile(file: File): Promise<string>{
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = async () => {
      const buffer = reader.result as ArrayBuffer
      const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
      resolve(hashHex);
    };
    reader.onerror = () => {
      reject(reader.error);
    };
    reader.readAsArrayBuffer(file);
  });
}
