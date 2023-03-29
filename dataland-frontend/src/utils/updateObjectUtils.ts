export type objectType = { [key: string]: string | object };
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
    } else if (objectWithNewData[key] !== null) {
      baseObject[key] = objectWithNewData[key];
    }
  }
}

/**
 * Changes the value of a variable by dividing it by 100
 *
 * @param obj object in which it is looking for the value to change
 * @param typeOfModification determines how we change object values
 * @returns Object modified
 */
export function modifyObjectKeys(obj: objectType, typeOfModification: string): objectType {
  const objectModified = obj;
  for (const key in objectModified) {
    if (typeOfModification === "send") {
      if (key === "value" && objectModified[key]) {
        objectModified[key] = (Math.round(+objectModified[key] * 100) / 100 / 100).toString();
      } else if (key === "dataSource" && Object.keys(key).length === 0) {
        delete objectModified[key];
      } else if (typeof objectModified[key] === "object" && key !== "totalAmount") {
        modifyObjectKeys(objectModified[key] as unknown as objectType, typeOfModification);
      }
    } else if (typeOfModification === "receive") {
      if (key === "value" && objectModified[key]) {
        objectModified[key] = (Math.round(+objectModified[key] * 100 * 100) / 100).toString();
      } else if (key === "dataSource" && objectModified[key] == null) {
        objectModified[key] = {};
      } else if (typeof objectModified[key] === "object" && key !== "totalAmount") {
        modifyObjectKeys(objectModified[key] as unknown as objectType, typeOfModification);
      }
    }
  }
  return objectModified;
}
