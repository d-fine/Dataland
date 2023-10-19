import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { CurrencyDataPoint, type ExtendedDataPointBigDecimal } from "@clients/backend";
import {
  getFieldValueFromFrameworkDataset,
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
export function dataPointValueGetterFactory(
  path: string,
  field: Field,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const datapoint = getFieldValueFromFrameworkDataset(path, dataset) as ExtendedDataPointBigDecimal | undefined;

    if (!datapoint?.value) { // TODO then QA sees nothing
      return MLDTDisplayObjectForEmptyString;
    }

    const datapointValue = formatNumberToReadableFormat(datapoint.value);
    let datapointUnitSuffix: string;

    if((datapoint as CurrencyDataPoint)?.currency && (datapoint as CurrencyDataPoint)?.currency?.length > 0) {
      datapointUnitSuffix = (datapoint as CurrencyDataPoint)?.currency!;
    } else if (field.options?.length) {
      // TODO why does the following check for field unit as well as options? This should never happen, right?
      const datapointUnitRaw = field.unit ?? "";
      const matchingEntry = field.options.find((it) => it.value == datapointUnitRaw);
      datapointUnitSuffix = matchingEntry?.label ?? datapointUnitRaw;
    } else {
      datapointUnitSuffix = field.unit ?? "";
    }

    const formattedValue: string = datapointValue ? `${datapointValue} ${datapointUnitSuffix}`.trim() : ""; // TODO Data hidden from QA
    if (hasDataPointValidReference(datapoint)) {
      const documentName = getGloballyReferencableDocuments(dataset).find(
        (document) => document.fileName == datapoint?.dataSource?.fileName,
      );
      if (documentName == undefined) {
        throw Error(
          `There is no document with name ${
            datapoint?.dataSource?.fileName ?? "NOT PROVIDED"
          } referenced in this dataset`,
        );
      }
      return {
        displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponent,
        displayValue: {
          label: formattedValue,
          fileReference: datapoint?.dataSource?.fileReference as string,
          fileName: datapoint?.dataSource?.fileName as string,
          page: datapoint?.dataSource?.page ?? undefined,
          quality: datapoint?.quality,
          comment: datapoint?.comment,
        },
      };
    } else {
      return {
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: formattedValue,
      };
    }
  };
}
