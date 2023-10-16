import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayObjectForEmptyString,
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { getFieldValueFromFrameworkDataset } from "@/components/resources/dataTable/conversion/Utils";
import { type Field } from "@/utils/GenericFrameworkTypes";
import { type LksgProcurementCategory } from "@clients/backend";
import { type ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";
import { convertSingleNaceCode } from "@/utils/NaceCodeConverter";
import { formatPercentageNumberAsString } from "@/utils/Formatter";
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import { lksgModalColumnHeaders } from "@/components/resources/frameworkDataSearch/lksg/LksgModalColumnHeaders";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";

export type LksgProcurementType = { [key in ProcurementCategoryType]?: LksgProcurementCategory };

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

interface LksgProcurementCategoryDisplayFormat {
  procurementCategory: string;
  procuredProductTypesAndServicesNaceCodes: string[];
  suppliersAndCountries: string[];
  totalProcurementInPercent: string;
}

/**
 * Convert an object of type LksgProcurementType into a list that can be displayed using the standard
 * modal DataTable
 * @param datasetValue the value of the dataset
 * @returns the converted list
 */
function convertLksgProcumentTypeToListForModal(
  datasetValue: LksgProcurementType,
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
        lksgProcurementCategory.numberOfSuppliersPerCountryCode ?? {},
      ),
      totalProcurementInPercent:
        lksgProcurementCategory.shareOfTotalProcurementInPercent != null
          ? formatPercentageNumberAsString(lksgProcurementCategory.shareOfTotalProcurementInPercent)
          : "",
    });
  }
  return listForModal;
}

/**
 * Returns a value factory that returns the value of the LKSG Procurement field as a modal
 * If the value is non-truthy, an empty string is returned
 * @param path the path to the field
 * @param field the underlying form field
 * @returns the created getter
 */
export function lksgProcurementCategoriesValueGetterFactory(
  path: string,
  field: Field,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): (dataset: any) => AvailableMLDTDisplayObjectTypes {
  return (dataset) => {
    const datasetValue = getFieldValueFromFrameworkDataset(path, dataset) as LksgProcurementType | undefined;
    if (!datasetValue || Object.keys(datasetValue).length == 0) {
      return MLDTDisplayObjectForEmptyString;
    }

    const convertedValueForModal = convertLksgProcumentTypeToListForModal(datasetValue);

    return <MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponentName>>{
      displayComponentName: MLDTDisplayComponentName.ModalLinkDisplayComponentName,
      displayValue: {
        label: `Show ${field.label}`,
        modalComponent: DetailsCompanyDataTable,
        modalOptions: {
          props: {
            header: field.label,
            modal: true,
            dismissableMask: true,
          },
          data: {
            listOfRowContents: convertedValueForModal,
            kpiKeyOfTable: "procurementCategories",
            columnHeaders: lksgModalColumnHeaders,
          },
        },
      },
    };
  };
}
