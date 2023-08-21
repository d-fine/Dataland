import { type DropdownOption } from "@/utils/PremadeDropdownDatasets";
import {
  type DataAndMetaInformationEuTaxonomyDataForFinancials,
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  type DataAndMetaInformationLksgData, DataAndMetaInformationNewEuTaxonomyDataForNonFinancials,
  type DataAndMetaInformationPathwaysToParisData,
  type DataAndMetaInformationSfdrData,
  type DataAndMetaInformationSmeData,
  type EuTaxonomyDataForFinancials,
  type EuTaxonomyDataForNonFinancials,
  type LksgData, NewEuTaxonomyDataForNonFinancials,
  type PathwaysToParisData,
  type SfdrData,
  type SmeData,
} from "@clients/backend";

export interface Category {
  name: string;
  label: string;
  color: string;
  showIf: (dataModel?: FrameworkData) => boolean;
  subcategories: Array<Subcategory>;
}

export interface Subcategory {
  name: string;
  label: string;
  fields: Array<Field>;
}

export interface Field {
  showIf: (dataModel?: FrameworkData) => boolean;
  name: string;
  label: string;
  description: string;
  unit?: string;
  component: string;
  dependency?: string;
  certificateRequiredIfYes?: boolean;
  validation?: string;
  validationLabel?: string;
  required?: boolean;
  evidenceDesired?: boolean;

  // input field specific values
  placeholder?: string;

  // selection specific values
  options?: DropdownOption[];
  // filed form fields from an existing data set
  existingFieldValues?: (dataModel?: LksgData) => object;
}

export type FrameworkData =
  | EuTaxonomyDataForFinancials
  | EuTaxonomyDataForNonFinancials
    | NewEuTaxonomyDataForNonFinancials
  | LksgData
  | SfdrData
  | SmeData
  | PathwaysToParisData;

export type DataAndMetaInformation =
  | DataAndMetaInformationEuTaxonomyDataForFinancials
  | DataAndMetaInformationEuTaxonomyDataForNonFinancials
    | DataAndMetaInformationNewEuTaxonomyDataForNonFinancials
  | DataAndMetaInformationLksgData
  | DataAndMetaInformationSfdrData
  | DataAndMetaInformationSmeData
  | DataAndMetaInformationPathwaysToParisData;
