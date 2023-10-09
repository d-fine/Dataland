import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { type BaseDataPointYesNoNa, type BaseDataPointYesNo, YesNoNa } from "@clients/backend";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";

const humanReadableYesNoMap: { [key in YesNoNa]: string } = {
  Yes: "Yes",
  No: "No",
  NA: "N/A",
};

/**
 * Formats the value of a YesNoFormField if the field serves as a certificate
 * @param elementValue the value of the field
 * @returns the formatted display value
 */
function formatYesNoValueWhenCertificateRequiredIsYes(
  elementValue: BaseDataPointYesNoNa | BaseDataPointYesNo | undefined,
): AvailableDisplayValues {
  if (!elementValue) {
    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: "",
    };
  }

  if (elementValue.value == YesNoNa.Yes) {
    if (elementValue.dataSource) {
      return {
        displayComponent: MLDTDisplayComponents.DocumentLinkDisplayComponent,
        displayValue: {
          label: "Certified",
          reference: elementValue.dataSource,
        },
      };
    } else {
      return {
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "Yes",
      };
    }
  } else if (elementValue.value == YesNoNa.No) {
    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: "Uncertified",
    };
  } else if (elementValue.value == YesNoNa.Na) {
    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: "N/A",
    };
  }
  return {
    displayComponent: MLDTDisplayComponents.StringDisplayComponent,
    displayValue: "",
  };
}

/**
 * Formats the value of a YesNoFormField if evidence for the field value is required
 * @param elementValue the value of the field
 * @returns the formatted display value
 */
function formatYesNoValueWhenEvidenceDesiredIsYes(
  elementValue: BaseDataPointYesNoNa | undefined,
): AvailableDisplayValues {
  if (!elementValue?.value) {
    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: "",
    };
  }

  const yesNoValue = elementValue.value;
  return {
    displayComponent: MLDTDisplayComponents.StringDisplayComponent,
    displayValue: humanReadableYesNoMap[yesNoValue],
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
        getFieldValueFromDataModel(path, dataset) as BaseDataPointYesNoNa | undefined,
      );
    } else {
      const value = getFieldValueFromDataModel(path, dataset) as YesNoNa | undefined;
      const displayValue = value ? humanReadableYesNoMap[value] : "";
      return {
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: displayValue,
      };
    }
  };
}
