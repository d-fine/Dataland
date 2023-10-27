import { type Field } from "@/utils/GenericFrameworkTypes";
import {
    type AvailableMLDTDisplayObjectTypes,
    MLDTDisplayComponentName,
    type MLDTDisplayObject,
    MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type CurrencyDataPoint } from "@clients/backend";
import {
    getFieldValueFromFrameworkDataset,
    getGloballyReferencableDocuments,
    hasDataPointValidReference,
} from "@/components/resources/dataTable/conversion/Utils";
import { formatAmountWithCurrency } from "@/utils/Formatter";

/**
 * Returns a value factory that returns the value of the Yes / No form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function currencyDataPointValueGetterFactory(path: string, field: Field): (dataset: any) => AvailableMLDTDisplayObjectTypes {
    return (dataset) => {
        const datapoint = getFieldValueFromFrameworkDataset(path, dataset) as CurrencyDataPoint | undefined;
        if (datapoint?.value == null) {
            return MLDTDisplayObjectForEmptyString;
        }
        const datapointValue = formatAmountWithCurrency({ amount: datapoint.value });
        let datapointUnitSuffix: string;
        if (datapoint.currency && datapoint.currency?.length) {
            datapointUnitSuffix = datapoint?.currency ?? "";
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
                    fieldLabel: field.label,
                    value: formattedValue,
                    dataSource: datapoint?.dataSource,
                    quality: datapoint?.quality,
                    comment: datapoint?.comment,
                },
            } as MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent>;
        } else {
            return {
                displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
                displayValue: formattedValue,
            } as MLDTDisplayObject<MLDTDisplayComponentName.StringDisplayComponent>;
        }
    };
}
