import { type AvailableDisplayValues } from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";
import { type Field } from "@/utils/GenericFrameworkTypes";

/**
 * Returns a value factory that returns the value of the field as a string using the display mapping in the options field
 * If the value is non-truthy, an empty string is returned
 * @param path the path to the field
 * @param field the single select field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function dataTableModalGetterFactory(path: string, field: Field): (dataset: any) => AvailableDisplayValues {
  return (dataset) => {
    const pathWithoutField = `${path
      .split(".")
      .filter((item, index, array) => index < array.length - 1)
      .join(".")}`;
    const highImpactClimateSectors = ["A", "B", "C", "D", "E", "F", "G", "H", "L"];
    let accumulatedData = {};
    highImpactClimateSectors.forEach((sector) => {
      accumulatedData = {
        ...accumulatedData,
        [`highImpactClimateSectorEnergyConsumptionNace${sector}`]: getFieldValueFromDataModel(
          `${pathWithoutField}.highImpactClimateSectorEnergyConsumptionNace${sector}`,
          dataset,
        ) as number,
      };
    });
    return { label: field.label, data: accumulatedData };
  };
}
