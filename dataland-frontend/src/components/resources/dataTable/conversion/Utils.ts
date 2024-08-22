/**
 * Retrieves a deeply nested value from an object by an identifier.
 * @param identifier the path to the value to retrieve (dot-seperated)
 * @param frameworkDataset the data object of some framework to retrieve the value from
 * @returns the value at the path if it exists, undefined otherwise
 */
// This function is inherently not type-safe, but still required for the data-model conversion.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getFieldValueFromFrameworkDataset(identifier: string, frameworkDataset: any): any {
  const splits = identifier.split('.');
  // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-explicit-any
  let currentObject: any = frameworkDataset;
  for (const split of splits) {
    if (currentObject === undefined || currentObject === null) return currentObject;
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
    currentObject = currentObject[split];
  }
  return currentObject;
}

/**
 * Retrieves a deeply nested value from an object by an identifier.
 * @param identifier the path to the value to retrieve (dot-seperated)
 * @param dataModel the data object to retrieve the value from
 * @returns the value at the path if it exists, undefined otherwise
 */
// This function is inherently not type-safe, but still required for the data-model conversion.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getFieldValueFromDataModel(identifier: string, dataModel: any): any {
  const splits = identifier.split('.');
  // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-explicit-any
  let currentObject: any = dataModel;
  for (const split of splits) {
    if (currentObject === undefined || currentObject === null) return currentObject;
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
    currentObject = currentObject[split];
  }
  return currentObject;
}

/**
 * Maps the technical name of a select option to the respective original name
 * @param technicalName of a select option
 * @param mappingObject that contains the mappings
 * @returns original name that matches the technical name
 */
export function getOriginalNameFromTechnicalName<T extends string>(
  technicalName: T,
  mappingObject: { [key in T]: string }
): string {
  return mappingObject[technicalName];
}
