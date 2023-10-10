import {
  type AvailableDisplayValues,
  EmptyDisplayValue,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { type ExtendedDataPointBigDecimal } from "@clients/backend";
import {
  getFieldValueFromDataModel,
  getGloballyReferencableDocuments,
  hasDataPointValidReference,
} from "@/components/resources/dataTable/conversion/Utils";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { formatNumberToReadableFormat } from "@/utils/Formatter";

/**
 * Returns a value factory that returns the value of the DataPointFormField
 * @param path the path to the field
 * @param field the field from the data model
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function dataPointValueGetterFactory(path: string, field: Field): (dataset: any) => AvailableDisplayValues {
  return (dataset) => {
    const datapoint = getFieldValueFromDataModel(path, dataset) as ExtendedDataPointBigDecimal | undefined;

    if (!datapoint?.value) {
      return EmptyDisplayValue;
    }

    const datapointValue = formatNumberToReadableFormat(datapoint.value);
    let datapointUnitSuffix;

    if (field.options) {
      const datapointUnitRaw = field.unit ?? "";
      const matchingEntry = field.options.find((it) => it.value == datapointUnitRaw);
      datapointUnitSuffix = matchingEntry?.label ?? datapointUnitRaw;
    } else {
      datapointUnitSuffix = field.unit ?? "";
    }

    const formattedValue = `${datapointValue} ${datapointUnitSuffix}`.trim();
    if (hasDataPointValidReference(datapoint)) {
      const documentReference = getGloballyReferencableDocuments(dataset).find(
        (document) => document.fileName == datapoint.dataSource?.fileReference,
      );
      if (documentReference == undefined) {
        throw Error(
          `There is no document with name ${
            datapoint.dataSource?.fileReference ?? "NOT PROVIDED"
          } referenced in this dataset`,
        );
      }
      return {
        displayComponent: MLDTDisplayComponents.DataPointDisplayComponent,
        displayValue: {
          label: formattedValue,
          reference: documentReference,
          page: datapoint.dataSource?.page ?? undefined,
        },
      };
    } else {
      return {
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: formattedValue,
      };
    }
  };
}
