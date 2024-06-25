import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import DetailsCompanyDataTable from '@/components/general/DetailsCompanyDataTable.vue';
import { type LksgProduct } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/lksg-product';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { convertNace, convertSingleNaceCode } from '@/utils/NaceCodeConverter';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter';
import { formatPercentageNumberAsString } from '@/utils/Formatter';
import {
  type LksgGrievanceAssessmentMechanism,
  type LksgProductionSite,
  type LksgRiskOrViolationAssessment,
  type LksgProcurementCategory,
} from '@clients/backend';
import { type ProcurementCategoryType } from '@/api-models/ProcurementCategoryType';

export const lksgModalColumnHeaders = {
  listOfProductionSites: {
    nameOfProductionSite: 'Name',
    addressOfProductionSite: 'Address',
    listOfGoodsOrServices: 'List of Goods or Services',
  },
  mostImportantProducts: {
    name: 'Product Name',
    productionSteps: 'Production Steps',
    relatedCorporateSupplyChain: 'Related Corporate Supply Chain',
  },
  procurementCategories: {
    procurementCategory: 'Procurement Category',
    procuredProductTypesAndServicesNaceCodes: 'Procured Products/Services',
    suppliersAndCountries: 'Number of Direct Suppliers and Countries',
    totalProcurementInPercent: 'Order Volume',
  },
  subcontractingCompanies: {
    country: 'Country',
    naceCodes: 'Industries',
  },
  riskPositions: {
    riskPosition: 'Risk Position',
    measuresTaken: 'Were Measures Defined to Counteract the Risk',
    listedMeasures: 'Which Measures',
  },
  generalViolations: {
    riskPosition: 'Risk Position of the Violation',
    measuresTaken: 'Were Counteracting Measures Taken',
    listedMeasures: 'Which Measures Have Been Taken',
  },
  grievanceMechanisms: {
    riskPositions: 'Risk Positions in the Complaint',
    specifiedComplaint: 'Complaint Specification',
    measuresTaken: 'Were Measures Taken to Address the Complaint',
    listedMeasures: 'Which Measures Have Been Taken',
  },
};

/**
 * Generates a list of readable strings (or just a single one) combining suppliers and their associated countries
 * @param numberOfSuppliersPerCountryCode the map of number of suppliers and associated companies
 * from which strings are written
 * @returns the constructed collection of readable strings
 */
function generateReadableCombinationOfNumberOfSuppliersAndCountries(numberOfSuppliersPerCountryCode: {
  [key: string]: number;
}): string[] {
  return Object.entries(numberOfSuppliersPerCountryCode).map(([countryCode, numberOfSuppliers]) => {
    const countryName = getCountryNameFromCountryCode(countryCode) ?? countryCode;
    if (numberOfSuppliers != undefined) {
      return `${numberOfSuppliers} suppliers from ${countryName}`;
    } else {
      return `There are suppliers from ${countryName}`;
    }
  });
}

type LksgProcurementType = { [key in ProcurementCategoryType]?: LksgProcurementCategory };
interface LksgProcurementCategoryDisplayFormat {
  procurementCategory: string;
  procuredProductTypesAndServicesNaceCodes: string[];
  suppliersAndCountries: string[];
  totalProcurementInPercent: string;
}

interface LksgSubcontractingCompaniesDisplayFormat {
  country: string;
  naceCodes: string[];
}

/**
 * Convert an object of type LksgProcurementType into a list that can be displayed using the standard
 * modal DataTable
 * @param datasetValue the value of the dataset
 * @returns the converted list
 */
function convertLksgProcumentTypeToListForModal(
  datasetValue: LksgProcurementType
): LksgProcurementCategoryDisplayFormat[] {
  const listForModal: LksgProcurementCategoryDisplayFormat[] = [];
  for (const [procurementCategoryType, lksgProcurementCategory] of Object.entries(datasetValue)) {
    if (!lksgProcurementCategory) continue;

    listForModal.push({
      procurementCategory: humanizeStringOrNumber(procurementCategoryType),
      procuredProductTypesAndServicesNaceCodes: (
        lksgProcurementCategory.procuredProductTypesAndServicesNaceCodes ?? []
      ).map(convertSingleNaceCode),
      suppliersAndCountries: generateReadableCombinationOfNumberOfSuppliersAndCountries(
        lksgProcurementCategory.numberOfSuppliersPerCountryCode ?? {}
      ),
      totalProcurementInPercent:
        lksgProcurementCategory.shareOfTotalProcurementInPercent != null
          ? formatPercentageNumberAsString(lksgProcurementCategory.shareOfTotalProcurementInPercent)
          : '',
    });
  }
  return listForModal;
}

/**
 * Convert an object of type LksgSubcontractingCompanies into a list that can be displayed using the standard
 * modal DataTable
 * @param datasetValue the value of the dataset
 * @returns the converted list
 */
function convertLksgSubcontractingCompaniesToListForModal(datasetValue: {
  [key: string]: Array<string>;
}): LksgSubcontractingCompaniesDisplayFormat[] {
  const listForModal: LksgSubcontractingCompaniesDisplayFormat[] = [];
  for (const [countryCode, naceCodes] of Object.entries(datasetValue)) {
    listForModal.push(<LksgSubcontractingCompaniesDisplayFormat>{
      country: getCountryNameFromCountryCode(countryCode),
      naceCodes: convertNace(naceCodes),
    });
  }
  return listForModal;
}

