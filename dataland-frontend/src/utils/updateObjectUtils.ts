export type objectType = { [key: string]: string | object };

/**
 * Updates keys from one object to another
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
    } else if (objectWithNewData[key] !== null) {
      baseObject[key] = objectWithNewData[key];
    }
  }
}

/**
 * Changes the value of a variable by dividing it by 100
 * @param obj object in which it is looking for the value to change
 * @param typeOfModification determines how we change object values
 * @returns Object modified
 */
export function modifyObjectKeys(obj: objectType, typeOfModification: string): objectType {
  const objectModified = obj;
  for (const key in objectModified) {
    if (key === "value" && objectModified[key]) {
      if (typeOfModification === "send") {
        objectModified[key] = (Math.round(+objectModified[key] * 100) / 100 / 100).toString();
      }
      if (typeOfModification === "receive") {
        objectModified[key] = (Math.round(+objectModified[key] * 100 * 100) / 100).toString();
      }
    } else if (typeof objectModified[key] === "object" && key !== "totalAmount") {
      modifyObjectKeys(objectModified[key] as unknown as objectType, typeOfModification);
    }
  }
  return objectModified;
}

/**
 * Drops all nulls from arbitrarily nested object by stringifying it and parsing it back to object
 * It does not work in the case of the value being of type Array<null>!
 * @param obj the object that needs the nulls dropped
 * @returns the object without all keys with undefined or null value
 */
export function objectDropNull(obj: objectType): objectType {
  return JSON.parse(
    JSON.stringify(obj, (key, value: string | number) => {
      return value ?? undefined;
    })
  ) as objectType;
}
