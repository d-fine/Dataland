/**
 * Check if string is valid url
 *
 * @param  str string to check if it is valid url
 * @returns boolean
 */
export function isValidHttpUrl(str: string): boolean {
  let url;
  try {
    url = new URL(str);
  } catch (err) {
    return false;
  }
  return url.protocol === "http:" || url.protocol === "https:";
}