/**
 * Generate a ModalLinkDisplayComponent that displays the most important products for Lksg
 * @returns a ModalLinkDisplayComponent to the modal (if any data is present).
 * @param input List of Lksg Products
 * @param fieldLabel Field Label for the corresponding object
 */
export function formatLksgMostImportantProductsForDisplay(
  input: LksgProduct[] | null | undefined,
  fieldLabel: string
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
          kpiKeyOfTable: 'mostImportantProducts',
          columnHeaders: lksgModalColumnHeaders,
        },
      },
    },
  };
}

/**
 * Generates a display modal component for all procurement categories
 * @param input list of lksg procurement categories for display
 * @param fieldLabel Field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatLksgProcurementCategoriesForDisplay(
  input: LksgProcurementType | null | undefined,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  let convertedValueForModal = null;
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  }
  convertedValueForModal = convertLksgProcumentTypeToListForModal(input);

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
          kpiKeyOfTable: 'procurementCategories',
          columnHeaders: lksgModalColumnHeaders,
        },
      },
    },
  };
}

/**
 * Generates a display modal component for all subcontracting companies
 * @param input list of lksg procurement categories for display
 * @param fieldLabel Field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatLksgSubcontractingCompaniesForDisplay(
  input: { [key: string]: Array<string> } | null | undefined,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  let convertedValueForModal = null;
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    convertedValueForModal = convertLksgSubcontractingCompaniesToListForModal(input);
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
          kpiKeyOfTable: 'subcontractingCompanies',
          columnHeaders: lksgModalColumnHeaders,
        },
      },
    },
  };
}
/**
 * Generates a display modal component for all production sites
 * @param input list of lksg production site for display
 * @param fieldLabel field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatLksgProductionSitesForDisplay(
  input: LksgProductionSite[] | null | undefined,
  fieldLabel: string
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
          kpiKeyOfTable: 'listOfProductionSites',
          columnHeaders: lksgModalColumnHeaders,
        },
      },
    },
  };
}
interface LksgRiskOrViolationAssessmentDisplayFormat {
  riskPosition: string;
  measuresTaken: string;
  listedMeasures?: string | null;
}

/**
 * Converts the risk positions to human-readable strings
 * @param input to be humanized
 * @returns the converted object
 */
function convertLksgRiskOrViolationPositionForDisplay(
  input: LksgRiskOrViolationAssessment[]
): LksgRiskOrViolationAssessmentDisplayFormat[] {
  return input.map((item) => {
    const humanizedItem: LksgRiskOrViolationAssessmentDisplayFormat = {
      riskPosition: humanizeStringOrNumber(item.riskPosition),
      measuresTaken: humanizeStringOrNumber(item.measuresTaken),
      listedMeasures: item.listedMeasures,
    };
    return humanizedItem;
  });
}

/**
 * Generates a display modal component for the general violations or risks component
 * @param input list of lksg general violations/risks for display
 * @param fieldLabel field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatLksgRisksOrViolationsForDisplay(
  input: LksgRiskOrViolationAssessment[] | null | undefined,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    const convertedValueForModal = convertLksgRiskOrViolationPositionForDisplay(input);

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
            kpiKeyOfTable: 'riskPositions',
            columnHeaders: lksgModalColumnHeaders,
          },
        },
      },
    };
  }
}

interface LksgGrievanceMechanismsDisplayFormat {
  riskPositions: string[];
  specifiedComplaint: string;
  measuresTaken: string;
  listedMeasures?: string | null;
}
/**
 * Converts the risk positions to human-readable strings
 * @param input to be humanized
 * @returns the converted object
 */
function convertLksGrievanceMechanismsForDisplay(
  input: LksgGrievanceAssessmentMechanism[]
): LksgGrievanceMechanismsDisplayFormat[] {
  return input.map((item) => {
    const humanizedItem: LksgGrievanceMechanismsDisplayFormat = {
      riskPositions: item.riskPositions.map((riskPosition) => humanizeStringOrNumber(riskPosition)),
      specifiedComplaint: item.specifiedComplaint,
      measuresTaken: humanizeStringOrNumber(item.measuresTaken),
      listedMeasures: item.listedMeasures,
    };
    return humanizedItem;
  });
}

/**
 * Generates a display modal component for the grievance mechanism component
 * @param input list of lksg grievance mechanism for display
 * @param fieldLabel field label for the corresponding object
 * @returns ModalLinkDisplayComponent to the modal (if any data is present).
 */
export function formatLksgGrievanceMechanismsForDisplay(
  input: LksgGrievanceAssessmentMechanism[] | null | undefined,
  fieldLabel: string
): AvailableMLDTDisplayObjectTypes {
  if (!input) {
    return MLDTDisplayObjectForEmptyString;
  } else {
    const humanizedInput = convertLksGrievanceMechanismsForDisplay(input);

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
            listOfRowContents: humanizedInput,
            kpiKeyOfTable: 'grievanceMechanisms',
            columnHeaders: lksgModalColumnHeaders,
          },
        },
      },
    };
  }
}
