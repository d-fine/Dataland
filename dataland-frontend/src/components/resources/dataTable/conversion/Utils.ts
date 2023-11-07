import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { type BaseDataPoint, type ExtendedDataPoint } from "@/utils/DataPoint";

/**
 * Retrieves a deeply nested value from an object by an identifier.
 * @param identifier the path to the value to retrieve (dot-seperated)
 * @param frameworkDataset the data object of some framework to retrieve the value from
 * @returns the value at the path if it exists, undefined otherwise
 */
// This function is inherently not type-safe, but still required for the data-model conversion.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getFieldValueFromFrameworkDataset(identifier: string, frameworkDataset: any): any {
  const splits = identifier.split(".");
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
 * Checks if a given data point has a valid reference set
 * @param dataPoint the datapoint whose reference to check
 * @param dataPoint.dataSource the data source of the data point
 * @returns true if the reference is properly set
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function hasDataPointValidReference(dataPoint: ExtendedDataPoint<any> | BaseDataPoint<any>): boolean {
  return !!dataPoint?.dataSource?.fileReference?.trim().length;
}

/**
 * Returns the document references globally available in a dataset
 * @param path the path to the field value
 * @param field the field
 * @param formatter a function to transform the datapoint value ot a display value
 * @returns the data point getter factory
 */
export function getDataPointGetterFactory<
  V,
  D extends BaseDataPoint<V | null | undefined> | ExtendedDataPoint<V> = ExtendedDataPoint<V>,
>(
  path: string,
  field: Field,
  formatter: (dataPoint?: D) => string | undefined,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const dataPoint = getFieldValueFromFrameworkDataset(path, dataset) as D | undefined;
    if (!dataPoint) {
      return MLDTDisplayObjectForEmptyString;
    }
    const formattedValue = formatter(dataPoint);
    let displayValue: string;
    if (formattedValue == undefined || formattedValue == "") {
      displayValue = "No data provided";
    } else {
      displayValue = formattedValue;
    }
    const dataPointAsExtendedDataPoint = dataPoint as unknown as ExtendedDataPoint<V>;
    if (
      dataPointAsExtendedDataPoint.quality ||
      dataPointAsExtendedDataPoint.comment?.length ||
      dataPointAsExtendedDataPoint.dataSource?.page != null
    ) {
      return {
        displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
        displayValue: {
          fieldLabel: field.label,
          value: displayValue,
          dataSource: dataPointAsExtendedDataPoint.dataSource,
          quality: dataPointAsExtendedDataPoint.quality,
          comment: dataPointAsExtendedDataPoint.comment,
        },
      } as AvailableMLDTDisplayObjectTypes;
    } else if (hasDataPointValidReference(dataPoint)) {
      return {
        displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
        displayValue: {
          label: displayValue,
          dataSource: dataPoint.dataSource,
        },
      } as AvailableMLDTDisplayObjectTypes;
    } else {
      return {
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: displayValue,
      } as AvailableMLDTDisplayObjectTypes;
    }
  };
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
  const splits = identifier.split(".");
  // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-explicit-any
  let currentObject: any = dataModel;
  for (const split of splits) {
    if (currentObject === undefined || currentObject === null) return currentObject;
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
    currentObject = currentObject[split];
  }
  return currentObject;
}
