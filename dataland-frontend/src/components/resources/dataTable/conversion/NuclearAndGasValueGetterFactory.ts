import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import NuclearAndGasDataTable from '@/components/general/NuclearAndGasDataTable.vue';
import type {
  ExtendedDataPointNuclearAndGasAlignedDenominator,
  ExtendedDataPointNuclearAndGasAlignedNumerator,
  ExtendedDataPointNuclearAndGasEligibleButNotAligned,
  ExtendedDataPointNuclearAndGasNonEligible,
} from '@clients/backend';
import { formatStringForDatatable } from '@/components/resources/dataTable/conversion/PlainStringValueGetterFactory.ts';
import { buildDisplayValueWhenDataPointMetaInfoIsAvailable } from '@/components/resources/dataTable/conversion/DataPoints';

export const nuclearAndGasModalColumnHeaders = {
  nuclearAndGasAlignedOrEligible: {
    economicActivity: 'Economic Activity',
    mitigation: 'CCM',
    adaptation: 'CCA',
    mitigationAndAdaptation: 'CCM + CCA',
  },
  nuclearAndGasNonEligible: {
    economicActivity: 'Economic Activity',
    proportion: 'Proportion',
  },
};

/**
 * Formats a NuclearAndGas component for display in the multi-layer-data-table.
 * @param nuclearAndGasExtendedDataPoint the list of nuclear and gas economic activities
 * @param fieldLabel the label of the respective field in the framework
 * @returns the display object for the multi-layer-data-table to render a modal to display the
 * nuclear and gas economic activities.
 */
export function formatNuclearAndGasTaxonomyShareDataForTable(
  nuclearAndGasExtendedDataPoint:
    | ExtendedDataPointNuclearAndGasAlignedDenominator
    | ExtendedDataPointNuclearAndGasAlignedNumerator
    | ExtendedDataPointNuclearAndGasEligibleButNotAligned
    | ExtendedDataPointNuclearAndGasNonEligible
    | null
    | undefined,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  if (!nuclearAndGasExtendedDataPoint?.value) {
    if (nuclearAndGasExtendedDataPoint) {
      return {
        displayComponentName: MLDTDisplayComponentName.DataPointWrapperDisplayComponent,
        displayValue: buildDisplayValueWhenDataPointMetaInfoIsAvailable(
          MLDTDisplayObjectForEmptyString,
          fieldLabel,
          nuclearAndGasExtendedDataPoint
        ),
      };
    }
    return MLDTDisplayObjectForEmptyString;
  }

  return {
    displayComponentName: MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent,
    displayValue: {
      label: `Show Table`,
      modalComponent: NuclearAndGasDataTable,
      modalOptions: {
        props: {
          header: fieldLabel,
          modal: true,
          dismissableMask: true,
        },
        data: {
          columnHeaders: nuclearAndGasModalColumnHeaders,
          input: nuclearAndGasExtendedDataPoint.value,
          dataPointDisplay: {
            dataSource: nuclearAndGasExtendedDataPoint.dataSource,
            comment: nuclearAndGasExtendedDataPoint.comment,
            quality: nuclearAndGasExtendedDataPoint.quality,
          },
        },
      },
      dataSource: nuclearAndGasExtendedDataPoint.dataSource,
      comment: nuclearAndGasExtendedDataPoint.comment ?? undefined,
      quality: nuclearAndGasExtendedDataPoint.quality ?? undefined,
    },
  };
}
