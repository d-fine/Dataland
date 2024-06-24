import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';

import {
  type EsgQuestionnaireYearlyDecimalTimeseriesDataConfiguration,
  type MappedOptionalDecimal,
  type YearlyTimeseriesData,
} from '@/components/resources/dataTable/conversion/esg-questionnaire/EsgQuestionnaireYearlyDecimalTimeseriesData';
import EsgQuestionnaireYearlyDecimalTimeseriesModal from '@/components/resources/dataTable/modals/EsgQuestionnaireYearlyDecimalTimeseriesModal.vue';

/**
 * Formtas a EsgQuestionnaireYearlyDecimalTimeseries for display in the table using a modal
 * @param input the input to display
 * @param options meta-data regarding the generic input
 * @param fieldLabel the label of the containing field
 * @returns the display-value for the table
 */
export function formatEsgQuestionnaireYearlyDecimalTimeseriesDataForTable<KeyList extends string>(
  input: YearlyTimeseriesData<MappedOptionalDecimal<KeyList>> | null | undefined,
  options: EsgQuestionnaireYearlyDecimalTimeseriesDataConfiguration<KeyList>,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  const yearlyData = input?.yearlyData;
  if (
    !yearlyData ||
    !input?.currentYear ||
    Object.keys(yearlyData).length == 0 ||
    areAllTimeSeriesNumbersNull(yearlyData)
  ) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
      displayValue: {
        label: `Show data`,
        modalComponent: EsgQuestionnaireYearlyDecimalTimeseriesModal,
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

/**
 * Checks if a timeseries object has null-values for all of its numbers
 * @param yearlyData contains the timeseries data as map of years to objects that contain number properties
 * @returns a boolean stating if all numbers are null
 */
function areAllTimeSeriesNumbersNull<KeyList extends string>(
  yearlyData: { [key: string]: MappedOptionalDecimal<KeyList> } | null | undefined
): boolean {
  for (const year in yearlyData) {
    const mappedOptionDecimalForOneYear = yearlyData[year];
    if (mappedOptionDecimalForOneYear !== null && typeof mappedOptionDecimalForOneYear === 'object') {
      for (const key in mappedOptionDecimalForOneYear) {
        const singleValue = mappedOptionDecimalForOneYear[key];
        if (singleValue !== null) {
          return false;
        }
      }
    }
  }
  return true;
}
