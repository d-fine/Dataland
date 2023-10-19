import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type CurrencyDataPoint, type ExtendedDataPointBigDecimal } from "@clients/backend";
import {
  getFieldValueFromFrameworkDataset,
  getGloballyReferencableDocuments,
  hasDataPointValidReference,
} from "@/components/resources/dataTable/conversion/Utils";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { formatAmountWithCurrency, formatNumberToReadableFormat } from "@/utils/Formatter";

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
    const datapoint = getFieldValueFromFrameworkDataset(path, dataset) as ExtendedDataPointBigDecimal;

    if (datapoint?.value == null) {
      return MLDTDisplayObjectForEmptyString;
    }

    let datapointValue = formatNumberToReadableFormat(datapoint.value);
    let datapointUnitSuffix: string;

    if ((datapoint as CurrencyDataPoint)?.currency && (datapoint as CurrencyDataPoint)?.currency?.length) {
      datapointUnitSuffix = (datapoint as CurrencyDataPoint)?.currency ?? "";
      datapointValue = formatAmountWithCurrency({ amount: datapoint.value });
    } else {
      datapointUnitSuffix = field.unit ?? "";
    }

    const formattedValue: string = datapointValue ? `${datapointValue} ${datapointUnitSuffix}`.trim() : "";
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
