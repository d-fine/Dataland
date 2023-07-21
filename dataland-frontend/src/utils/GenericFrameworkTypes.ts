import { DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { LksgData, PathwaysToParisData } from "@clients/backend";
export interface Category {
  name: string;
  label: string;
  color: string;
  showIf: (dataModel?: LksgData | PathwaysToParisData) => boolean;
  subcategories: Array<Subcategory>;
}

export interface Subcategory {
  name: string;
  label: string;
  fields: Array<Field>;
}

export interface Field {
  showIf: (dataModel?: LksgData | PathwaysToParisData) => boolean;
  name: string;
  label: string;
  description: string;
  component: string;
  dependency?: string;
  certificateRequiredIfYes?: boolean;
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
