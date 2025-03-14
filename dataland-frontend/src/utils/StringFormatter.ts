/**
 * Module to convert string to a human-readable text
 */

import { HumanizedYesNoNa } from '@/utils/YesNoNa';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { DataTypeEnum } from '@clients/backend';
import { getBasePrivateFrameworkDefinition } from '@/frameworks/BasePrivateFrameworkRegistry';
import { DocumentMetaInfoDocumentCategoryEnum, type DocumentMetaInfoResponse } from '@clients/documentmanager';

/**
 * convert kebab case string to pascal case string using regex
 * @param rawText is the string to be converted
 * @returns the converted string in pascal case
 */
export function convertKebabCaseToPascalCase(rawText: string): string {
  const camelCase = rawText.replace(/-([a-z])/g, (_, char: string) => char.toUpperCase());
  return camelCase.charAt(0).toUpperCase() + camelCase.slice(1);
}

/**
 * convert camel case string to sentence case string using regex
 * @param rawText is the string to be converted to a human-readable string
 * @returns the converted string in "sentence-case"
 */
function convertCamelCaseToSentenceCase(rawText: string): string {
  // Split the sting to words
  const processedText = rawText.replace(/((?!^)[A-Z])/g, ' $1');
  // uppercase the first letter of the first word
  return processedText.charAt(0).toUpperCase() + processedText.slice(1);
}

/**
 * convert camel case string to a string with spaces
 * @param rawText is the string to be converted to a human-readable string
 * @returns the converted string with "Words With Spaces"
 */
export function convertCamelCaseToWordsWithSpaces(rawText: string): string {
  return rawText
    .toString()
    .replace(/([A-Z])/g, ' $1')
    .trim();
}

/**
 * get the representable text from the mapping object
 * @param rawText is the string to be converted to a human-readable string
 * @returns the converted string
 */
function humanizeViaMapping(rawText: string): string {
  const mappingObject: { [key: string]: string } = {
    isin: 'ISIN',
    permid: 'PermID',
    lei: 'LEI',
    ticker: 'Ticker',
    duns: 'DUNS',
    eligiblecapex: 'Eligible CapEx',
    eligibleopex: 'Eligible OpEx',
    alignedcapex: 'Aligned CapEx',
    alignedopex: 'Aligned OpEx',
    insuranceorreinsurance: 'Insurance or Reinsurance',
    annualreport: 'Annual Report',
    sustainabilityreport: 'Sustainability Report',
    integratedreport: 'Integrated Report',
    esefreport: 'ESEF Report',
    yes: HumanizedYesNoNa.Yes,
    no: HumanizedYesNoNa.No,
    na: HumanizedYesNoNa.NA,
    'eutaxonomy-financials': 'EU Taxonomy for financial companies',
    'eutaxonomy-non-financials': 'EU Taxonomy for non-financial companies',
    lksg: 'LkSG',
    sfdr: 'SFDR',
    sme: 'SME',
    p2p: 'WWF Pathways to Paris',
    inhouseproduction: 'In-house Production',
    contractprocessing: 'Contract Processing',
    hvcplastics: 'HVC Plastics',
    contaminationofsoilwaterairornoiseemissionsorexcessivewaterconsumption:
      'Contamination of soil/water/air, noise emissions, excessive water consumption',
    useofmercuryormercurywaste: 'Use of mercury, mercury waste (Minamata Convention)',
    productionanduseofpersistentorganicpollutants:
      'Production and use of persistent organic pollutants (POPs Convention)',
    exportimportofhazardouswaste: 'Export/import of hazardous waste (Basel Convention)',
    policy: 'Policy',
    other: 'Other Document',
  };

  const lowerCaseText = rawText.toLowerCase();
  return lowerCaseText in mappingObject ? mappingObject[lowerCaseText] : '';
}

/**
 * convert string or number to a human-readable string
 * @param rawInput is the string or number to be converted to a human-readable string
 * @returns the converted string
 */
export function humanizeStringOrNumber(rawInput: string | number | null | undefined): string {
  if (typeof rawInput === 'number') {
    return rawInput.toString();
  }
  if (!rawInput) {
    return '';
  }

  const frameworkLabel =
    getBasePublicFrameworkDefinition(rawInput)?.label ?? getBasePrivateFrameworkDefinition(rawInput)?.label;
  if (frameworkLabel) return frameworkLabel;
  const resultOfCustomMappingHumanisation = humanizeViaMapping(rawInput);
  return resultOfCustomMappingHumanisation === ''
    ? convertCamelCaseToSentenceCase(rawInput)
    : resultOfCustomMappingHumanisation;
}
/**
 * Return the title of a framework
 * @param framework dataland framework
 * @returns title of framework
 */
export function getFrameworkTitle(framework: string): string {
  switch (framework) {
    case DataTypeEnum.EutaxonomyFinancials:
      return 'EU Taxonomy';
    case DataTypeEnum.EutaxonomyNonFinancials:
      return 'EU Taxonomy';
    case DataTypeEnum.P2p:
      return 'WWF';
    case DataTypeEnum.EsgDatenkatalog:
      return 'ESG Datenkatalog';
    default:
      return humanizeStringOrNumber(framework);
  }
}
/**
 * Checks the existence of subtitle for framework
 * @param framework dataland framework
 * @returns boolean if framework has subtitle
 */
export function frameworkHasSubTitle(framework: string): boolean {
  return (
    framework == DataTypeEnum.P2p ||
    framework == DataTypeEnum.EutaxonomyFinancials ||
    framework == DataTypeEnum.EutaxonomyNonFinancials ||
    framework == DataTypeEnum.EsgDatenkatalog
  );
}
/**
 * Return the subtitle of a framework
 * @param framework dataland framework
 * @returns subtitle of framework
 */
export function getFrameworkSubtitle(framework: string): string {
  switch (framework) {
    case DataTypeEnum.EutaxonomyFinancials:
      return 'for financial companies';
    case DataTypeEnum.EutaxonomyNonFinancials:
      return 'for non-financial companies';
    case DataTypeEnum.P2p:
      return 'Pathways to Paris';
    case DataTypeEnum.EsgDatenkatalog:
      return 'für Corporate Schuldscheindarlehen';
    default:
      return '';
  }
}

/**
 * Return the human readable plural of a report category
 * @param category document category
 * @returns title of category
 */
export function getPluralCategory(category: string): string {
  switch (category) {
    case DocumentMetaInfoDocumentCategoryEnum.Policy:
      return 'Policies';
    case DocumentMetaInfoDocumentCategoryEnum.AnnualReport:
      return 'Annual Reports';
    case DocumentMetaInfoDocumentCategoryEnum.SustainabilityReport:
      return 'Sustainability Reports';
    case DocumentMetaInfoDocumentCategoryEnum.Other:
      return 'Other Reports';
    default:
      return humanizeStringOrNumber(category);
  }
}

/**
 * Returns the filename to a given document, that has not more than 28 characters.
 * @param document The document of interest
 * @return A string containing the eventually shortened name
 */
export function truncatedDocumentName(document: DocumentMetaInfoResponse): string {
  const name = document.documentName ?? document.documentId;
  return name.length > 28 ? name.slice(0, 25) + '...' : name;
}
