import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type ExtendedDataPointYesNo, type YesNoNa } from "@clients/backend";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";

const humanReadableYesNoMap: { [key in YesNoNa]: string } = {
  Yes: "Yes",
  No: "No",
  NA: "N/A",
};

/**
 * Formats the value of a YesNoFormField if evidence for the field value is required
 * @param elementValue the value of the field
 * @returns the formatted display value
 */
function formatYesNoValueWhenEvidenceDesiredIsYes(
  elementValue: ExtendedDataPointYesNo | undefined,
): AvailableMLDTDisplayObjectTypes {
  if (!elementValue?.value) {
    return MLDTDisplayObjectForEmptyString;
  }

  const yesNoValue = elementValue.value;
  return {
    displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
    displayValue: humanReadableYesNoMap[yesNoValue],
  };
}

/**
 * Returns a value factory that returns the value of the Extended Data Point Yes / No form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function yesNoExtendedDataPointValueGetterFactory(
  path: string,
  field: Field,
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    return formatYesNoValueWhenEvidenceDesiredIsYes(
      getFieldValueFromFrameworkDataset(path, dataset) as ExtendedDataPointYesNo | undefined,
    );
  };
}
