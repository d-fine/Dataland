import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";
import { type Field } from "@/utils/GenericFrameworkTypes";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { type ExtendedDataPointBigDecimal } from "@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/extended-data-point-big-decimal";
import { HighImpactClimateSectorsKeys } from "@/types/HighImpactClimateSectors";

interface HighImpactClimateDisplayFormat {
  sector: string;
  energyConsumption: string;
}

export type HighImpactClimateValueObject = {
  [key: string]: ExtendedDataPointBigDecimal;
};

/**
 * Convert an object into a list that can be displayed using the standard
 * modal DataTable
 * @param datasetValue the value of the dataset
 * @returns the converted list
 */
function convertHighImpactClimateToListForModal(
  datasetValue: HighImpactClimateValueObject,
): HighImpactClimateDisplayFormat[] {
  const listForModal: HighImpactClimateDisplayFormat[] = [];
  for (const [naceCodeType, climateSectorValues] of Object.entries(datasetValue)) {
    if (!climateSectorValues) continue;
    listForModal.push({
      sector: HighImpactClimateSectorsKeys[naceCodeType as keyof typeof HighImpactClimateSectorsKeys] as string,
      energyConsumption:
        climateSectorValues.value !== null && climateSectorValues.value !== undefined
          ? formatNumberToReadableFormat(climateSectorValues.value)
          : "",
    });
  }
  return listForModal;
}

/**
 * Returns a value factory that returns the value of the field as a string using the display mapping in the options field
 * If the value is non-truthy, an empty string is returned
 * @param path the path to the field
 * @param field the single select field
 * @returns the created getter
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function highImpactClimateGetterFactory(
  path: string,
  field: Field,
): (dataset: HighImpactClimateValueObject) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const highImpactClimateSectors = ["A", "B", "C", "D", "E", "F", "G", "H", "L"];
    let accumulatedData: HighImpactClimateValueObject = {};
    highImpactClimateSectors.forEach((sector: string) => {
      accumulatedData = {
        ...accumulatedData,
        [`NaceCode${sector}InGWh`]: getFieldValueFromDataModel(
          `${path}.NaceCode${sector}InGWh`,
          dataset,
        ) as ExtendedDataPointBigDecimal,
      };
    });
    return {
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        label: field.label,
        modalComponent: DetailsCompanyDataTable,
        modalOptions: {
          props: {
            header: field.label,
            modal: true,
            dismissableMask: true,
          },
          data: {
            listOfRowContents: convertHighImpactClimateToListForModal(accumulatedData),
            kpiKeyOfTable: "highImpactSectorEnergyConsumptions",
            columnHeaders: {
              highImpactSectorEnergyConsumptions: {
                sector: "Sectors",
                energyConsumption: "Energy Consumption",
              },
            },
          },
        },
      },
    };
  };
}
