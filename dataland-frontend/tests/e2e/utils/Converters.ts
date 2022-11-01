export function convertStringToQueryParamFormat(inputString: string): string {
  const stringInQueryParamFormat = inputString.replace(" ", "+");
  return stringInQueryParamFormat;
}
