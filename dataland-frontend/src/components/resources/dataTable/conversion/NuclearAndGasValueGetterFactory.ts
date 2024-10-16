/**
 * Formats a NuclearAndGasAlignedDenominator component for display in the table using a modal
 * @param input the input to display
 * @param fieldLabel the label of the containing field
 * @returns the display-value for the table
 */
import {
  AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import TaxonomyShareDataTable from '@/components/general/TaxonomyShareDataTable.vue';
import type {
  ExtendedDataPointNuclearAndGasAlignedDenominator,
  ExtendedDataPointNuclearAndGasAlignedNumerator,
  ExtendedDataPointNuclearAndGasEligibleButNotAligned,
  ExtendedDataPointNuclearAndGasNonEligible,
} from '@clients/backend';

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
  if (!nuclearAndGasData) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    //ToDo: What text should be displayed at the frontend?
    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        modalComponent: TaxonomyShareDataTable,
        modalOptions: {
          props: {
            header: fieldLabel,
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: fieldLabel,
            values: nuclearAndGasData.value,
          },
        },
      },
    };
  }
}
