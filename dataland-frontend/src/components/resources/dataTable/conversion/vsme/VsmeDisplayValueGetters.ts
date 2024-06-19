import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import {
  type VsmeSubsidiary,
  type VsmePollutionEmission,
  type ReleaseMedium,
  type VsmeWasteClassificationObject,
  type VsmeSiteAndArea,
  type VsmeEmployeesPerCountry,
  type WasteClassifications,
} from "@clients/backend";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";

export const vsmeModalColumnHeaders = {
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
interface VsmePollutionEmissionDisplayFormat {
  pollutionType: string;
  emissionInKilograms: number;
  releaseMedium: ReleaseMedium;
}

interface VsmeWasteClassificationDisplayFormat {
  wasteClassification: WasteClassifications;
  typeWaste: string;
  totalAmountInTons: number;
  wasteRecycleOrReuseInTons: number;
  wasteDisposalInTons: number;
  totalAmountInCubicMeters: number;
  wasteRecycleOrReuseInCubicMeters: number;
  wasteDisposalInCubicMeters: number;
}

/**
 * Generates a display modal component for subsidiaries
 * @param input list of sme subsidiaries for display
 * @param fieldLabel field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatVsmeSubsidiaryForDisplay(
  input: VsmeSubsidiary[] | null | undefined,
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
          columnHeaders: vsmeModalColumnHeaders,
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
export function formatVsmePollutionEmissionForDisplay(
  input: VsmePollutionEmission[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  let convertedValueForModal = null;
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    convertedValueForModal = convertVsmePollutionEmissionToListForModal(input);
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
          columnHeaders: vsmeModalColumnHeaders,
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
export function formatVsmeWasteClassificationObjectForDisplay(
  input: VsmeWasteClassificationObject[] | null | undefined,
  fieldLabel: string,
): AvailableMLDTDisplayObjectTypes {
  let convertedValueForModal = null;
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    convertedValueForModal = convertVsmeWasteClassificationToListForModal(input);
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
          columnHeaders: vsmeModalColumnHeaders,
        },
      },
    },
  };
}

/**
 * Convert an object of type VsmePollutionEmission into a list that can be displayed using the standard
 * modal DataTable
 * @param datasetValue the value of the dataset
 * @returns the converted list
 */
function convertVsmePollutionEmissionToListForModal(datasetValue: VsmePollutionEmission[]): VsmePollutionEmission[] {
  return datasetValue.map((item) => {
    const humanizedItem: VsmePollutionEmissionDisplayFormat = {
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
function convertVsmeWasteClassificationToListForModal(
  datasetValue: VsmeWasteClassificationObject[],
): VsmeWasteClassificationDisplayFormat[] {
  return datasetValue.map((item) => {
    const humanizedItem: VsmeWasteClassificationDisplayFormat = {
      wasteClassification: item.wasteClassification,
      typeWaste: humanizeStringOrNumber(item.typeOfWaste),
      totalAmountInTons: item.totalAmountInTons!,
      wasteRecycleOrReuseInTons: item.wasteRecycleOrReuseInTons!,
      wasteDisposalInTons: item.wasteDisposalInTons!,
      totalAmountInCubicMeters: item.totalAmountInCubicMeters!,
      wasteRecycleOrReuseInCubicMeters: item.wasteRecycleOrReuseInCubicMeters!,
      wasteDisposalInCubicMeters: item.wasteDisposalInCubicMeters!,
      //TODO come back here and see if ! are really necessary
      //TODO check why humanzeStringOrNumber doesnt work for wasteClassification, see riskposition for lksg
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
export function formatVsmeSiteAndAreaForDisplay(
  input: VsmeSiteAndArea[] | null | undefined,
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
            columnHeaders: vsmeModalColumnHeaders,
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
export function formatVsmeEmployeesPerCountryForDisplay(
  input: VsmeEmployeesPerCountry[] | null | undefined,
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
            columnHeaders: vsmeModalColumnHeaders,
          },
        },
      },
    };
  }
}
