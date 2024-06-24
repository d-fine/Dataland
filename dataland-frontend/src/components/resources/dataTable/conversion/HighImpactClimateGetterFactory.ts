import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import DetailsCompanyDataTable from '@/components/general/DetailsCompanyDataTable.vue';
import { type ExtendedDataPointBigDecimal } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/extended-data-point-big-decimal';
import { HighImpactClimateSectorsKeys } from '@/types/HighImpactClimateSectors';
import { type ExtendedDataPoint } from '@/utils/DataPoint';
import { type SfdrHighImpactClimateSectorEnergyConsumption } from '@clients/backend';

interface HighImpactClimateDisplayFormat {
  sector: string;
  energyConsumption: Partial<ExtendedDataPoint<string>>;
  relativeEnergyConsumption: Partial<ExtendedDataPoint<string>>;
}

export type HighImpactClimateValueObject = {
  [key: string]: {
    highImpactClimateSectorEnergyConsumptionInGWh?: ExtendedDataPointBigDecimal | null;
    highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue?: ExtendedDataPointBigDecimal | null;
  };
};

/**
 * Convert an object into a list that can be displayed using the standard
 * modal DataTable
 * @param datasetValue the value of the dataset
 * @returns the converted list
 */
function convertHighImpactClimateToListForModal(
  datasetValue: HighImpactClimateValueObject
): HighImpactClimateDisplayFormat[] {
  const listForModal: HighImpactClimateDisplayFormat[] = [];
  for (const [naceCodeType, climateSectorValues] of Object.entries(datasetValue)) {
    const value = climateSectorValues.highImpactClimateSectorEnergyConsumptionInGWh;
    const revenueValue = climateSectorValues.highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue;

    if (!value && !revenueValue) {
      continue;
    }
    listForModal.push({
      sector: HighImpactClimateSectorsKeys[naceCodeType as keyof typeof HighImpactClimateSectorsKeys],
      energyConsumption: {
        value: `${value?.value ? value.value.toString() + ' GWh' : 'No data provided'}`,
        dataSource: value?.dataSource,
        quality: value?.quality === null ? undefined : value?.quality,
        comment: value?.comment,
      },
      relativeEnergyConsumption: {
        value: `${revenueValue?.value ? revenueValue.value.toString() + ' GWh / €M revenue' : 'No data provided'}`,
        dataSource: revenueValue?.dataSource,
        quality: revenueValue?.quality === null ? undefined : revenueValue?.quality,
        comment: revenueValue?.comment,
      },
    });
  }
  return listForModal;
}

/**
 * Formats the field value for the "High Impact Climate Sectors" field of the SFDR framework into a
 * format required for the display page
 * @param fieldValue the value to display
 * @returns the converted cell for the table
 */
export function formatHighImpactClimateSectorForDisplay(
  fieldValue: { [key: string]: SfdrHighImpactClimateSectorEnergyConsumption } | null | undefined
): AvailableMLDTDisplayObjectTypes {
  if (!fieldValue) return MLDTDisplayObjectForEmptyString;

  const highImpactClimateSectors = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'L'];
  const accumulatedData: HighImpactClimateValueObject = {};
  let doesAccumulatedDataContainData = false;

  for (const sector of highImpactClimateSectors) {
    const sectorFieldValue = fieldValue[`NaceCode${sector}`];
    if (!sectorFieldValue) continue;
    accumulatedData[`NaceCode${sector}`] = {
      highImpactClimateSectorEnergyConsumptionInGWh: sectorFieldValue.highImpactClimateSectorEnergyConsumptionInGWh,
      highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue:
        sectorFieldValue.highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue,
    };
  }

  doesAccumulatedDataContainData = Object.values(accumulatedData).some((obj) =>
    Object.values(obj).some((value) => value !== undefined)
  );

  if (!doesAccumulatedDataContainData) {
    return MLDTDisplayObjectForEmptyString;
  }
  return {
    displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
    displayValue: {
      label: 'Applicable High Impact Climate Sectors',
      modalComponent: DetailsCompanyDataTable,
      modalOptions: {
        props: {
          header: 'Applicable High Impact Climate Sectors',
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: convertHighImpactClimateToListForModal(accumulatedData),
          kpiKeyOfTable: 'highImpactSectorEnergyConsumptions',
          columnHeaders: {
            highImpactSectorEnergyConsumptions: {
              sector: 'Sector',
              energyConsumption: 'Energy Consumption',
              relativeEnergyConsumption: 'Relative Energy Consumption',
            },
          },
        },
      },
    },
  };
}
