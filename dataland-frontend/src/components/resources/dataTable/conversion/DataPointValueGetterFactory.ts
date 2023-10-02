import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";
import {type DataPointWithUnitBigDecimal, SfdrData} from "@clients/backend";
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
    const datapoint = getFieldValueFromDataModel(path, dataset) as DataPointWithUnitBigDecimal | undefined;

    if (!datapoint?.value) {
      return {
        displayComponent: MLDTDisplayComponents.StringDisplayComponent,
        displayValue: "",
      };
    }

    // TODO why assume this is a number?
    const datapointValue = formatNumberToReadableFormat(datapoint.value);
    let datapointUnitSuffix;

    if (field.options) {
      const datapointUnitRaw = datapoint.unit ?? "";
      const matchingEntry = field.options.find((it) => it.value == datapointUnitRaw);
      datapointUnitSuffix = matchingEntry?.label ?? datapointUnitRaw;
    } else {
      datapointUnitSuffix = field.unit ?? "";
    }

    const formattedValue = `${datapointValue} ${datapointUnitSuffix}`.trim()
    if (datapoint.dataSource?.report) {
      const referencableDocuments = Object.entries((dataset as SfdrData)?.general?.general?.referencedReports ?? {});
      const documentReference: string = referencableDocuments.find((document) => document[0] == datapoint.dataSource?.report)[1].reference;
      return {
        displayComponent: MLDTDisplayComponents.DataPointDisplayComponent,
        displayValue: {
          label: formattedValue,
          reference: {
            name: datapoint.dataSource?.report,
            reference: documentReference
          },
          page: datapoint.dataSource?.page ?? undefined
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
