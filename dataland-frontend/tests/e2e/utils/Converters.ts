/**
 * A partial query string encoder that only replaces spaces by + signs.
 * @param inputString the string to encode
 * @returns the encoded inputString
 */
export function convertStringToQueryParamFormat(inputString: string): string {
  const stringInQueryParamFormat = inputString.replace(" ", "+");
  return stringInQueryParamFormat;
}
