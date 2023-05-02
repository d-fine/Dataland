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
 *  calculates the hash from a file
 *
 * @param [file] the file to calculate the hash from
 * @returns a promise of the hash as string
 */
export async function calculateSha256HashFromFile(file: File): Promise<string> {
  const buffer = await file.arrayBuffer();
  const hashBuffer = await crypto.subtle.digest("SHA-256", buffer);
  return toHex(hashBuffer);
}

/**
 *  helper to encode a hash of type buffer in hex
 *
 * @param [buffer] the buffer to encode in hex
 * @returns  the array as string, hex encoded
 */
function toHex(buffer: ArrayBuffer): string {
  const array = Array.from(new Uint8Array(buffer)); // convert buffer to byte array
  return array.map((b) => b.toString(16).padStart(2, "0")).join(""); // convert bytes to hex string
}
