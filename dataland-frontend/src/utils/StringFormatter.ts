/**
 * Module to convert string to a human-readable text
 */

import { HumanizedYesNoNa } from "@/utils/YesNoNa";
import { getBaseFrameworkDefinition } from "@/frameworks/BaseFrameworkRegistry";

/**
 * convert kebab case string to pascal case string using regex
 * @param rawText is the string to be converted
 * @returns the converted string in pascal case
 */
export function convertKebabCaseToPascalCase(rawText: string): string {
  const camelCase = rawText.replace(/-([a-z])/g, (_, char: string) => char.toUpperCase());
  const pascalCase = camelCase.charAt(0).toUpperCase() + camelCase.slice(1);
  return pascalCase;
}

/**
 * convert camel case string to sentence case string using regex
 * @param rawText is the string to be converted to a human-readable string
 * @returns the converted string in "sentence-case"
 */
function convertCamelCaseToSentenceCase(rawText: string): string {
  // Split the sting to words
  const processedText = rawText.replace(/((?!^)[A-Z])/g, " $1");
  // uppercase the first letter of the first word
  return processedText.charAt(0).toUpperCase() + processedText.slice(1);
}

/**
 * get the representable text from the mapping object
 * @param rawText is the string to be converted to a human-readable string
 * @returns the converted string
 */
function humanizeViaMapping(rawText: string): string {
  const mappingObject: { [key: string]: string } = {
    isin: "ISIN",
    permid: "PermID",
    lei: "LEI",
    ticker: "Ticker",
    duns: "DUNS",
    eligiblecapex: "Eligible CapEx",
    eligibleopex: "Eligible OpEx",
    alignedcapex: "Aligned CapEx",
    alignedopex: "Aligned OpEx",
    insuranceorreinsurance: "Insurance or Reinsurance",
    annualreport: "Annual Report",
    sustainabilityreport: "Sustainability Report",
    integratedreport: "Integrated Report",
    esefreport: "ESEF Report",
    yes: HumanizedYesNoNa.Yes,
    no: HumanizedYesNoNa.No,
    na: HumanizedYesNoNa.NA,
    "eutaxonomy-financials": "EU Taxonomy for financial companies",
    "eutaxonomy-non-financials": "EU Taxonomy for non-financial companies",
    lksg: "LkSG",
    sfdr: "SFDR",
    sme: "SME",
    p2p: "WWF Pathway to Paris",
    inhouseproduction: "In-house Production",
    contractprocessing: "Contract Processing",
    hvcplastics: "HVC Plastics",
    contaminationofsoilwaterairornoiseemissionsorexcessivewaterconsumption:
      "Contamination of soil/water/air, noise emissions, excessive water consumption",
    useofmercuryormercurywaste: "Use of mercury, mercury waste (Minamata Convention)",
    productionanduseofpersistentorganicpollutants:
      "Production and use of persistent organic pollutants (POPs Convention)",
    exportimportofhazardouswaste: "Export/import of hazardous waste (Basel Convention)",
  };

  const lowerCaseText = rawText.toLowerCase();
  return lowerCaseText in mappingObject ? mappingObject[lowerCaseText] : "";
}

/**
 * convert string or number to a human-readable string
 * @param rawInput is the string or number to be converted to a human-readable string
 * @returns the converted string
 */
export function humanizeStringOrNumber(rawInput: string | number | null | undefined): string {
  if (typeof rawInput === "number") {
    return rawInput.toString();
  }
  if (!rawInput) {
    return "";
  }

  const frameworkLabel = getBaseFrameworkDefinition(rawInput)?.label;
  if (frameworkLabel) return frameworkLabel;
  const resultOfCustomMappingHumanisation = humanizeViaMapping(rawInput);
  return resultOfCustomMappingHumanisation == ""
    ? convertCamelCaseToSentenceCase(rawInput)
    : resultOfCustomMappingHumanisation;
}
