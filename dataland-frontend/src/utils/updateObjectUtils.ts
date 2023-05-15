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
 * Changes the "value" named properties of an object from decimals to percentages
 * to maintain different number formatting between backend and frontend
 * @param obj object in which it is looking for the "value" properties to change
 * @returns the modified object
 */
export function convertValuesFromDecimalsToPercentages(obj: ObjectType): ObjectType {
  const objectModified = obj;
  for (const key in objectModified) {
    if (key === "value" && objectModified[key]) {
      objectModified[key] = (Math.round(+objectModified[key] * 100 * 100) / 100).toString();
    } else if (typeof objectModified[key] === "object" && key !== "totalAmount") {
      convertValuesFromDecimalsToPercentages(objectModified[key] as unknown as ObjectType);
    }
  }
  return objectModified;
}

/**
 * Changes the "value" named properties of an object from percentages to decimals
 * to maintain different number formatting between backend and frontend
 * @param obj object in which it is looking for the "value" properties to change
 * @returns the modified object
 */
export function convertValuesFromPercentagesToDecimals(obj: ObjectType): ObjectType {
  const objectModified = obj;
  for (const key in objectModified) {
    if (key === "value" && objectModified[key]) {
      objectModified[key] = (Math.round(+objectModified[key] * 100) / 100 / 100).toString();
    } else if (typeof objectModified[key] === "object" && key !== "totalAmount") {
      convertValuesFromPercentagesToDecimals(objectModified[key] as unknown as ObjectType);
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

/**
 * Drops all nulls from arbitrarily nested object by stringifying it and parsing it back to object
 * It does not work in the case of the value being of type Array<null>!
 * @param obj the object that needs the nulls dropped
 * @returns the object without all keys with undefined or null value
 */
export function objectDropNull(obj: ObjectType): ObjectType {
  return JSON.parse(
    JSON.stringify(obj, (key, value: string | number) => {
      return value ?? undefined;
    })
  ) as ObjectType;
}
