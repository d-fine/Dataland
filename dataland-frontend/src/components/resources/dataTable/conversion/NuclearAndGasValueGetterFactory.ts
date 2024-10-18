/**
 * Formats a NuclearAndGas component for display in the multi-layer-data-table.
 * @param input the list of nuclear and gas economic activities
 * @param fieldLabel the label of the respective field in the framework
 * @returns the display object for the multi-layer-data-table to render a modal to display the
 * nuclear and gas economic activities.
 */
import {
  AvailableMLDTDisplayObjectTypes,
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

function isNonEligible(
    nuclearAndGasData: any
): nuclearAndGasData is ExtendedDataPointNuclearAndGasNonEligible {
  return (
      typeof nuclearAndGasData === 'object' &&
      nuclearAndGasData !== null &&
      'taxonomyNonEligibleShareNAndG426' in nuclearAndGasData.value
  );
}

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
      activityCount = Object.values(nuclearAndGasData.value).filter(value => value !== null).length;
    } else {
      activityCount = Object.values(nuclearAndGasData.value).filter(
          field => field && Object.values(field).some(value => value !== null)
      ).length;
    }

    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
      displayValue: {
        label: `Show ${activityCount} activit${activityCount > 1 ? 'ies' : 'y'}`,
        modalComponent: NuclearAndGasDataTable,
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