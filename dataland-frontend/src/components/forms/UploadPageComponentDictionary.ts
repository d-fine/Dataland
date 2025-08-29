import AddressFormField from '@/components/forms/parts/fields/AddressFormField.vue';
import AmountWithCurrencyFormField from '@/components/forms/parts/fields/AmountWithCurrencyFormField.vue';
import BigDecimalBaseDataPointFormField from '@/components/forms/parts/fields/BigDecimalBaseDataPointFormField.vue';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';
import DateFormField from '@/components/forms/parts/fields/DateFormField.vue';
import EmployeesPerCountryFormField from '@/components/forms/parts/fields/EmployeesPerCountryFormField.vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import GeneralViolationsAssessmentsFormField from '@/components/forms/parts/fields/GeneralViolationsAssessmentsFormField.vue';
import GrievanceMechanismAssessmentsFormField from '@/components/forms/parts/fields/GrievanceMechanismAssessmentsFormField.vue';
import InputTextFormField from '@/components/forms/parts/fields/InputTextFormField.vue';
import IntegerExtendedDataPointFormField from '@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue';
import ListOfBaseDataPointsFormField from '@/components/forms/parts/fields/ListOfBaseDataPointsFormField.vue';
import LksgSubcontractingCompaniesFormField from '@/components/forms/parts/fields/LksgSubcontractingCompaniesFormField.vue';
import MostImportantProductsFormField from '@/components/forms/parts/fields/MostImportantProductsFormField.vue';
import MultiSelectFormField from '@/components/forms/parts/fields/MultiSelectFormField.vue';
import NaceCodeFormField from '@/components/forms/parts/fields/NaceCodeFormField.vue';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import PollutionEmissionFormField from '@/components/forms/parts/fields/PollutionEmissionFormField.vue';
import ProcurementCategoriesFormField from '@/components/forms/parts/fields/ProcurementCategoriesFormField.vue';
import ProductionSitesFormField from '@/components/forms/parts/fields/ProductionSitesFormField.vue';
import RadioButtonsExtendedDataPointFormField from '@/components/forms/parts/fields/RadioButtonsExtendedDataPointFormField.vue';
import RadioButtonsFormField from '@/components/forms/parts/fields/RadioButtonsFormField.vue';
import RiskAssessmentsFormField from '@/components/forms/parts/fields/RiskAssessmentsFormField.vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import ExtendedSingleSelectFormField from '@/components/forms/parts/fields/ExtendedSingleSelectFormField.vue';
import SiteAndAreaFormField from '@/components/forms/parts/fields/SiteAndAreaFormField.vue';
import SubsidiaryFormField from '@/components/forms/parts/fields/SubsidiaryFormField.vue';
import WasteClassificationFormField from '@/components/forms/parts/fields/WasteClassificationFormField.vue';
import YesNoBaseDataPointFormField from '@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import YesNoNaBaseDataPointFormField from '@/components/forms/parts/fields/YesNoNaBaseDataPointFormField.vue';
import YesNoNaFormField from '@/components/forms/parts/fields/YesNoNaFormField.vue';
import UploadReports from '@/components/forms/parts/UploadReports.vue';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const componentNameMap: { [key: string]: any } = {
  AddressFormField,
  AmountWithCurrencyFormField,
  BigDecimalBaseDataPointFormField,
  BigDecimalExtendedDataPointFormField,
  CurrencyDataPointFormField,
  DateFormField,
  EmployeesPerCountryFormField,
  ExtendedSingleSelectFormField,
  FreeTextFormField,
  GeneralViolationsAssessmentsFormField,
  GrievanceMechanismAssessmentsFormField,
  InputTextFormField,
  IntegerExtendedDataPointFormField,
  ListOfBaseDataPointsFormField,
  LksgSubcontractingCompaniesFormField,
  MostImportantProductsFormField,
  MultiSelectFormField,
  NaceCodeFormField,
  NumberFormField,
  PercentageFormField,
  PollutionEmissionFormField,
  ProcurementCategoriesFormField,
  ProductionSitesFormField,
  RadioButtonsExtendedDataPointFormField,
  RadioButtonsFormField,
  RiskAssessmentsFormField,
  SingleSelectFormField,
  SiteAndAreaFormField,
  SubsidiaryFormField,
  UploadReports,
  WasteClassificationFormField,
  YesNoBaseDataPointFormField,
  YesNoExtendedDataPointFormField,
  YesNoFormField,
  YesNoNaBaseDataPointFormField,
  YesNoNaFormField,
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
