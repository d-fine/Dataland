import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";

import {
  type GdvYearlyDecimalTimeseriesDataConfiguration,
  type MappedOptionalDecimal,
  type YearlyTimeseriesData,
} from "@/components/resources/dataTable/conversion/gdv/GdvYearlyDecimalTimeseriesData";
import GdvYearlyDecimalTimeseriesModal from "@/components/resources/dataTable/modals/GdvYearlyDecimalTimeseriesModal.vue";

/**
 * Formtas a GDVYearlyDecimalTimeseries for display in the table using a modal
 * @param input the input to display
 * @param options meta-data regarding the generic input
 * @param fieldLabel the label of the containing field
 * @returns the display-value for the table
 */
export function formatGdvYearlyDecimalTimeseriesDataForTable<KeyList extends string>(
  input: YearlyTimeseriesData<MappedOptionalDecimal<KeyList>> | null | undefined,
  options: GdvYearlyDecimalTimeseriesDataConfiguration<KeyList>,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  if (!input?.yearlyData || !input?.currentYear || Object.keys(input.yearlyData).length == 0) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        label: `Show data`,
        modalComponent: GdvYearlyDecimalTimeseriesModal,
        modalOptions: {
          props: {
            header: fieldLabel,
            modal: true,
            dismissableMask: true,
          },
          data: {
            label: fieldLabel,
            input: input,
            options: options,
          },
        },
      },
    };
  }
}
