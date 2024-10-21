import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import NuclearAndGasDataTable from '@/components/general/NuclearAndGasDataTable.vue';
import type {
  ExtendedDataPointNuclearAndGasAlignedDenominator,
  ExtendedDataPointNuclearAndGasAlignedNumerator,
  ExtendedDataPointNuclearAndGasEligibleButNotAligned,
  ExtendedDataPointNuclearAndGasNonEligible,
} from '@clients/backend';

/**
 * Checks if the provided NuclearAndGasData represents a non-eligible economic activity.
 * @param nuclearAndGasData The input data representing one of the Nuclear and Gas data points.
 * @returns Returns true if the data represents a non-eligible activity, otherwise false.
 */
function isNonEligible(
  nuclearAndGasData:
    | ExtendedDataPointNuclearAndGasAlignedDenominator
    | ExtendedDataPointNuclearAndGasAlignedNumerator
    | ExtendedDataPointNuclearAndGasEligibleButNotAligned
    | ExtendedDataPointNuclearAndGasNonEligible
): nuclearAndGasData is ExtendedDataPointNuclearAndGasNonEligible {
  return (
    typeof nuclearAndGasData === 'object' &&
    nuclearAndGasData !== null &&
    nuclearAndGasData.value !== null &&
    typeof nuclearAndGasData.value === 'object' &&
    'taxonomyNonEligibleShareNAndG426' in nuclearAndGasData.value
  );
}

export const nuclearAndGasModalColumnHeaders = {
  nuclearAndGas: {
    economicActivity: 'Economic Activity',
    ccmCca: 'CCM + CCA',
    ccm: 'CCM',
    cca: 'CCA',
  },
  nuclearAndGasNonEligible: {
    economicActivity: 'Economic Activity',
    proportion: 'Proportion',
  },
};

/**
 * Formats a NuclearAndGas component for display in the multi-layer-data-table.
 * @param nuclearAndGasData the list of nuclear and gas economic activities
 * @param fieldLabel the label of the respective field in the framework
 * @returns the display object for the multi-layer-data-table to render a modal to display the
 * nuclear and gas economic activities.
 */
export function formatNuclearAndGasTaxonomyShareDataForTable(
  nuclearAndGasData:
    | ExtendedDataPointNuclearAndGasAlignedDenominator
    | ExtendedDataPointNuclearAndGasAlignedNumerator
    | ExtendedDataPointNuclearAndGasEligibleButNotAligned
    | ExtendedDataPointNuclearAndGasNonEligible
    | null
    | undefined,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  if (!nuclearAndGasData || !nuclearAndGasData.value) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    let activityCount: number;

    if (isNonEligible(nuclearAndGasData)) {
      activityCount = Object.values(nuclearAndGasData.value).filter((value) => value !== null).length;
    } else {
      activityCount = Object.values(nuclearAndGasData.value).filter(
        (field) => field && Object.values(field).some((value) => value !== null)
      ).length;
    }

    const columnHeaders = isNonEligible(nuclearAndGasData)
      ? nuclearAndGasModalColumnHeaders.nuclearAndGasNonEligible
      : nuclearAndGasModalColumnHeaders.nuclearAndGas;

    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
      displayValue: {
        label: `Show ${activityCount} activit${activityCount > 1 ? 'ies' : 'y'}`,
        modalComponent: NuclearAndGasDataTable,
        columnHeaders: columnHeaders,
        modalOptions: {
          props: {
            header: fieldLabel,
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: fieldLabel,
            input: nuclearAndGasData.value,
            dataPointDisplay: {
              dataSource: nuclearAndGasData.dataSource,
              comment: nuclearAndGasData.comment,
              quality: nuclearAndGasData.quality,
            },
          },
        },
      },
    };
  }
}
