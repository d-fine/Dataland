import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import { type SmeSubsidiary, type SmePollutionEmission, type ReleaseMedium } from "@clients/backend";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";

export const smeModalColumnHeaders = {
  listOfSubsidiary: {
    nameOfSubsidiary: "Name",
    addressOfSubsidiary: "Address",
  },
  pollutionEmission: {
    pollutionType: "Type of Pollution",
    emissionInKilograms: "Amount of Emission in kg",
    releaseMedium: "Release Medium",
  },
};
interface SmePollutionEmissionDisplayFormat {
  pollutionType: string;
  emissionInKilograms: number;
  releaseMedium: ReleaseMedium;
}

/**
 * Generates a display modal component for subsidiaries
 * @param input list of sme subsidiaries for display
 * @param fieldLabel field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatSmeSubsidiaryForDisplay(
  input: SmeSubsidiary[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  }

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
    displayValue: {
      label: `Show ${fieldLabel}`,
      modalComponent: DetailsCompanyDataTable,
      modalOptions: {
        props: {
          header: fieldLabel,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: input,
          kpiKeyOfTable: "listOfSubsidiary",
          columnHeaders: smeModalColumnHeaders,
        },
      },
    },
  };
}
/**
 * Generates a display modal component for all pollution emission
 * @param input list of sme pollution emission
 * @param fieldLabel Field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatSmePollutionEmissionForDisplay(
  input: SmePollutionEmission[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  let convertedValueForModal = null;
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    convertedValueForModal = convertSmePollutionEmissionToListForModal(input);
  }

  return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponent>>{
    displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponent,
    displayValue: {
      label: `Show ${fieldLabel}`,
      modalComponent: DetailsCompanyDataTable,
      modalOptions: {
        props: {
          header: fieldLabel,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: convertedValueForModal,
          kpiKeyOfTable: "pollutionEmission",
          columnHeaders: smeModalColumnHeaders,
        },
      },
    },
  };
}
/**
 * Convert an object of type SmePollutionEmission into a list that can be displayed using the standard
 * modal DataTable
 * @param datasetValue the value of the dataset
 * @returns the converted list
 */
function convertSmePollutionEmissionToListForModal(datasetValue: SmePollutionEmission[]): SmePollutionEmission[] {
  return datasetValue.map((item) => {
    const humanizedItem: SmePollutionEmissionDisplayFormat = {
      pollutionType: humanizeStringOrNumber(item.pollutionType),
      emissionInKilograms: item.emissionInKilograms!,
      releaseMedium: item.releaseMedium!,
      //TODO come back here and see if ! are really necessary
    };
    return humanizedItem;
  });
}
