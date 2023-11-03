import { type Field } from "@/utils/GenericFrameworkTypes";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { type YesNoNa } from "@clients/backend";
import { getDataPointGetterFactory } from "@/components/resources/dataTable/conversion/Utils";
import { type GenericDataPoint } from "@/utils/DataPoint";
import { HumanizedYesNoNa } from "@/utils/YesNoNa";

const certificateHumanReadableYesNoMap: { [key in YesNoNa]: string } = {
  Yes: "Certified",
  No: "Uncertified",
  NA: HumanizedYesNoNa.NA,
};

/**
 * Returns a value factory that returns the value of the Yes / No Data Point form field
 * @param path the path to the field
 * @param field the field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function yesNoDataPointValueGetterFactory(
  path: string,
  field: Field,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return getDataPointGetterFactory<YesNoNa>(
    path,
    field,
    (dataPoint?: GenericDataPoint<YesNoNa>): string | undefined => {
      const lowerFieldLabel = field.label.toLowerCase();
      const isCertificationField = lowerFieldLabel.includes("certificate") || lowerFieldLabel.includes("certification");
      const humanReadableValue = isCertificationField
        ? certificateHumanReadableYesNoMap[dataPoint?.value ?? "NA"]
        : HumanizedYesNoNa[dataPoint?.value ?? "NA"];
      return dataPoint?.value ? humanReadableValue : "";
    },
  );
}
