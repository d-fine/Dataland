export function getFieldValueFromDataModel(identifier: string, dataModel: any): any {
  const splits = identifier.split(".");
  let currentObject: any = dataModel;
  for (const split of splits) {
    if (currentObject == undefined || currentObject == null) return currentObject;
    currentObject = currentObject[split];
  }
  return currentObject;
}
