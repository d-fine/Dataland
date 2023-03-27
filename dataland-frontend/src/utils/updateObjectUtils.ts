type objectType = { [key: string]: string | object };
/**
 * Updates keys from one object to another
 *
 * @param baseObject object to be updated
 * @param objectWithNewData object to be updated
 */
export function updateObject(baseObject: objectType, objectWithNewData: objectType): void {
  for (const key in objectWithNewData) {
    if (typeof objectWithNewData[key] === "object" && objectWithNewData[key] !== null) {
      if (baseObject[key]) {
        updateObject(baseObject[key] as unknown as objectType, objectWithNewData[key] as unknown as objectType);
      } else {
        baseObject[key] = {};
        updateObject(baseObject[key] as unknown as objectType, objectWithNewData[key] as unknown as objectType);
      }
    } else {
      if (
        Object.prototype.hasOwnProperty.call(baseObject, key) ||
        baseObject[key] === null ||
        baseObject[key] === undefined
      ) {
        baseObject[key] = objectWithNewData[key];
      }
    }
  }
}
