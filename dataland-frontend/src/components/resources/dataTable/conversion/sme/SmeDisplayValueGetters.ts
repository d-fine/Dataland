import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import {
  type SmeSubsidiary,
  type SmePollutionEmission,
  type ReleaseMedium,
  type SmeWasteClassificationObject,
  type SmeSiteAndArea,
  type SmeEmployeesPerCountry,
} from "@clients/backend";
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
  wasteClassification: {
    classificationOfWaste: "Classification of waste",
    typeOfWaste: "Type of Waste",
    totalAmountTons: "Total amout in tons",
    wasteRecycleOrReuseTons: "Waste recycle or reuse tons",
    wasteDisposalTons: "Waste disposal in tons",
    totalAmountCubicMeters: "Total amount in cubic meters",
    wasteRecycleOrReuseInCubicMeters: "Waste recycle or reuse in m³",
    wasteDisposalCubicMeters: "Waste disposal in m³",
  },
  siteAndArea: {
    siteName: "Name of the Site",
    siteAddress: "Address of the Site",
    siteGeocoordinateLongitudeval: "Longitudeval Geocoordinates of the Site",
    siteGeocoordinateLatitude: "Latitude Geocoordinates of the Site",
    areaInHectare: "Area Size in Hectare",
    biodiversitySensitiveArea: "Specify the Biodiversity-senstive Area",
    areaAddress: "Address of the Area",
    areaGeocoordinateLongitude: "Longitudeval Geocoordinates of the Area",
    areaGeocoordinateLatitude: "Latitude Geocoordinates of the Area",
    specificationOfAdjointness: "Adjointness of the Area to the Site",
  },
  employeesPerCountry: {
    country: "Country",
    numberOfEmployeesInHeadCount: "Number of Employees in Head Count",
    numberOfEmployeesInFullTimeEquivalent: "Number of Employees in Full Time Equivalents",
  },
};
interface SmePollutionEmissionDisplayFormat {
  pollutionType: string;
  emissionInKilograms: number;
  releaseMedium: ReleaseMedium;
}

interface SmeWasteClassificationDisplayFormat {
  wasteClassification: string;
  typeWaste: string;
  totalAmountTons: number;
  wasteRecycleOrReuseTons: number;
  wasteDisposalTons: number;
  totalAmountCubicMeters: number;
  wasteRecycleOrReuseInCubicMeters: number;
  wasteDisposalCubicMeters: number;
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
 * Generates a display modal component for all waste classification components
 * @param input list of sme waste classifications
 * @param fieldLabel Field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatSmeWasteClassificationObjectForDisplay(
  input: SmeWasteClassificationObject[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  let convertedValueForModal = null;
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    convertedValueForModal = convertSmeWasteClassificationToListForModal(input);
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
          kpiKeyOfTable: "wasteClassification",
          columnHeaders: smeModalColumnHeaders,
        },
      },
    },
  };
}

/**
 * Convert an object of type LksgSubcontractingCompanies into a list that can be displayed using the standard
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

/**
 * Convert an object of type SmeWasteClassificationObject into a list that can be displayed using the standard
 * modal DataTable
 * @param datasetValue the value of the dataset
 * @returns the converted list
 */
function convertSmeWasteClassificationToListForModal(
  datasetValue: SmeWasteClassificationObject[],
): SmeWasteClassificationDisplayFormat[] {
  return datasetValue.map((item) => {
    const humanizedItem: SmeWasteClassificationDisplayFormat = {
      wasteClassification: humanizeStringOrNumber(item.wasteClassification),
      typeWaste: humanizeStringOrNumber(item.typeOfWaste),
      totalAmountTons: item.totalAmountTons!,
      wasteRecycleOrReuseTons: item.wasteRecycleOrReuseTons!,
      wasteDisposalTons: item.wasteDisposalTons!,
      totalAmountCubicMeters: item.totalAmountCubicMeters!,
      wasteRecycleOrReuseInCubicMeters: item.wasteRecycleOrReuseCubicMeters!,
      wasteDisposalCubicMeters: item.wasteDisposalCubicMeters!,
      //TODO come back here and see if ! are really necessary
    };
    return humanizedItem;
  });
}

/**
 * Generates a display modal component for all site and area components
 * @param input list of sme site and area
 * @param fieldLabel Field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatSmeSiteAndAreaForDisplay(
  input: SmeSiteAndArea[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
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
            kpiKeyOfTable: "siteAndArea",
            columnHeaders: smeModalColumnHeaders,
          },
        },
      },
    };
  }
}
/**
 * Generates a display modal component for all employees per country components
 * @param input list of sme employees per country
 * @param fieldLabel Field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatSmeEmployeesPerCountryForDisplay(
  input: SmeEmployeesPerCountry[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
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
            kpiKeyOfTable: "employeesPerCountry",
            columnHeaders: smeModalColumnHeaders,
          },
        },
      },
    };
  }
}
