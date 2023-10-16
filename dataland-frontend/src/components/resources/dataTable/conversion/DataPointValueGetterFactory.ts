import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";
import { type ExtendedDataPointBigDecimal } from "@clients/backend";
import {
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
    const datapoint = getFieldValueFromFrameworkDataset(path, dataset) as ExtendedDataPointBigDecimal;

    if (!datapoint?.value) {
      return MLDTDisplayObjectForEmptyString;
    }

    const datapointValue = formatNumberToReadableFormat(datapoint.value);
    let datapointUnitSuffix: string;

    if (field.options) {
      const datapointUnitRaw = field.unit ?? "";
      const matchingEntry = field.options.find((it) => it.value == datapointUnitRaw);
      datapointUnitSuffix = matchingEntry?.label ?? datapointUnitRaw;
    } else {
      datapointUnitSuffix = field.unit ?? "";
    }

    return {
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: `${datapointValue} ${datapointUnitSuffix}`.trim(),
    };

    const formattedValue: string = `${datapointValue} ${datapointUnitSuffix}`.trim();
    if (hasDataPointValidReference(datapoint)) {
      const documentReference = getGloballyReferencableDocuments(dataset).find(
        (document) => document.fileName == datapoint?.dataSource?.fileReference,
      );
      if (documentReference == undefined) {
        throw Error(
          `There is no document with name ${
            datapoint?.dataSource?.fileReference ?? "NOT PROVIDED"
          } referenced in this dataset`,
        );
      }
      return {
        displayComponentName: MLDTDisplayComponentName.DataPointDisplayComponentName,
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
