export type ObjectType = { [key: string]: string | object };

/**
 * Updates keys from one object to another
 * @param baseObject object to be updated
 * @param objectWithNewData object to be updated
 */
export function updateObject(baseObject: ObjectType, objectWithNewData: ObjectType): void {
  for (const key in objectWithNewData) {
    if (typeof objectWithNewData[key] === 'object' && objectWithNewData[key] !== null) {
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
    } else if (typeof value === 'object' && value != null) {
      return acc.concat(findAllValuesForKey(value as ObjectType, keyToFind));
    } else {
      return acc;
    }
  }, []);
}

type PickNullable<T> = {
  [P in keyof T as null extends T[P] ? P : never]: T[P];
};

type PickNotNullable<T> = {
  [P in keyof T as null extends T[P] ? never : P]: T[P];
};

// Adapted from: https://stackoverflow.com/a/72634592
type OptionalNullable<T> = T extends Array<unknown> | Set<unknown>
  ? T
  : T extends object
    ? {
        [K in keyof PickNullable<T>]?: OptionalNullable<Exclude<T[K], null>>;
      } & {
        [K in keyof PickNotNullable<T>]: OptionalNullable<T[K]>;
      }
    : T;

/**
 * Drops all nulls from arbitrarily nested object by stringifying it and parsing it back to object
 * It does not work in the case of the value being of type Array<null>!
 * @param obj the object that needs the nulls dropped
 * @returns the object without all keys with undefined or null value
 */
export function objectDropNull<T>(obj: T): OptionalNullable<T> {
  return JSON.parse(
    JSON.stringify(obj, (key, value: string | number) => {
      return value ?? undefined;
    })
  ) as OptionalNullable<T>;
}

/**
 * Utility function to create a deep copy by parsing it to Json and parsing it back.
 * @param obj the object to be deep copied
 * @returns the deep copied object
 */
export function deepCopyObject(obj: ObjectType): ObjectType {
  return JSON.parse(JSON.stringify(obj)) as ObjectType;
}
