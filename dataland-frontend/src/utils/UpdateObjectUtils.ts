export type ObjectType = { [key: string]: string | object };
/**
 * Updates keys from one object to another
 *
 * @param baseObject object to be updated
 * @param objectWithNewData object to be updated
 */
export function updateObject(baseObject: ObjectType, objectWithNewData: ObjectType): void {
  for (const key in objectWithNewData) {
    if (typeof objectWithNewData[key] === "object" && objectWithNewData[key] !== null) {
      if (baseObject[key]) {
        updateObject(baseObject[key] as unknown as ObjectType, objectWithNewData[key] as unknown as ObjectType);
      } else {
        baseObject[key] = {};
        updateObject(baseObject[key] as unknown as ObjectType, objectWithNewData[key] as unknown as ObjectType);
      }
    } else if (objectWithNewData[key] !== null) {
      baseObject[key] = objectWithNewData[key];
    }
  }
}

/**
 * Changes the value of a variable to maintain different number formatting between backend and frontend
 *
 * @param obj object in which it is looking for the value to change
 * @param typeOfModification determines how we change object values
 * @returns Object modified
 */
export function modifyObjectKeys(obj: ObjectType, typeOfModification: "send" | "receive"): ObjectType {
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
      modifyObjectKeys(objectModified[key] as unknown as ObjectType, typeOfModification);
    }
  }
  return objectModified;
}
