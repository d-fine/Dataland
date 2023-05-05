export type ObjectType = { [key: string]: string | object };

/**
 * Updates keys from one object to another
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

/**
 * A recursive function that is able to retrieve all values for a provided key in an arbitrarily deeply nested object.
 * Example: the datasource.reference in the EUTaxonomyDataModel occurs for several data points and you might want to get
 * a list of all references in one dataset.
 * @param [obj] object in which it is looking for the value to change
 * @param [keyToFind] the key which is to be found
 * @returns all the values corresponding to the key
 */
export function findAllValuesForKey(obj: ObjectType, keyToFind: string): Array<string> {
  return Object.entries(obj).reduce((acc: Array<string>, [key, value]) => {
    if (key === keyToFind) {
      return acc.concat(value as string);
    } else if (typeof value === "object" && value != null) {
      return acc.concat(findAllValuesForKey(value as ObjectType, keyToFind));
    } else {
      return acc;
    }
  }, []);
}
