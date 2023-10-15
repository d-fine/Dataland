import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import { getFieldValueFromDataModel } from "@/components/resources/dataTable/conversion/Utils";
import { type Field } from "@/utils/GenericFrameworkTypes";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { formatNumberToReadableFormat } from "@/utils/Formatter";
import { type DataPointOneValueBigDecimal } from "@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/data-point-one-value-big-decimal";
import { type ObjectType } from "@/utils/UpdateObjectUtils";

interface HighImpactClimateDisplayFormat {
  sector: string;
  energyConsumption: string;
}

interface ValueObject {
  [key: string]: DataPointOneValueBigDecimal;
}

/**
 * Convert an object into a list that can be displayed using the standard
 * modal DataTable
 * @param datasetValue the value of the dataset
 * @returns the converted list
 */
function convertHighImpactClimateToListForModal(datasetValue: ValueObject): HighImpactClimateDisplayFormat[] {
  const listForModal: HighImpactClimateDisplayFormat[] = [];
  for (const [naceCodeType, climateSectorValues] of Object.entries(datasetValue)) {
    if (!climateSectorValues) continue;

    listForModal.push({
      sector: humanizeStringOrNumber(naceCodeType),
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
export function highImpactClimateGetterFactory(path: string, field: Field): (dataset: any) => AvailableDisplayValues {
  return (dataset) => {
    const pathWithoutField = `${path
      .split(".")
      .filter((item, index, array) => index < array.length - 1)
      .join(".")}`;
    const highImpactClimateSectors = ["A", "B", "C", "D", "E", "F", "G", "H", "L"];
    let accumulatedData: ObjectType = {};
    highImpactClimateSectors.forEach((sector: string) => {
      accumulatedData = {
        ...accumulatedData,
        [`NaceCode${sector}InGWh`]: getFieldValueFromDataModel(
          `${pathWithoutField}.applicableHighImpactClimateSectors.NaceCode${sector}InGWh`,
          dataset,
        ),
      };
    });

    return {
      displayComponent: MLDTDisplayComponents.ModalLinkDisplayComponent,
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
