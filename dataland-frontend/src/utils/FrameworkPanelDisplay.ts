import { naceCodeMap } from "@/components/forms/parts/elements/derived/NaceCodeTree";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { Field } from "@/utils/GenericFrameworkTypes";
import { KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";

/**
 * Converts a number to millions with max two decimal places and adds "MM" at the end of the number.
 * @param inputNumber The number to convert
 * @returns a string with the converted number and "MM" at the end
 */
export function convertToMillions(inputNumber: number): string {
  return `${(inputNumber / 1000000).toLocaleString("en-GB", { maximumFractionDigits: 2 })} MM`;
}

/**
 * Converts a nace code to a human readable value
 * @param kpiValue the value that should be reformated corresponding to its field
 * @returns the reformatted Country value ready for display
 */
export function reformatIndustriesValue(kpiValue: KpiValue) {
  return Array.isArray(kpiValue)
    ? kpiValue.map((naceCodeShort: string) => naceCodeMap.get(naceCodeShort)?.label ?? naceCodeShort)
    : naceCodeMap.get(kpiValue as string)?.label ?? kpiValue;
}

/**
 * Converts a country code to a human readable value
 * @param kpiValue the value that should be reformated corresponding to its field
 * @returns the reformatted Country value ready for display
 */
export function reformatCountriesValue(kpiValue: KpiValue) {
  return Array.isArray(kpiValue)
    ? kpiValue.map((countryCodeShort: string) => getCountryNameFromCountryCode(countryCodeShort) ?? countryCodeShort)
    : getCountryNameFromCountryCode(kpiValue as string) ?? kpiValue;
}

/**
 *
 * @param kpiField the Field to which the value belongs
 * @param kpiValue the value that should be reformated corresponding to its field
 * @returns the reformatted value ready for display
 */
export function reformatValueForDisplay(kpiField: Field, kpiValue: KpiValue): KpiValue {
  if (kpiField.name === "totalRevenue" && typeof kpiValue === "number") {
    kpiValue = convertToMillions(kpiValue);
  }
  if (kpiField.name === "industry" || kpiField.name === "subcontractingCompaniesIndustries") {
    kpiValue = reformatIndustriesValue(kpiValue);
  }
  if (kpiField.name.includes("Countries") && kpiField.component !== "YesNoFormField") {
    kpiValue = reformatCountriesValue(kpiValue);
  }

  let returnValue;

  if (kpiField.options?.length) {
    const filteredOption = kpiField.options.find((option: DropdownOption) => option.value === kpiValue);
    if (filteredOption) returnValue = filteredOption.label;
  }

  return returnValue ?? kpiValue;
}
