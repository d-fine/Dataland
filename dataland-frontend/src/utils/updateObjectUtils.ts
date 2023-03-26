/**
 * Updates keys from one object to another
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
