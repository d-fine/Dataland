import { type DropdownOption } from "@/utils/PremadeDropdownDatasets";
import {
  type EuTaxonomyDataForFinancials,
  type EsgQuestionnaireData,
  type LksgData,
  type PathwaysToParisData,
  type SfdrData,
  type SmeData,
  type EutaxonomyNonFinancialsData,
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
  name: string;
  label: string;
  description: string;
  unit?: string;
  component: string;
  dependency?: string;
  showIf: (dataModel?: FrameworkData) => boolean;
  validation?: string;
  validationLabel?: string;
  required?: boolean;

  // input field specific values
  placeholder?: string;

  // selection specific values
  options?: DropdownOption[];
  // filed form fields from an existing data set
  existingFieldValues?: (dataModel?: LksgData) => object;
}

export type FrameworkData =
  | EuTaxonomyDataForFinancials
  | EutaxonomyNonFinancialsData
  | LksgData
  | SfdrData
  | SmeData
  | EsgQuestionnaireData
  | PathwaysToParisData;
