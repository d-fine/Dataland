import { type Field } from "@/utils/GenericFrameworkTypes";
import {
  type AvailableDisplayValues,
  EmptyDisplayValue,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import type { BaseDocumentReference } from "@clients/backend";
import {
  type BaseDataPointYesNoNa,
  type BaseDataPointYesNo,
  YesNoNa,
} from "@clients/backend";
import {
  getFieldValueFromDataModel,
  getGloballyReferencableDocuments,
  hasDataPointValidReference,
} from "@/components/resources/dataTable/conversion/Utils";

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
 * Formats the value of a YesNoFormField if the field serves as a certificate
 * @param elementValue the value of the field
 * @param field the YesNoFormField
 * @returns the formatted display value
 */
function formatYesNoValueWhenCertificateRequiredIsYes(
  elementValue: BaseDataPointYesNoNa | BaseDataPointYesNo | undefined,
  field: Field,
): AvailableDisplayValues {
  if (!elementValue) {
    return EmptyDisplayValue;
  }
  const lowerFieldLabel = field.label.toLowerCase();
  const isCertificationField = lowerFieldLabel.includes("certificate") || lowerFieldLabel.includes("certification");

  const displayValue = isCertificationField
    ? certificateHumanReadableYesNoMap[elementValue.value]
    : humanReadableYesNoMap[elementValue.value];

  if (elementValue.value == YesNoNa.Yes && elementValue.dataSource) {
    return {
      displayComponent: MLDTDisplayComponents.DocumentLinkDisplayComponent,
      displayValue: {
        label: displayValue,
        reference: elementValue.dataSource,
      },
    };
  } else {
    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: displayValue,
    };
  }
}

/**
 * Formats the value of a YesNoFormField if evidence for the field value is required
 * @param elementValue the value of the field
 * @param dataset the to be displayed
 * @returns the formatted display value
 */
function formatYesNoValueWhenEvidenceDesiredIsYes(
  elementValue: BaseDataPointYesNoNa | undefined,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  dataset: any,
): AvailableDisplayValues {
  if (!elementValue?.value) {
    return EmptyDisplayValue;
  }

  const yesNoValue = elementValue.value;
  if (hasDataPointValidReference(elementValue)) {
    const documentReference = getGloballyReferencableDocuments(dataset).find(
      (document: BaseDocumentReference) => document.fileName == elementValue.dataSource?.fileName,
    );
    if (documentReference == undefined) {
      throw Error(
        `There is no document with name ${
          elementValue.dataSource?.fileName ?? "NOT PROVIDED"
        } referenced in this dataset`,
      );
    }
    return {
      displayComponent: MLDTDisplayComponents.DataPointDisplayComponent,
      displayValue: {
        label: humanReadableYesNoMap[yesNoValue],
        reference: documentReference,
        //TODO Check if the datasource here should be of the kind BaseDocumentReference, ExtendedDocumentReference or CompanyReport
        //page: elementValue.dataSource?.page ?? undefined,
      },
    };
  } else {
    return {
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: humanReadableYesNoMap[yesNoValue],
    };
  }
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
        field,
      );
    } else if (field.evidenceDesired) {
      return formatYesNoValueWhenEvidenceDesiredIsYes(
        getFieldValueFromDataModel(path, dataset) as BaseDataPointYesNoNa | undefined,
        dataset,
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
