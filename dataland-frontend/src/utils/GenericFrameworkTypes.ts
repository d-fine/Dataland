import { type DropdownOption } from "@/utils/PremadeDropdownDatasets";
import {
  type EuTaxonomyDataForFinancials,
  type EuTaxonomyDataForNonFinancials,
  type GdvData,
  type LksgData,
  type PathwaysToParisData,
  type SfdrData,
  type SmeData,
} from "@clients/backend";

// export function getSelectOptions(): DropdownOption[] {
//   return [
//       {label: "Hello", value: "World"},
//       {label: "Hello2", value: "World2"},
//       {label: "Hello3", value: "World3"},
//       {label: "Hello4", value: "World4"},
//   ]
// }

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
  | EuTaxonomyDataForNonFinancials
  | LksgData
  | SfdrData
  | SmeData
  | GdvData
  | PathwaysToParisData;
