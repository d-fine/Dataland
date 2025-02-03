import NaceCodeFormField from '@/components/forms/parts/fields/NaceCodeFormField.vue';
import InputTextFormField from '@/components/forms/parts/fields/InputTextFormField.vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import DateFormField from '@/components/forms/parts/fields/DateFormField.vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import MultiSelectFormField from '@/components/forms/parts/fields/MultiSelectFormField.vue';
import AddressFormField from '@/components/forms/parts/fields/AddressFormField.vue';
import RadioButtonsFormField from '@/components/forms/parts/fields/RadioButtonsFormField.vue';
import YesNoNaFormField from '@/components/forms/parts/fields/YesNoNaFormField.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import ProductionSitesFormField from '@/components/forms/parts/fields/ProductionSitesFormField.vue';
import LksgSubcontractingCompaniesFormField from '@/components/forms/parts/fields/LksgSubcontractingCompaniesFormField.vue';
import MostImportantProductsFormField from '@/components/forms/parts/fields/MostImportantProductsFormField.vue';
import ProcurementCategoriesFormField from '@/components/forms/parts/fields/ProcurementCategoriesFormField.vue';
import IntegerExtendedDataPointFormField from '@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';
import YesNoBaseDataPointFormField from '@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue';
import YesNoNaBaseDataPointFormField from '@/components/forms/parts/fields/YesNoNaBaseDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue';
import AmountWithCurrencyFormField from '@/components/forms/parts/fields/AmountWithCurrencyFormField.vue';
import BigDecimalBaseDataPointFormField from '@/components/forms/parts/fields/BigDecimalBaseDataPointFormField.vue';
import RiskAssessmentsFormField from '@/components/forms/parts/fields/RiskAssessmentsFormField.vue';
import GeneralViolationsAssessmentsFormField from '@/components/forms/parts/fields/GeneralViolationsAssessmentsFormField.vue';
import GrievanceMechanismAssessmentsFormField from '@/components/forms/parts/fields/GrievanceMechanismAssessmentsFormField.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const componentNameMap: { [key: string]: any } = {
  NaceCodeFormField,
  InputTextFormField,
  FreeTextFormField,
  NumberFormField,
  DateFormField,
  SingleSelectFormField,
  MultiSelectFormField,
  AddressFormField,
  RadioButtonsFormField,
  YesNoNaFormField,
  PercentageFormField,
  ProductionSitesFormField,
  LksgSubcontractingCompaniesFormField,
  MostImportantProductsFormField,
  ProcurementCategoriesFormField,
  IntegerExtendedDataPointFormField,
  BigDecimalExtendedDataPointFormField,
  CurrencyDataPointFormField,
  YesNoBaseDataPointFormField,
  YesNoNaBaseDataPointFormField,
  YesNoExtendedDataPointFormField,
  AmountWithCurrencyFormField,
  BigDecimalBaseDataPointFormField,
  RiskAssessmentsFormField,
  GeneralViolationsAssessmentsFormField,
  GrievanceMechanismAssessmentsFormField,
  YesNoFormField,
};

/**
 * Get a component for the upload page by name
 * @param name the name of the component
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getComponentByName(name: string): any {
  if (componentNameMap[name]) {
    return componentNameMap[name];
  }
  throw new Error(`Unknown component ${name}`);
}
