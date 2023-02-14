/**
 * Module to convert string to a human-readable text
 */

/**
 * convert camel case string to sentence case string using regex
 *
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
 *
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
    yes: "Yes",
    no: "No",
    na: "N/A",
    "eutaxonomy-financials": "EU Taxonomy for financial companies",
    "eutaxonomy-non-financials": "EU Taxonomy for non-financial companies",
    lksg: "LkSG",
    inhouseproduction: "In-house Production",
    contractprocessing: "Contract Processing",
  };
  const lowerCaseText = rawText.toLowerCase();
  return lowerCaseText in mappingObject ? mappingObject[lowerCaseText] : "";
}

/**
 * convert string to a human-readable text
 *
 * @param rawText is the string to be converted to a human-readable string
 * @returns the converted string
 */
export function humanizeString(rawText: string | null | undefined): string {
  if (!rawText) {
    return "";
  }
  const resultOfCustomMappingHumanisation = humanizeViaMapping(rawText);
  return resultOfCustomMappingHumanisation == ""
    ? convertCamelCaseToSentenceCase(rawText)
    : resultOfCustomMappingHumanisation;
}
