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

/**
 * Changes the value of a variable by dividing it by 100
 *
 * @param obj object in which it is looking for the value to change
 * @returns Object modified
 */
export function modifyObjectKeys(obj: objectType): objectType {
  const objectModified = obj;
  for (const key in objectModified) {
    if (key === "value" && objectModified[key]) {
      objectModified[key] = (parseInt(objectModified[key] as string) / 100).toFixed(2).toString();
    } else if (typeof objectModified[key] === "object" && objectModified.constructor.name !== "totalAmount") {
      modifyObjectKeys(objectModified[key] as unknown as objectType);
    }
  }
  return objectModified;
}
