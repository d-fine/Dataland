import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { type BaseDataPointYesNo, type BaseDataPointYesNoNa, type DataPointOneValueYesNo } from "@clients/backend";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";

// The effort of making this file type-safe greatly outweighs the benefit.
/* eslint @typescript-eslint/no-explicit-any: 0 */

/**
 * Formats the value of a YesNoFormField if the field serves as a certificate
 * @param elementValue the value of the field
 * @returns the formatted display value
 */
function formatYesNoValueWhenCertificateRequiredIsYes(
  elementValue: BaseDataPointYesNo | BaseDataPointYesNoNa | undefined,
): AvailableDisplayValues {
  if (!elementValue) {
    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: "",
    };
  }

  if (elementValue.dataSource) {
    const dataSource = elementValue.dataSource;
    return {
      displayComponent: MLDTDisplayComponents.DocumentLinkDisplayComponent,
      displayValue: {
        label: elementValue.value + " (Certified)",
        reference: dataSource,
      },
    };
  } else {
    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: elementValue.value,
    };
  }
}

/**
 * Formats the value of a YesNoFormField if evidence for the field value is required
 * @param elementValue the value of the field
 * @returns the formatted display value
 */
function formatYesNoValueWhenEvidenceDesiredIsYes(
  elementValue: DataPointOneValueYesNo | undefined,
): AvailableDisplayValues {
  if (!elementValue) {
    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: "",
    };
  }

  const yesNoValue = elementValue.value ?? "";
  return {
    displayComponent: MLDTDisplayComponents.StringDisplayComponent,
    displayValue: yesNoValue,
  };
}

/**
 * Returns a value factory that returns the value of the Yes / No form field
 * If the form field requires certification, a link to the certificate is returned if available.
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function yesNoValueGetterFactory(path: string, field: Field): (dataset: any) => AvailableDisplayValues {
  return (dataset) => {
    if (field.certificateRequiredIfYes) {
      return formatYesNoValueWhenCertificateRequiredIsYes(
        getFieldValueFromDataModel(path, dataset) as BaseDataPointYesNo | BaseDataPointYesNoNa | undefined,
      );
    } else if (field.evidenceDesired) {
      return formatYesNoValueWhenEvidenceDesiredIsYes(
        getFieldValueFromDataModel(path, dataset) as DataPointOneValueYesNo | undefined,
      );
    } else {
      return {
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: getFieldValueFromDataModel(path, dataset) as string | undefined,
      };
    }
  };
}
