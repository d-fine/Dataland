import { DropdownOption } from "@/utils/PremadeDropdownDatasets";
import {
  DataAndMetaInformationEuTaxonomyDataForFinancials,
  DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  DataAndMetaInformationLksgData,
  DataAndMetaInformationPathwaysToParisData,
  DataAndMetaInformationSfdrData,
  DataAndMetaInformationSmeData,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForNonFinancials,
  LksgData,
  PathwaysToParisData,
  SfdrData,
  SmeData,
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
  component: string;
  dependency?: string;
  certificateRequiredIfYes?: boolean;
  validation?: string;
  validationLabel?: string;
  required?: boolean;
  unit?: string;
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
  | LksgData
  | SfdrData
  | SmeData
  | PathwaysToParisData;

export type DataAndMetaInformation =
  | DataAndMetaInformationEuTaxonomyDataForFinancials
  | DataAndMetaInformationEuTaxonomyDataForNonFinancials
  | DataAndMetaInformationLksgData
  | DataAndMetaInformationSfdrData
  | DataAndMetaInformationSmeData
  | DataAndMetaInformationPathwaysToParisData;
