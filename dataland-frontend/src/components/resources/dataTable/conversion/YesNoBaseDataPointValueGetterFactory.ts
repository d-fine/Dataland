import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type BaseDataPointYesNoNa, type BaseDataPointYesNo, YesNoNa } from "@clients/backend";
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
export function yesNoBaseDataPointValueGetterFactory(
  path: string,
  field: Field,
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const elementValue = getFieldValueFromFrameworkDataset(path, dataset) as BaseDataPointYesNo | BaseDataPointYesNoNa | undefined;
    if (!elementValue) { // TODO what to do if there is no value provided? still show meta info?
      return MLDTDisplayObjectForEmptyString;
    }
    const lowerFieldLabel = field.label.toLowerCase();
    const isCertificationField = lowerFieldLabel.includes("certificate") || lowerFieldLabel.includes("certification");

    // TODO handle element.value undefined!!!
    const displayValue = isCertificationField
      ? certificateHumanReadableYesNoMap[elementValue.value]
      : humanReadableYesNoMap[elementValue.value];

    if (elementValue.value == YesNoNa.Yes && elementValue.dataSource) {
      return {
        displayComponentName: MLDTDisplayComponentName.DocumentLinkDisplayComponent,
        displayValue: {
          label: displayValue,
          dataSource: elementValue.dataSource,
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
