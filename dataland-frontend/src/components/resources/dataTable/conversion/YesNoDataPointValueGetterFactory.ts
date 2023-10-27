import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import {
  type BaseDataPointYesNoNa,
  type BaseDataPointYesNo,
  type YesNoNa,
  type ExtendedDataPointYesNo,
} from "@clients/backend";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";

const humanReadableYesNoMap: { [key in YesNoNa]: string } = {
  Yes: "Yes",
  No: "No",
  NA: "N/A",
};

const certificateHumanReadableYesNoMap: { [key in YesNoNa]: string } = {
  Yes: "Certified",
  No: "Uncertified",
  NA: "N/A",
};

/**
 * Returns a value factory that returns the value of the Base Data Point Yes / No form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function yesNoDataPointValueGetterFactory(
  path: string,
  field: Field,
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const elementValue = getFieldValueFromFrameworkDataset(path, dataset) as
      | BaseDataPointYesNo
      | BaseDataPointYesNoNa
      | ExtendedDataPointYesNo
      | undefined;
    if (!elementValue) {
      return MLDTDisplayObjectForEmptyString;
    }
    const lowerFieldLabel = field.label.toLowerCase();
    const isCertificationField = lowerFieldLabel.includes("certificate") || lowerFieldLabel.includes("certification");

    // TODO handle element.value undefined!!! ASK IN DAILY
    const displayValue = elementValue.value
      ? isCertificationField
        ? certificateHumanReadableYesNoMap[elementValue.value]
        : humanReadableYesNoMap[elementValue.value]
      : "";

    const extendedDataPoint = elementValue as ExtendedDataPointYesNo;
    if (elementValue.dataSource || extendedDataPoint.quality || extendedDataPoint.comment?.length) {
      return {
        displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent, // TODO should this be handled as a document reference?? ASKI IN DAILY
        displayValue: {
          fieldLabel: field.label,
          value: displayValue,
          dataSource: elementValue.dataSource, // TODO change the dataSource of displayValue to support base datapoint data source
          quality: extendedDataPoint.quality,
          comment: extendedDataPoint.comment,
        },
      };
    } else {
      return {
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: displayValue,
      };
    }
  };
}
